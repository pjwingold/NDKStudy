package com.pjwin.ndkstudy;

import android.app.Application;
import android.content.ContentResolver;

/**
 * Created by hans on 23-Feb-16.
 */
public class NdkApplication extends Application {
    private static NdkApplication mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    public static NdkApplication getInstance() {
        return mApp;
    }

    public static ContentResolver myContentResolver() {
        return mApp.getContentResolver();
    }
}
