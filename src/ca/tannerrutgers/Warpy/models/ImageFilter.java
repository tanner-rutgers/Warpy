package ca.tannerrutgers.Warpy.models;

import android.graphics.Bitmap;

/**
 * Created by Tanner on 1/18/14.
 */
public abstract class ImageFilter {

    public static final int SIZE_MIN = 3;
    public static final int SIZE_DEFAULT = 3;

    protected Bitmap bitmap;
    protected int maskSize;

    public boolean cancelFiltering;

    public ImageFilter(Bitmap bitmap) {
        this(bitmap, SIZE_DEFAULT);
    }

    public ImageFilter(Bitmap bitmap, int maskSize) {
        this.bitmap = bitmap;

        // Do not let mask size be larger than bitmap
        int maxSize = Math.min(bitmap.getWidth(), bitmap.getHeight());

        if (maskSize < SIZE_MIN) {
            maskSize = SIZE_MIN;
        } else if (maskSize > maxSize) {
            maskSize = maxSize;
        }

        this.maskSize = maskSize;
        this.cancelFiltering = false;
    }

    /**
     * Method that applies filter to the bitmap.
     * Must be implemented by inheriting objects.
     */
    public abstract Bitmap applyFilter();
}
