package com.pjwin.ndkstudy.task;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pjwin.ndkstudy.ImageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hans on 09-Jan-16.
 */
public class ApplyEffectService extends Service {
    private static final String TAG = "ApplyEffectService";
    public static final String SERVICE_METHOD = "method";
    public static final String RESULT_CODE = "result_code";
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final String ACTION = ApplyEffectService.class.getCanonicalName();

    private final ApplyEffectServiceBinder mBinder = new ApplyEffectServiceBinder();
    private Bitmap mResultBitmap;
    private Bitmap mSourceBitmap;
    private ExecutorService mService;
    private int mMethod;
    private int resultCode;

    public class ApplyEffectServiceBinder extends Binder {
        public ApplyEffectService getService() {
            return ApplyEffectService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setSourceBitmap(Bitmap bitmap) {
        mSourceBitmap = bitmap;
    }

    public void processImage(int method) {
        Log.i(TAG, Thread.currentThread().getId() + " id");
        mMethod = method;
        resultCode = SUCCESS;
        ProcessImage processImage = new ProcessImage();
        Log.i("Service", Runtime.getRuntime().availableProcessors() + " cores");

        mService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            mService.submit(processImage);
        } finally {
            mService.shutdown();
        }
    }

    private void finished() {
        Intent in = new Intent(ACTION);
        in.putExtra(RESULT_CODE, resultCode);
        LocalBroadcastManager.getInstance(ApplyEffectService.this).sendBroadcast(in);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Bitmap getResultBitmap() {
        return mResultBitmap;
    }

    private class ProcessImage implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, Thread.currentThread().getId() + " id");
            switch (mMethod) {
                case 1 ://java
                    //SystemClock.sleep(20000);
                    mResultBitmap = ImageUtil.toImageRelief(mSourceBitmap);
                    break;
                case 2://jni
                    mResultBitmap = ImageUtil.toImageReliefJni(mSourceBitmap);
                    break;
                default:
                    break;
            }

            finished();
        }
    }


}