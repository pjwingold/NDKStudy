package com.pjwin.ndkstudy;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pjwin.ndkstudy.entity.Customer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private int count = 0;
    private String labelStr = "";
    private Customer customer;

    static {
        System.loadLibrary("ndk_study");
        //System.loadLibrary("ndk_test");
    }

    private ImageView imageView;
    private Bitmap mainImage;
    private Button resetBtn, javaUpdateBtn, jniUpdateBtn;
    private DisplayMetrics dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customer = new Customer();

        TextView tv = (TextView) findViewById(R.id.myText);
        imageView = (ImageView) findViewById(R.id.image);
        resetBtn = (Button) findViewById(R.id.reset);
        javaUpdateBtn = (Button) findViewById(R.id.javaUpdate);
        jniUpdateBtn = (Button) findViewById(R.id.jniUpdate);
        resetBtn.setOnClickListener(this);
        javaUpdateBtn.setOnClickListener(this);
        jniUpdateBtn.setOnClickListener(this);

        tv.setText(getNDKString());
        //Log.i(TAG, getNDKTest());
        incrementCount(10);
        incrementCount(5);

        setLabelStrNative("WHO");
        Log.i(TAG, labelStr);
        labelStrAppend(" ARE YOU");
        Log.i(TAG, String.valueOf(count));
        Log.i(TAG, labelStr);

        //mainImage = ((BitmapDrawable) (imageView.getDrawable())).getBitmap();
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        loadImage();
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
        Log.i(TAG, "onclick");
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

    private void javaUpdate() {
        long start = System.currentTimeMillis();
        Bitmap jBitmap = ImageUtil.toImageRelief(mainImage);
        imageView.setImageBitmap(jBitmap);
        long after = System.currentTimeMillis();

        Log.i(TAG, " " + (after - start) + " Java");
        Toast.makeText(this, "Process finished", Toast.LENGTH_SHORT).show();
    }

    private void jniUpdate() {
        long start = System.currentTimeMillis();

        Bitmap jBitmap = ImageUtil.toImageReliefJni(mainImage);

        imageView.setImageBitmap(jBitmap);
        long after = System.currentTimeMillis();

        Log.i(TAG, " " + (after - start) + " JNI");
    }

    private void resetImage() {
        imageView.setImageBitmap(mainImage);
    }

}
