package ca.tannerrutgers.ImageWarp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Tanner on 22/01/14.
 */
public class BitmapUtils {

    /**
     * Retrieve Bitmap located at given filepath scaled to reqWidth x requiredHeight
     * @param filepath Filepath of Bitmap
     * @param reqWidth Required width of returned Bitmap
     * @param reqHeight Required height of returned Bitmap
     */
    public static Bitmap decodeSampledBitmapFromFilepath(String filepath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    /**
     * Calculates sample size to use in the passed options so that the Bitmap
     * represeneted by options can be scaled to reqWidth and reqHeight
     * @param options Options of corresponding Bitmap
     * @param reqWidth Required width of Bitmap
     * @param reqHeight Required height of Bitmap
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

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
     * Returns pixel map of given Bitmap
     * @param bitmap Bitmap to retrieve pixels from
     * @return int[] representing pixels of Bitmap
     */
    public static int[] getPixels(Bitmap bitmap) {

        if (bitmap == null) {
            return new int[0];
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width*height];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        return pixels;
    }
}
