package ca.tannerrutgers.ImageWarp.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Toast;
import ca.tannerrutgers.ImageWarp.R;
import ca.tannerrutgers.ImageWarp.dialogs.DiscardImageWarningDialog;
import ca.tannerrutgers.ImageWarp.tasks.SaveImageTask;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements DiscardImageWarningDialog.DiscardImageWarningDialogListener {

    public static final String APP_TAG = "Warpy";

    private static final int REQUEST_LOAD_IMAGE = 0;
    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_BACK_PRESSED = 2;

    // Current state variables
    private Uri currentImageUri;
    private Bitmap currentImage;
    private boolean isCurrentImageSaved;
    private boolean isImageLoaded;

    // View items
    private ImageView imageView;

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
        isCurrentImageSaved = false;
        isImageLoaded = false;

        // Initialize views
        imageView = (ImageView)findViewById(R.id.imageView);
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
        if (currentImageUri != null && !isCurrentImageSaved) {
            showDiscardImageWarningDialog(REQUEST_BACK_PRESSED);
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
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Called before menu is created
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hide save option if no image is loaded
        if (!isImageLoaded || isCurrentImageSaved) {
            MenuItem save = menu.findItem(R.id.menu_save_image);
            if (save != null) {
                save.setEnabled(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Called when an option is selected in the menu bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            // Take picture has been selected
            case R.id.menu_take_picture:
                takePictureSelected();
                return true;
            // Load image has been selected
            case R.id.menu_load_image:
                loadImageSelected();
                return true;
            case R.id.menu_save_image:
                saveCurrentImage();
                return true;
            // Settings has been selected
            case R.id.menu_settings:
//                launchPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when take picture is selected from menu
     */
    private void takePictureSelected() {
        if (currentImage != null && !isCurrentImageSaved) {
            showDiscardImageWarningDialog(REQUEST_TAKE_PICTURE);
        } else {
            takePicture();
        }
    }

    /**
     * Called when load image is selected from menu
     */
    private void loadImageSelected() {
        if (currentImage != null && !isCurrentImageSaved) {
            showDiscardImageWarningDialog(REQUEST_LOAD_IMAGE);
        } else {
            loadImage();
        }
    }

    /**
     * Save currently loaded/warped image to external storage
     */
    private void saveCurrentImage() {
        SaveImageTask saveTask = new SaveImageTask(this);
        saveTask.execute(currentImage);
    }


//    /**
//     * Called when settings is selected from menu
//     */
//    private void launchPreferences() {
//        Intent preferencesIntent = new Intent(MainActivity.this, SettingsActivity.class);
//        startActivity(preferencesIntent);
//    }


    /**
     * Display dialog warning user of image discard
     */
    private void showDiscardImageWarningDialog(int requestType) {
        DialogFragment discardImageWarning = new DiscardImageWarningDialog(requestType);
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
                    currentImage = getImageFromUri(returnedIntent.getData());
                    updateViews(true);
                }
                break;
            // Request is image from camera
            case REQUEST_TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    // Retrieve taken picture and update views
                    currentImage = getImageFromUri(currentImageUri);
                    updateViews(true);
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
     * Launch an image selection activity to select an image to load
     */
    public void loadImage() {
        Intent loadImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        loadImageIntent.setType("image/*");
        if (loadImageIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(loadImageIntent, REQUEST_LOAD_IMAGE);
        }
    }

    /**
     * Launch a new camera activity to take a picture
     */
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createTempImageFile();
            } catch (IOException ex) {
                Log.e(APP_TAG, "Error occured creating temp image file");
            }
            if (photoFile != null) {
                currentImageUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
            }
        }
    }

    /**
     * Called when user clicks positive button on discard image warning
     */
    @Override
    public void onDiscardImageSelection(int requestType) {
        switch (requestType) {
            case REQUEST_TAKE_PICTURE:
                takePicture();
                break;
            case REQUEST_LOAD_IMAGE:
                loadImage();
                break;
            case REQUEST_BACK_PRESSED:
                super.onBackPressed();
                break;
        }
    }

    /**
     * Create temporary image file for camera intent
     * @return the temporary image file
     */
    private File createTempImageFile() throws IOException {
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
    private void updateViews(boolean newImage) {
        if (currentImage != null) {
            imageView.setBackground(null);
            imageView.setImageBitmap(currentImage);
            isImageLoaded = true;
            if (newImage) {
                isCurrentImageSaved = false;
            }
            invalidateOptionsMenu();
        } else {
            imageView.setBackground(getResources().getDrawable(R.drawable.inset_background));
            isImageLoaded = false;
            isCurrentImageSaved = false;
            invalidateOptionsMenu();
        }
    }
}
