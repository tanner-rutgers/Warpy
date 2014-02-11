package ca.tannerrutgers.Warpy.models;

import android.graphics.Bitmap;
import android.graphics.Color;
import ca.tannerrutgers.Warpy.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Tanner on 1/18/14.
 */
public class MedianFilter extends ImageFilter {

    public MedianFilter(Bitmap image) {
        super(image);
    }

    public MedianFilter(Bitmap image, int size) {
        super(image, size);
    }

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

                ArrayList<Integer> reds = new ArrayList<Integer>(offset);
                ArrayList<Integer> greens = new ArrayList<Integer>(offset);
                ArrayList<Integer> blues = new ArrayList<Integer>(offset);
                ArrayList<Integer> alphas = new ArrayList<Integer>(offset);

                // Retrieve mask pixels and calculate median value for new pixel
                // This is done primitively for efficiency
                for (int row = y-offset; row <= y+offset; row++) {
                    for (int col = x-offset; col <= x+offset; col++) {
                        if (row >= 0 && col >= 0 && row < height && col < width) {

                            int color = pixels[row*width+col];

                            reds.add(Color.red(color));
                            greens.add(Color.green(color));
                            blues.add(Color.blue(color));
                            alphas.add(Color.alpha(color));
                        }
                    }
                }

                int red = getMedian(reds);
                int green = getMedian(greens);
                int blue = getMedian(blues);
                int alpha = getMedian(alphas);

                pixels[y*width+x] = Color.argb(alpha,red,green,blue);
            }
        }
        return Bitmap.createBitmap(pixels,width,height,bitmap.getConfig());
    }

    /**
     * Returns the median value of the passed value list
     */
    private int getMedian(ArrayList<Integer> values) {
        Collections.sort(values);
        int size = values.size();

        if (size % 2 != 0) {
            return values.get(size/2);
        } else {
            return (values.get(size/2) + values.get(size/2 - 1))/2;
        }
    }
}
