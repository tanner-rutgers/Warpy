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
import ca.tannerrutgers.Warpy.activities.MainActivity;

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

    private static final String ALBUM_NAME = "Warpy";

    private ProgressDialog dialog;      // Spinner dialog to show when processing
    private MainActivity parent;            // Parent activity that started us
    private IOException exception;      // Exception to be handled later if occurs

    /**
     * Constructor - takes and stores calling activity
     */
    public SaveImageTask(MainActivity activity) {
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
                if (imageFile != null && image != null) {
                    OutputStream fOut = new FileOutputStream(imageFile);
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    MediaStore.Images.Media.insertImage(parent.getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), imageFile.getName());
                } else {
                    exception = new IOException("Tried to save null image");
                }
            } catch (IOException e) {
                exception = e;
            }
        } else {
            exception = new IOException("External storage not writable");
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
            Log.e(MainActivity.APP_TAG, exception.getMessage());
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
    public File createImageFile() throws IOException {
        File image = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "WARPED_" + timeStamp + "_";

        // Get storage directory for app
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ALBUM_NAME);
        if (!storageDir.exists() && !storageDir.mkdir()) {
            Log.e(MainActivity.APP_TAG, "Could not create image directory");
        } else {
            // Create image file
            image = new File(storageDir, imageFileName + ".jpg");
        }

        return image;
    }
}
