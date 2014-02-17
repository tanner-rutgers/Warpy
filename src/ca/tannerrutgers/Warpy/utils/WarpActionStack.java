/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Tanner Rutgers
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    // Resize stack, removing old entries if needed
    public void resize(int newSize) {
        maxSize = newSize;
        while (newSize < this.size()) {
            remove(0);
        }
    }

    /**
     * Create file from passed bitmap and push Uri onto stack
     */
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

    /**
     * Remove item from stack at given location, deleting file in cache as well
     */
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

    /**
     * Clear the stack, removing all cached files
     */
    @Override
    public void clear() {
        while (this.size() > 0) {
            this.remove(0);
        }
    }
}
