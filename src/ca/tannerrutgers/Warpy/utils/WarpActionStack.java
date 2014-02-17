package ca.tannerrutgers.Warpy.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import ca.tannerrutgers.Warpy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * Implementation of Stack with a fixed size and cached storage
 * Created by Tanner on 16/02/14.
 */
public class WarpActionStack<T> extends Stack<T> {

    private Context context;
    private int maxSize;

    public WarpActionStack(Context context, int size) {
        super();
        this.context = context;
        this.maxSize = size;
    }

    public T pushFromBitmap(Bitmap image) {

        // If the stack is too big, remove elements until it's the right size.
        while (this.size() >= maxSize) {
            this.remove(0);
        }

        // Save image in cache and add Uri to stack
        String cacheFolder = context.getCacheDir().getAbsolutePath();
        File tempFile = new File(cacheFolder + "/temp_warp" + this.size() + ".png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(context.getResources().getString(R.string.app_name), "Could not save temp image");
        }

        return super.push((T) Uri.fromFile(tempFile));
    }

    @Override
    public T remove(int location) {
        Uri fileUri = (Uri)super.get(location);
        if (fileUri != null) {
            File file = new File(fileUri.getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        return super.remove(location);
    }

    @Override
    public void clear() {
        while (this.size() > 0) {
            this.remove(0);
        }
    }
}
