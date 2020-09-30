package com.example.screenshotlistentest.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.screenshotlistentest.fragment.Page1Fragment;
import com.example.screenshotlistentest.fragment.Page2Fragment;
import com.example.screenshotlistentest.R;
import com.example.screenshotlistentest.manager.ScreenShotListenManager;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    public ScreenShotListenManager manager;
    MainHandler mainHandler = new MainHandler(MainActivity.this);
    CountTimeThread countTimeThread;

    private RadioGroup radioGroup;
    private Button getHelp;

    private Fragment[] mFragments;
    private Page1Fragment page1Fragment;
    private Page2Fragment page2Fragment;

    private int mIndex;

    private static class MainHandler extends Handler{
        private final int MSG_HIDE = 1;
        private final WeakReference<MainActivity> weakRef;

        public MainHandler(MainActivity pAcitivity){
            weakRef = new WeakReference<MainActivity>(pAcitivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MainActivity mainActivity = weakRef.get();
            if(mainActivity != null){
                switch (msg.what){
                    case MSG_HIDE:
                        mainActivity.hide();
                        break;
                }
            }
            super.handleMessage(msg);
        }

        /**
         * 发送信息
         */
        public void sendHideControlMessage(){
            obtainMessage(MSG_HIDE).sendToTarget();
        }
    }


    private class CountTimeThread extends Thread{
        private final long maxVisibleTime;
        private long startVisibleTime;

        public CountTimeThread(int second){
            maxVisibleTime = second * 1000;

            //设置为后台进程
            setDaemon(true);
        }

        private synchronized void reset(){
            startVisibleTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            startVisibleTime = System.currentTimeMillis();

            while (true){
                if((startVisibleTime + maxVisibleTime) < System.currentTimeMillis()){
                    mainHandler.sendHideControlMessage();
                }
            }
        }
    }

    /**
     * 输入定时显示时间，开启线程
     */
    private void startCountTimeThread(){
        countTimeThread = new CountTimeThread(5);
        countTimeThread.start();
    }

    /**
     * 隐藏按钮
     */
    private void hide(){
        getHelp.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
        manager = ScreenShotListenManager.newInstance(MainActivity.this);
        manager.startListen();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            listenScreenShot();
        }
    }


    /**
     * 对主活动界面进行初始化
     */
    private void initView(){
        getHelp = (Button)findViewById(R.id.getHelp);
        getHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,HelpActivity.class);
                startActivity(intent);
            }
        });
        radioGroup = (RadioGroup)findViewById(R.id.rgTab);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                for(int index = 0;index < group.getChildCount();index++){
                    RadioButton rb = (RadioButton)group.getChildAt(index);
                    if(rb.isChecked()){
                        setIndexSelected(index);
                        break;
                    }
                }
            }
        });
    }

    /**
     * 设置初始碎片
     */
    private void initFragment(){
        page1Fragment = new Page1Fragment();
        page2Fragment = new Page2Fragment();
        mFragments = new Fragment[]{page1Fragment,page2Fragment};
        FragmentManager fragmentManager = getSupportFragmentManager();
        //开启事务
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.fragments_contain,page1Fragment).commit();
        setIndexSelected(0);
    }

    /**
     *主活动两个碎片的切换
     */
    private void setIndexSelected(int index){
        if(mIndex == index){
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        //开启事务
        FragmentTransaction ft = fragmentManager.beginTransaction();
        //将前一个碎片隐藏
        ft.hide(mFragments[mIndex]);
        if(!mFragments[index].isAdded()){
            ft.add(R.id.fragments_contain,mFragments[index]).show(mFragments[index]);
        }else{
            ft.show(mFragments[index]);
        }

        ft.commit();
        mIndex = index;
    }

    /**
     * 截屏监听接口，实现具体逻辑
     */
    protected void listenScreenShot(){
        manager.setListener(new ScreenShotListenManager.OnScreenShotListener() {
            @Override
            public void onShot(String imagePath) {
                Toast.makeText(MainActivity.this,"截图监听成功，图片路径为：" + imagePath,Toast.LENGTH_SHORT).show();
                Log.d("MainActivity.class","imagePath:" + imagePath);
                getHelp.setVisibility(View.VISIBLE);
                startCountTimeThread();
            }
        });
    }

    /**
     * 结束活动时结束截屏监听
     */
    @Override
    protected void onDestroy() {
        manager.stopListen();
        super.onDestroy();
    }

    /**
     *确认权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    listenScreenShot();
                }
                break;
        }
    }
}