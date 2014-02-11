package ca.tannerrutgers.Warpy.models;

import android.graphics.Bitmap;
import android.graphics.Color;
import ca.tannerrutgers.Warpy.utils.BitmapUtils;

/**
 * Created by Tanner on 1/18/14.
 */
public class MeanFilter extends ImageFilter {

    public MeanFilter(Bitmap image) {
        super(image);
    }

    public MeanFilter(Bitmap image, int size) {
        super(image, size);
    }

    /**
     * Apply a mean filter to the Bitmap
     */
    @Override
    public Bitmap applyFilter() {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int offset = maskSize/2;

        // Retrieve pixels of bitmap for efficiency
        int[] pixels = BitmapUtils.getPixels(bitmap);

        // Iterate over all pixels of image determine new values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // If filtering has been asked to cancel, stop filtering
                if (cancelFiltering) {
                    return null;
                }

                int red = 0;
                int green = 0;
                int blue = 0;
                int alpha = 0;

                // Retrieve mask pixels and calculate mean value for new pixel
                // This is done primitively for efficiency
                int maskPixels = 0;
                for (int row = y-offset; row <= y+offset; row++) {
                    for (int col = x-offset; col <= x+offset; col++) {
                        if (row >= 0 && col >= 0 && row < height && col < width) {

                            int color = pixels[row*width+col];

                            red += Color.red(color);
                            green += Color.green(color);
                            blue += Color.blue(color);
                            alpha += Color.alpha(color);

                            maskPixels++;
                        }
                    }
                }

                red = red/maskPixels;
                green = green/maskPixels;
                blue = blue/maskPixels;
                alpha = alpha/maskPixels;

                // Set new pixel to mean value
                pixels[y*width+x] = Color.argb(alpha,red,green,blue);
            }
        }
        return Bitmap.createBitmap(pixels,width,height,bitmap.getConfig());
    }


}
