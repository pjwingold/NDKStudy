package com.pjwin.ndkstudy.task;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.pjwin.ndkstudy.ImageUtil;

/**
 * Created by hans on 07-Jan-16.
 */
public class ApplyEffectThread implements Runnable {
    private Bitmap mBitmap;
    private int mMethod;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Bitmap bitmap = (Bitmap) msg.obj;
                if (mCallback != null) {
                    mCallback.onProcessedFinished(bitmap);
                }
            }
        }
    };
    private ProcessImageCallback mCallback;

    public interface ProcessImageCallback {
        void onProcessedFinished(Bitmap bitmap);
    }

    public ApplyEffectThread(Bitmap mBitmap, int mMethod, ProcessImageCallback callback) {
        this.mBitmap = mBitmap;
        this.mMethod = mMethod;
        mCallback = callback;
        //can check if is fragment and make sure getRetainInstance() is true
        //Fragment fragment = (Fragment) callback;
        //fragment.getRetainInstance()
    }

    @Override
    public void run() {
        Message message = mHandler.obtainMessage();// Message.obtain(mHandler);
        Bitmap result = null;

        switch (mMethod) {
            case 1 ://java
                //SystemClock.sleep(2000);

                result = ImageUtil.toImageRelief(mBitmap);
                break;
            case 2://jni
                result = ImageUtil.toImageReliefJni(mBitmap);
                break;
            default:
                break;
        }

        message.obj = result;
        message.what = 1;
        message.sendToTarget();
    }
}