package com.pjwin.ndkstudy;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * Created by hans on 14-Dec-15.
 */
public class ImageUtil {
    static {
        System.loadLibrary("ndk_study");
    }

    public static Bitmap decodeBitmapFromResource(Resources resources, int rId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        BitmapFactory.decodeResource(resources, rId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resources, rId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 3;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Applies the Relief effect on a Bitmap, pixel by pixel
     *
     * @param source
     * @return
     */
    public static Bitmap toImageRelief(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int before = source.getPixel(0, 0), after = 0;//pixel

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int current = source.getPixel(i, j);
                int r = Color.red(current) - Color.red(after) +127;
                int g = Color.green(current) - Color.green(after) +127;
                int b = Color.blue(current) - Color.blue(after) +127;
                int a = Color.alpha(current);

                int dest = Color.argb(a, r, g, b);
                result.setPixel(i, j, dest);

                after = before;
                before = current;
            }
        }

        return result;
    }

    /**
     * Calls JNI function to apply the relief effect
     *
     * @param source
     * @return
     */
    public static Bitmap toImageReliefJni(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] buffer = new int[width * height];

        source.getPixels(buffer, 0, width, 1, 1, width - 1, height - 1);
        int[] jniBuffer = toImageReliefNative(buffer, width, height);

        return Bitmap.createBitmap(jniBuffer, width, height, Bitmap.Config.RGB_565);

    }

    public static native int[] toImageReliefNative(int[] buffer, int width, int height);
}
