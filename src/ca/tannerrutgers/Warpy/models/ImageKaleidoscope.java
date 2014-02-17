package ca.tannerrutgers.Warpy.models;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by Tanner on 12/02/14.
 */
public class ImageKaleidoscope extends ImageWarp {
    public ImageKaleidoscope(Context context, Bitmap image) {
        super(context, image);
    }

    @Override
    public Bitmap applyWarp() {
        warpScript.invoke_kaleidoscope_warp();
        warpOutAllocation.copyTo(mImage);
        return mImage;
    }
}
