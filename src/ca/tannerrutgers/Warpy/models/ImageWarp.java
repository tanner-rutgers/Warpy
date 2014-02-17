package ca.tannerrutgers.Warpy.models;

import ca.tannerrutgers.Warpy.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.*;

/**
 * Created by Tanner on 12/02/14.
 */
public abstract class ImageWarp {

    protected Bitmap mImage;                // Image for warping
    protected ScriptC_warp warpScript;      // RenderScript script instance for warping
    Allocation warpOutAllocation;

    public ImageWarp(Context context, Bitmap image) {
        this.mImage = image;

        // Initialize RenderScript
        RenderScript warpRS = RenderScript.create(context);
        Allocation warpInAllocation = Allocation.createFromBitmap(warpRS, image, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        warpOutAllocation = Allocation.createTyped(warpRS, warpInAllocation.getType());
        warpScript = new ScriptC_warp(warpRS, context.getResources(), R.raw.warp);

        // Set common RenderScript fields
        warpScript.set_image_width(image.getWidth());
        warpScript.set_image_height(image.getHeight());
        warpScript.set_is_cancelled(false);
        warpScript.bind_input(warpInAllocation);
        warpScript.bind_output(warpOutAllocation);
    }

    public Bitmap getImage() {
        return mImage;
    }

    // Cancel current warp
    public void cancelWarp() {
        if (warpScript != null) {
            warpScript.set_is_cancelled(true);
        }
    }

    // Apply warp to the Bitmap instance
    abstract public Bitmap applyWarp();
}
