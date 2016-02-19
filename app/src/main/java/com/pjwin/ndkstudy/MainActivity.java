package com.pjwin.ndkstudy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pjwin.ndkstudy.entity.Customer;
import com.pjwin.ndkstudy.task.ApplyEffectService;
import com.pjwin.ndkstudy.task.ApplyEffectThread;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ApplyEffectThread.ProcessImageCallback {
    private static final String TAG = "MainActivity";
    private int count = 0;
    private String labelStr = "";
    private Customer customer;

    static {
        System.loadLibrary("ndk_study");
    }

    private ImageView imageView;
    private static Bitmap mainImage;
    private Button resetBtn, javaUpdateBtn, jniUpdateBtn;
    private DisplayMetrics dm;
    private TextView titleText;
    private ProgressBar pb;

    private ExecutorService mService;
    private ApplyEffectService applyEffectService;
    private boolean mBound;
    private long start;
    private long after;

    public static final int PROCESS_JAVA = 1;
    public static final int PROCESS_JNI = 2;

    private int processId;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customer = new Customer();

        titleText = (TextView) findViewById(R.id.titleText);
        imageView = (ImageView) findViewById(R.id.image);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        resetBtn = (Button) findViewById(R.id.reset);
        javaUpdateBtn = (Button) findViewById(R.id.javaUpdate);
        jniUpdateBtn = (Button) findViewById(R.id.jniUpdate);
        resetBtn.setOnClickListener(this);
        javaUpdateBtn.setOnClickListener(this);
        jniUpdateBtn.setOnClickListener(this);

        titleText.setText(getNDKString());
        //Log.i(TAG, getNDKTest());
        incrementCount(10);
        incrementCount(5);

        setLabelStrNative("WHO");
        Log.i(TAG, labelStr);
        labelStrAppend(" ARE YOU");
        Log.i(TAG, String.valueOf(count));
        Log.i(TAG, labelStr);

        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        loadImage();

        serviceIntent = new Intent(this, ApplyEffectService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        mService = Executors.newSingleThreadExecutor();

        //finish();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void loadImage() {
        mainImage = ImageUtil.decodeBitmapFromResource(getResources(), R.drawable.pic, dm.widthPixels, dm.heightPixels);
        imageView.setImageBitmap(mainImage);
    }

    private native String getNDKString();

    private native String getNDKTest();

    private native void incrementCount(int inc);

    private native void setLabelStrNative(String str);

    private native void labelStrAppend(String appStr);

    private native void updateCustomerNative();

    @Override
    public void onClick(View v) {
        Log.i(TAG, Thread.currentThread().getId() + " id");
        int vId = v.getId();
        switch (vId) {
            case R.id.javaUpdate:
                javaUpdate();
                break;
            case R.id.jniUpdate:
                jniUpdate();
                break;
            case R.id.reset:
                resetImage();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        IntentFilter filter = new IntentFilter(ApplyEffectService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(ApplyEffectService.RESULT_CODE, ApplyEffectService.SUCCESS);
            //if (resultCode == ApplyEffectService.SUCCESS) {
                onImageProcessFinished(applyEffectService.getResultBitmap());
            //}
            //unbindService(mConnection);
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ApplyEffectService.ApplyEffectServiceBinder mBinder = (ApplyEffectService.ApplyEffectServiceBinder) service;
            applyEffectService = mBinder.getService();
            applyEffectService.setSourceBitmap(mainImage);
            mBound = true;
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    private void javaUpdate() {
        //finish();
        start = System.currentTimeMillis();
        processId = PROCESS_JAVA;
        pb.setVisibility(View.VISIBLE);

        processWithService();
        //processWithThread();
    }

    private void jniUpdate() {
        start = System.currentTimeMillis();
        processId = PROCESS_JNI;
        pb.setVisibility(View.VISIBLE);

        processWithService();
        //processWithThread
    }

    private void processWithThread() {
        mService.execute(new ApplyEffectThread(mainImage, processId, this));
    }

    private void processWithIntentService() {

    }

    private void processWithService() {
        //serviceIntent.putExtra(ApplyEffectService.SERVICE_METHOD, processId);
        //startService(serviceIntent);
        if (mBound) {
            applyEffectService.processImage(processId);
        }
    }

    private void resetImage() {
        processId = 0;
        imageView.setImageBitmap(mainImage);
    }

    @Override
    protected void onDestroy() {
        //mService.shutdown();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onProcessedFinished(Bitmap bitmap) {
        onImageProcessFinished(bitmap);
    }

    private void onImageProcessFinished(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        after = System.currentTimeMillis();
        pb.setVisibility(View.GONE);

        switch (processId) {
            case PROCESS_JAVA:
                Log.i(TAG, " " + (after - start) + " Java");
                titleText.setText(String.format(Locale.getDefault(), "Time taken to completed process in Java: (%f) seconds", (float) (after - start) / 1000));
                break;
            case PROCESS_JNI :
                Log.i(TAG, " " + (after - start) + " JNI");
                titleText.setText(String.format(Locale.getDefault(), "Time taken to completed process with JNI: (%f) seconds", (float) (after - start) / 1000));
                break;
            default:
                break;
        }
    }
}
