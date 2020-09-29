package com.example.screenshotlistentest.manager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ScreenShotListenManager {
    private static final String TAG = "ScreenShotListenManager";

    /**
     * 读取媒体数据库时要进行读取的列
     */
    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    private static final String[] MEDIA_PROJECTIONS_API_16 = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
    };

    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap"
    };

    private static Point sScreenRealSize;

    /**用来记录已经回调过的路径*/
    private final List<String> sHasCallbackPaths = new ArrayList<String>();

    private Context mContext;

    private OnScreenShotListener mListener;

    private long mStartListenTime;

    //内部存储器观察者
    private MediaContentObserver mInternalObserver;

    //外部存储器观察者
    private MediaContentObserver mExternalObserver;

    /** 运行在 UI 线程的 Handler, 用于运行监听器回调 */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private ScreenShotListenManager(Context context){
        if(context == null){
            throw new IllegalArgumentException("The context must not be null.");
        }
        mContext = context;

        /**获取屏幕的真是分辨率，用于之后的判断*/
        if(sScreenRealSize == null){
            sScreenRealSize = getRealScreenSize();
            if(sScreenRealSize != null){
                Log.d(TAG,"Screen Real Size:" + sScreenRealSize.x + " * " + sScreenRealSize.y);
            }else {
                Log.w(TAG,"Get screen real size failed.");
            }
        }
    }

    /**
     * 获得实例
     */
    public static ScreenShotListenManager newInstance(Context context){
        assertInMainThread();
        return new ScreenShotListenManager(context);
    }

    /**
     * 启动监听
     */
    public void startListen(){
        assertInMainThread();

        sHasCallbackPaths.clear();

        //记录开始监听的时间戳
        mStartListenTime = System.currentTimeMillis();

        //创建内容观察者，分别为内部内存和外存
        mInternalObserver = new MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI,mUiHandler);
        mExternalObserver = new MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,mUiHandler);

        //注册内容观察者
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false,//设定是否匹配Uri的派生Uri
                mInternalObserver
        );
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mExternalObserver
        );
    }

    public void stopListen(){
        assertInMainThread();

        //注销内容观察者
        if(mInternalObserver != null){
            try{
                mContext.getContentResolver().unregisterContentObserver(mInternalObserver);
            }catch (Exception e){
                e.printStackTrace();
            }
            mInternalObserver = null;
        }
        if(mExternalObserver != null){
            try{
                mContext.getContentResolver().unregisterContentObserver(mExternalObserver);
            }catch (Exception e){
                e.printStackTrace();
            }
            mExternalObserver = null;
        }

        //将数据清空
        mStartListenTime = 0;
        sHasCallbackPaths.clear();
    }

    /**
     * 对媒体内容库的改变进行处理
     */
    private void handleMediaContentChange(Uri contentUri){
        Cursor cursor = null;
        try{
            //数据改变时查询数据库中最后加入的一条数据
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    Build.VERSION.SDK_INT < 16 ? MEDIA_PROJECTIONS : MEDIA_PROJECTIONS_API_16,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );

            if(cursor == null){
                Log.e(TAG,"逻辑异常");
                return;
            }
            if(!cursor.moveToFirst()){
                Log.d(TAG,"找不到数据");
                return;
            }

            //获取各列的索引
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
            int widthIndex = -1;
            int heightIndex = -1;
            if (Build.VERSION.SDK_INT >= 16) {
                widthIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                heightIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
            }

            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);
            int width = 0;
            int height = 0;
            if (widthIndex >= 0 && heightIndex >= 0) {
                width = cursor.getInt(widthIndex);
                height = cursor.getInt(heightIndex);
            } else {
                // API 16 之前, 宽高要手动获取
                Point size = getImageSize(data);
                width = size.x;
                height = size.y;
            }

            // 处理获取到的行数据
            handleMediaRowData(data, dateTaken, width, height);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
    }

    private Point getImageSize(String imagePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //该项设置为true即在解码时只返回高、宽和Mime类型，不必申请内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new Point(options.outWidth,options.outHeight);
    }

    /**
     *处理获得的行数据
     */
    private void handleMediaRowData(String data, long dateTaken, int width, int height) {
        if (checkScreenShot(data, dateTaken, width, height)) {
            Log.d(TAG, "ScreenShot: path = " + data + "; size = " + width + " * " + height
                    + "; date = " + dateTaken);
            if (mListener != null && !checkCallback(data)) {
                mListener.onShot(data);
            }
        } else {
            Log.w(TAG, "Media content changed, but not screenshot: path = " + data
                    + "; size = " + width + " * " + height + "; date = " + dateTaken);
        }
    }

    /**
     *对得到的数据进行判断是否符合截屏条件
     */
    private boolean checkScreenShot(String data,long dateTaken,int width,int height){
        /**
         * 从时间判断
         */
        if(dateTaken < mStartListenTime || (System.currentTimeMillis() - dateTaken) > 10 * 1000){
            Log.d(TAG,System.currentTimeMillis() + "");
            return false;
        }

        /**
         * 从尺寸判断
         */
        if(sScreenRealSize != null){
            if(
                    !(
                            (width <= sScreenRealSize.x && height <= sScreenRealSize.y)
            ||
                                    (height <= sScreenRealSize.x && width <= sScreenRealSize.y)
                    )){
                return false;
            }
        }

        /**
         * 从路径判断
         */
        if(TextUtils.isEmpty(data)){
            return false;
        }

        data = data.toLowerCase();
        //判断路径中是否包含指定的关键字
        for(String keyWork : KEYWORDS){
            if(data.contains(keyWork)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断截屏动作是否已经回调过，若无则进行添加
     */
    private boolean checkCallback(String imagePath){
        if(sHasCallbackPaths.contains(imagePath)){
            return true;
        }
        if(sHasCallbackPaths.size() >= 20){
            for(int i = 0; i < 5; i++){
                sHasCallbackPaths.remove(0);
            }
        }
        sHasCallbackPaths.add(imagePath);
        return false;
    }

    /**
     * 媒体内容库观察者(用于观察媒体库的改变)
     */
    private class MediaContentObserver extends ContentObserver{

        private Uri mContentUri;

        public MediaContentObserver(Uri contentUri,Handler handler){
            super(handler);
            mContentUri = contentUri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handleMediaContentChange(mContentUri);
        }
    }

    /**
     * 获取屏幕分辨率
     */
    private Point getRealScreenSize() {
        Point screenSize = null;
        try {
            screenSize = new Point();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                defaultDisplay.getRealSize(screenSize);
            } else {
                try {
                    //此处运用了Android中的反射技术
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    screenSize.set(
                            (Integer) mGetRawW.invoke(defaultDisplay),
                            (Integer) mGetRawH.invoke(defaultDisplay)
                    );
                } catch (Exception e) {
                    screenSize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenSize;
    }

    /**
     * 设置截屏监听器
     */
    public void setListener(OnScreenShotListener listener){
        mListener = listener;
    }

    public interface OnScreenShotListener{
        void onShot(String imagePath);
    }

    /**
     * 没太弄清，功能应该是保证调用含该方法的方法时，保证是在主线程中进行的
     */
    private static void assertInMainThread(){
        if(Looper.myLooper() != Looper.getMainLooper()){
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            String methodMsg = null;
            if(elements != null && elements.length >= 4){
                methodMsg = elements[3].toString();
            }
            throw new IllegalStateException("Call the method must be in main thread:" + methodMsg);
        }
    }
}
