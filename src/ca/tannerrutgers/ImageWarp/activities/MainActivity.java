package ca.tannerrutgers.ImageWarp.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import ca.tannerrutgers.ImageWarp.R;
import ca.tannerrutgers.ImageWarp.dialogs.DiscardImageWarningDialog;
import ca.tannerrutgers.ImageWarp.dialogs.ImageSelectionDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements ImageSelectionDialog.ImageSelectionDialogListener, DiscardImageWarningDialog.DiscardImageWarningDialogListener {

    private static final String APP_TAG = "ImageWarp";

    private static final int REQUEST_LOAD_IMAGE = 0;
    private static final int REQUEST_TAKE_PICTURE = 1;

    // Current state variables
    private Uri currentImage;
    private boolean currentImageSaved;
    private boolean backPressed;

    // View items
    private ImageView imageView;
    private Button applyFilterButton;

    // ASyncTask used for filtering
//    private FilterTask filterTask;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set default values if first time launching
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Retrieve saved mask size
//        selectedMaskSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_mask_size",ImageFilter.SIZE_DEFAULT);

        // Initialize state
        currentImageSaved = false;
        backPressed = false;

        // Initialize views
        imageView = (ImageView)findViewById(R.id.imageView);
        applyFilterButton = (Button)findViewById(R.id.applyFilterButton);
    }

//    /**
//     * Called when the activity is resumed
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        selectedMaskSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_mask_size",ImageFilter.SIZE_DEFAULT);
//    }

//    /**
//     * Called when the activity is destroyed
//     */
//    @Override
//    public void onDestroy() {
//        // Cancel current filter task if needed
//        if (filterTask != null && filterTask.getStatus() != AsyncTask.Status.FINISHED) {
//            filterTask.cancel(true);
//        }
//        super.onDestroy();
//    }

    /**
     * Called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (currentImage != null && !currentImageSaved) {
            backPressed = true;
            showDiscardImageWarningDialog();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflates options menu in menu bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

//    /**
//     * Called when an option is selected in the menu bar
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            // Settings has been selected
//            case R.id.menu_settings:
//                launchPreferences();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

//    /**
//     * Launch preferences activity
//     */
//    private void launchPreferences() {
//        Intent preferencesIntent = new Intent(MainActivity.this, SettingsActivity.class);
//
//        // Pass maximum possible mask size to preferences if image is loaded
//        if (currentImage != null) {
//            int maxSize = Math.min(currentImage.getWidth(), currentImage.getHeight());
//            preferencesIntent.putExtra("max_mask_size", maxSize);
//        }
//
//        startActivity(preferencesIntent);
//    }

    /**
     * Handler for when select image button is clicked.
     * Launches a dialog to choose how to load the picture.
     */
    public void selectImageClicked(View v) {
        if (currentImage != null && !currentImageSaved) {
            showDiscardImageWarningDialog();
        } else {
            showImageSelectionDialog();
        }
    }

    /**
     * Display dialog letting user choose how to load new image
     */
    private void showImageSelectionDialog() {
        DialogFragment imageSelection = new ImageSelectionDialog();
        imageSelection.show(getFragmentManager(), "imageSelection");
    }

    /**
     * Display dialog warning user of image discard
     */
    private void showDiscardImageWarningDialog() {
        DialogFragment discardImageWarning = new DiscardImageWarningDialog();
        discardImageWarning.show(getFragmentManager(), "discardImageWarning");
    }

    /**
     * Handle results from spawned activities
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        switch (requestCode) {
            // Request is image selection from gallery
            case REQUEST_LOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    // Retrieve selected image and update views
                    currentImage = returnedIntent.getData();
                    updateViews();
                }
                break;
            // Request is image from camera
            case REQUEST_TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    // Image Uri is already set when taking pictures, update views
                    updateViews();
                }
        }
    }

    /**
     * Set selected image to the image specified by the given Uri
     */
    private Bitmap getImageFromUri(Uri imageUri) {
        getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
        } catch (FileNotFoundException e) {
            Log.e(APP_TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(APP_TAG, e.getMessage());
        }
        return bitmap;
    }

    /**
     * Called when user selects load image from image selection dialog
     */
    @Override
    public void onLoadImageSelection() {
        Intent loadImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        loadImageIntent.setType("image/*");
        if (loadImageIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(loadImageIntent, REQUEST_LOAD_IMAGE);
        }
    }

    /**
     * Called when user selects take picture from image selection dialog
     */
    @Override
    public void onTakePictureSelection() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                photoFile.delete();
            } catch (IOException ex) {
                Log.e(APP_TAG, "Error occured creating temp image file");
            }
            if (photoFile != null) {
                currentImage = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImage);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
            }
        }
    }

    /**
     * Called when user clicks ok on discard image warning
     */
    @Override
    public void onDiscardImageSelection() {
        if (backPressed) {
            backPressed = false;
            super.onBackPressed();
        } else {
            showImageSelectionDialog();
        }
    }

    /**
     * Called when user clicks cancel on discard image warning
     */
    @Override
    public void onDiscardCancelSelection() {
        backPressed = false;
    }

    /**
     * Create temporary image file for camera intent
     * @return the temporary image file
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

//    /**
//     * Returns the Bitmap located at the location filepath
//     * scaled down to the image view's resolution if needed
//     * @param filepath Path of original bitmap
//     * @return Scaled down Bitmap
//     */
//    private Bitmap getScaledBitmapFromFilepath(String filepath) {
//
//        int viewWidth = imageView.getWidth();
//        int viewHeight = imageView.getHeight();
//
//        return BitmapUtils.decodeSampledBitmapFromFilepath(filepath, viewWidth, viewHeight);
//    }

    /**
     * Updates views attached to this activity
     */
    private void updateViews() {
        if (currentImage != null) {
            imageView.setBackground(null);
            imageView.setImageBitmap(getImageFromUri(currentImage));
            applyFilterButton.setEnabled(true);
        }
    }

//    /**
//     * Class allowing image filtering to occur in background
//     */
//    private class FilterTask extends AsyncTask<ImageFilter, Void, Bitmap> {
//
//        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
//
//        ImageFilter filter;
//
//        /**
//         * Setup dialog
//         */
//        @Override
//        protected void onPreExecute() {
//            dialog.setMessage("Filtering...");
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setCancelable(true);
//
//            // Cancel task and filtering if dialog is cancelled
//            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    filter.cancelFiltering = true;
//                    cancel(true);
//                }
//            });
//            dialog.show();
//        }
//
//        /**
//         * Run filter in background task
//         */
//        @Override
//        protected Bitmap doInBackground(ImageFilter... filters) {
//            filter = filters[0];
//            return filter.applyFilter();
//        }
//
//        /**
//         * Called when background task is finished
//         */
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            if (dialog.isShowing()) {
//                dialog.dismiss();
//            }
//            currentImage = result;
//            updateViews();
//        }
//
//        /**
//         * Called when the current task is cancelled
//         */
//        @Override
//        protected void onCancelled(Bitmap result) {
//            filter.cancelFiltering = true;
//            onCancelled();
//        }
//    }
}
