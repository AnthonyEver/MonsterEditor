package com.dlvs.monstereditor;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.apkfuns.logutils.LogUtils;

/**
 * desc：项目application
 * author：haojie
 * date：2017-05-05
 */
public class App extends Application {
    private static App mApp;
    public static boolean debug = true;

    public static App getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        LogUtils.getLogConfig()
                .configAllowLog(debug)
                .configTagPrefix("editor")
                .configShowBorders(true)
                .configFormatTag("%d{HH:mm:ss} %t %c{-3}");
    }

    public static Context getAppContext() {
        return mApp;
    }

    public static Resources getAppResources() {
        return mApp.getResources();
    }

}
