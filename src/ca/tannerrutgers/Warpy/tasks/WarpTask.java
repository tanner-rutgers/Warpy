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

        parent.setBitmap(warpedImage);
    }

    /**
     * Called when task is cancelled
     */
    @Override
    protected void onCancelled() {
        warper.cancelWarp();
        super.onCancelled();
    }
}
