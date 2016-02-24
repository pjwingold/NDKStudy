package com.pjwin.ndkstudy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pjwin.ndkstudy.entity.Customer;
import com.pjwin.ndkstudy.task.ApplyEffectService;
import com.pjwin.ndkstudy.task.ApplyEffectThread;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ApplyEffectThread.ProcessImageCallback {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_GALLERY = 1;
    private int count = 0;
    private String labelStr = "";
    private Customer customer;


    static {
        System.loadLibrary("ndk_study");
    }

    private ImageView imageView;
    private int ivWidth, ivHeight;
    private static Bitmap mainImage;
    private static Uri imageUri;

    private Button resetBtn, javaUpdateBtn, jniUpdateBtn;
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
        imageView.setOnClickListener(this);

        titleText.setText(getNDKString());
        //Log.i(TAG, getNDKTest());
        incrementCount(10);
        incrementCount(5);

        setLabelStrNative("WHO");
        Log.i(TAG, labelStr);
        labelStrAppend(" ARE YOU");
        Log.i(TAG, String.valueOf(count));
        Log.i(TAG, labelStr);

        Intent serviceIntent = new Intent(this, ApplyEffectService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        mService = Executors.newSingleThreadExecutor();

        //finish();
    }

    private void loadImage() {
        if (imageUri != null) {
            new LoadImageTask(this, imageUri, ivWidth, ivHeight).execute();
        }
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
            case R.id.image:
                openPhoto();
                break;
            default:
                break;
        }
    }

    private void openPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ivHeight = imageView.getHeight();
        ivWidth = imageView.getWidth();
        loadImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GALLERY :
                if (data != null) {
                    imageUri = data.getData();

                    applyEffectService.setSourceImageUri(imageUri);
                    applyEffectService.setReqWidth(ivWidth);
                    applyEffectService.setReqHeight(ivHeight);

                    imageView.setImageURI(imageUri);
                }
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(ApplyEffectService.RESULT_CODE, ApplyEffectService.SUCCESS);
            if (resultCode == ApplyEffectService.SUCCESS) {
                onImageProcessFinished(applyEffectService.getResultBitmap());
            }
            else if (resultCode == ApplyEffectService.ERROR) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "Error Processing Image", Toast.LENGTH_SHORT).show();
                imageView.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);
            }
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
        processWithService();
        //processWithThread();
    }

    private void jniUpdate() {
        start = System.currentTimeMillis();
        processId = PROCESS_JNI;
        processWithService();
        //processWithThread
    }

    private void processWithThread() {
        if (imageUri == null && mainImage == null) {
            return;
        }
        pb.setVisibility(View.VISIBLE);
        mService.execute(new ApplyEffectThread(mainImage, processId, this));
    }

    private void processWithIntentService() {

    }

    private void processWithService() {
        if (imageUri == null && mainImage == null) {
            return;
        }

        if (mBound) {
            pb.setVisibility(View.VISIBLE);
            applyEffectService.processImage(processId);
        }
    }

    private void resetImage() {
        if (imageUri == null) {
            return;
        }

        processId = 0;

        imageView.setImageURI(imageUri);
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
        pb.setVisibility(View.GONE);
        if (bitmap == null) {
            return;
        }
        imageView.setImageBitmap(bitmap);
        after = System.currentTimeMillis();

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

    private static class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<MainActivity> mRef;
        private Uri imageUri;
        private int reqWidth;
        private int reqHeight;

        public LoadImageTask(MainActivity activity, Uri imageUri, int reqWidth, int reqHeight) {
            mRef = new WeakReference<>(activity);
            this.imageUri = imageUri;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            MainActivity activity = mRef.get();
            if (activity == null || imageUri == null) {
                return null;
            }

            Bitmap result = null;
            try {
                result = ImageUtil.decodeBitmapFromUri(imageUri, reqWidth, reqHeight);
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            MainActivity activity = mRef.get();
            if (activity == null || imageUri == null) {
                return;
            }

            if (bitmap == null) {
                Toast.makeText(activity.getApplicationContext(), "Unable to load image.", Toast.LENGTH_SHORT).show();
            }

            activity.imageView.setImageBitmap(bitmap);
        }
    }
}