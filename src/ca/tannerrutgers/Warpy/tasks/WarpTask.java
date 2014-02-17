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

package ca.tannerrutgers.Warpy.tasks;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import ca.tannerrutgers.Warpy.R;
import ca.tannerrutgers.Warpy.activities.WarpyActivity;
import ca.tannerrutgers.Warpy.models.ImageWarp;

/**
 * Created by Tanner on 16/02/14.
 */
public class WarpTask extends AsyncTask<ImageWarp, Void, Bitmap> {

    private ProgressDialog dialog;      // Spinner dialog to show when processing
    private WarpyActivity parent;       // Parent activity that started us
    private ImageWarp warper;           // Warper instance

    /**
     * Constructor - takes and stores calling activity
     */
    public WarpTask(WarpyActivity activity) {
        super();
        this.parent = activity;
        this.dialog = new ProgressDialog(activity);
    }

    /**
     * Setup dialog
     */
    @Override
    protected void onPreExecute() {
        dialog.setMessage(parent.getResources().getString(R.string.warping));
        dialog.setCanceledOnTouchOutside(false);
        // Cancel task and filtering if dialog is cancelled
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
        dialog.show();
    }

    /**
     * Task to run in background
     */
    @Override
    protected Bitmap doInBackground(ImageWarp... params) {
        warper = null;
        if (params.length == 1) {
            warper = params[0];
        } else {
            return null;
        }

        // Save untouched image for undoing later
        parent.pushToActionStack(warper.getImage());

        // Warp image and return
        return warper.applyWarp();
    }

    /**
     * Called when background task is finished
     */
    @Override
    protected void onPostExecute(Bitmap warpedImage) {
        // Dismiss progress dialog
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        parent.setBitmap(warpedImage, false);
    }

    /**
     * Called when task is cancelled
     */
    @Override
    protected void onCancelled() {
        warper.cancelWarp();
        parent.popFromActionStack();
        super.onCancelled();
    }
}
