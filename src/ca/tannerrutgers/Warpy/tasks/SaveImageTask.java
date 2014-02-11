package ca.tannerrutgers.Warpy.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import ca.tannerrutgers.Warpy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that saves a Bitmap image in the background
 */
public class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {

    private ProgressDialog dialog;      // Spinner dialog to show when processing
    private Activity parent;            // Parent activity that started us
    private IOException exception;      // Exception to be handled later if occurs

    /**
     * Constructor - takes and stores calling activity
     */
    public SaveImageTask(Activity activity) {
        super();
        this.parent = activity;
        this.dialog = new ProgressDialog(activity);
    }

    /**
     * Setup dialog
     */
    @Override
    protected void onPreExecute() {
        dialog.setMessage(parent.getResources().getString(R.string.saving));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Task to run in background
     */
    @Override
    protected Void doInBackground(Bitmap... params) {
        Bitmap image = null;
        if (params.length == 1) {
            image = params[0];
        } else {
            return null;
        }

        // Save the passed Bitmap publicly
        if (isExternalStorageWritable()) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
                OutputStream fOut = new FileOutputStream(imageFile);
                if (image != null) {
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    MediaStore.Images.Media.insertImage(parent.getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), imageFile.getName());
                }
            } catch (IOException e) {
                exception = e;
            }
        } else {
            exception = new IOException("Tried to save null image");
        }

        return null;
    }

    /**
     * Called when background task is finished
     */
    @Override
    protected void onPostExecute(Void nothing) {
        // Dismiss progress dialog
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        // If exception occurred, notify user and log
        if (exception != null) {
            Toast.makeText(parent.getApplicationContext(), "Could not save image. Please try again.", Toast.LENGTH_SHORT);
            Log.e(parent.getResources().getString(R.string.app_name), exception.getMessage());
        }
    }

    /**
     * Determines if external storage is writable
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Create public image file
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        return image;
    }
}
