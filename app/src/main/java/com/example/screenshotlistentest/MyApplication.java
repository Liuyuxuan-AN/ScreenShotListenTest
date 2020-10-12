package com.example.screenshotlistentest;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

/**
 * author : 刘雨轩
 * e-mail : 1262610086@qq.com
 * date   : 2020/10/12 11:25
 * desc   :自定义一个Application类，实现ARouter的初始化。
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.openLog();
        ARouter.openDebug();
        //对ARouter进行初始化
        ARouter.init(this);
    }
}
