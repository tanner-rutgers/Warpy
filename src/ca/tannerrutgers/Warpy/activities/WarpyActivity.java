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

package ca.tannerrutgers.Warpy.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import ca.tannerrutgers.Warpy.R;
import ca.tannerrutgers.Warpy.dialogs.DiscardImageWarningDialog;
import ca.tannerrutgers.Warpy.dialogs.UndoSizePreference;
import ca.tannerrutgers.Warpy.listeners.WarpGestureListener;
import ca.tannerrutgers.Warpy.tasks.SaveImageTask;
import ca.tannerrutgers.Warpy.utils.WarpActionStack;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WarpyActivity extends Activity implements DiscardImageWarningDialog.DiscardImageWarningDialogListener {

    public static final String APP_TAG = "Warpy";

    // Request types
    private static final int REQUEST_LOAD_IMAGE = 0;
    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_BACK_PRESSED = 2;

    // App behaviour types
    private static final int ACTION_EDIT = 3;
    private static final int ACTION_DEFAULT = 4;

    // Current state variables
    private int mAppBehaviour;
    private Uri mCurrentImageUri;
    private boolean mIsCurrentImageSaved;
    private Bitmap mCurrentBitmap;
    private WarpActionStack<Uri> mActionStack;

    // View items
    private ImageView mImageView;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set default values if first time launching
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Initialize views
        mImageView = (ImageView)findViewById(R.id.imageView);
        WarpGestureListener listener = new WarpGestureListener(this);
        mGestureDetector = new GestureDetector(this, listener);
        mScaleGestureDetector = new ScaleGestureDetector(this, listener);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                if (mCurrentBitmap != null) {
                    boolean returnVal = mScaleGestureDetector.onTouchEvent(me);
                    return mGestureDetector.onTouchEvent(me) || returnVal;
                }
                return false;
            }
        });

        // Initialize other state variables
        mIsCurrentImageSaved = false;

        // Determine behaviour of app (how it was launched)
        determineBehaviour();
    }

    /**
     * Determines behaviour of app based on how it was started
     */
    private void determineBehaviour() {
        // Get intent that started activity
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // Handle possibilities of how app was launched
        if ((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) && type != null) {
            // App was launched to view/edit an image
            if (type.startsWith("image/")) {
                Uri imageUri = intent.getData();
                if (imageUri != null) {
                    mAppBehaviour = ACTION_EDIT;
                    setBitmap(getImageFromUri(imageUri), true);
                }
            }
        } else {
            mAppBehaviour = ACTION_DEFAULT;
        }
    }

    /**
     * Called when the activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Set/reset undo size based on preferences
        int undoSize = PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingsActivity.KEY_PREF_UNDO_SIZE, UndoSizePreference.DEFAULT_VALUE);
        if (mActionStack == null) {
            mActionStack = new WarpActionStack<Uri>(this, undoSize);
        } else if (undoSize != mActionStack.capacity()){
            mActionStack.resize(undoSize);
        }
    }

    /**
     * Called when activity is started
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Called when the activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static final String STATE_IMAGE = "currentImage";
    private static final String STATE_STACK_SIZE = "actionStackSize";
    private static final String STATE_STACK = "actionStack";
    private static final String STATE_SAVED = "isImageSaved";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the app's current state
        savedInstanceState.putParcelable(STATE_IMAGE, mCurrentBitmap);
        savedInstanceState.putInt(STATE_STACK_SIZE, mActionStack.size());
        for (int i = 0; i < mActionStack.size(); i++) {
            savedInstanceState.putString(STATE_STACK + i, mActionStack.get(i).toString());
        }
        savedInstanceState.putBoolean(STATE_SAVED, mIsCurrentImageSaved);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        mCurrentBitmap = savedInstanceState.getParcelable(STATE_IMAGE);
        int stackSize = savedInstanceState.getInt(STATE_STACK_SIZE);
        for (int i = 0; i < stackSize; i++) {
            mActionStack.push(Uri.parse(savedInstanceState.getString(STATE_STACK + i)));
        }
        mIsCurrentImageSaved = savedInstanceState.getBoolean(STATE_SAVED);

        updateViews();
    }

    /**
     * Called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (mCurrentBitmap != null && !mIsCurrentImageSaved) {
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

        // Set menu items based on how activity was started

        if (mAppBehaviour == ACTION_EDIT) {  // Editing/viewing provided image
            // Hide load image menu item
            MenuItem loadImage = menu.findItem(R.id.menu_load_image);
            if (loadImage != null) {
                loadImage.setVisible(false);
            }
            // Hide take picture menu item
            MenuItem takePicture = menu.findItem(R.id.menu_take_picture);
            if (takePicture != null) {
                takePicture.setVisible(false);
            }
        }

        // Set status of save menu item
        MenuItem save = menu.findItem(R.id.menu_save_image);
        if (save != null) {
            boolean enabled = mCurrentBitmap != null && !mIsCurrentImageSaved;
            save.setEnabled(enabled);
            if (enabled) {
                save.getIcon().setAlpha(255);
            } else {
                save.getIcon().setAlpha(130);
            }
        }

        // Set status of undo item
        MenuItem undo = menu.findItem(R.id.menu_undo);
        if (undo != null) {
            boolean enabled = !mActionStack.isEmpty();
            undo.setEnabled(enabled);
            if (enabled) {
                undo.getIcon().setAlpha(255);
            } else {
                undo.getIcon().setAlpha(130);
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
            // Save image has been selected
            case R.id.menu_save_image:
                saveCurrentImage();
                return true;
            // Undo button has been selected
            case R.id.menu_undo:
                undoSelected();
                return true;
            // Settings has been selected
            case R.id.menu_settings:
                launchPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Returns a copy of the current Bitmap
     */
    public Bitmap getCurrentBitmapCopy() {
        return mCurrentBitmap.copy(mCurrentBitmap.getConfig(),true);
    }

    /**
     * Set currently displayed bitmap
     */
    public void setBitmap(Bitmap image, boolean isNew) {
        if (isNew && mActionStack != null) {
            mActionStack.clear();   // Clear action stack if new image
        }
        mCurrentBitmap = image;
        updateViews();
    }

    /**
     * Called when take picture is selected from menu
     */
    private void takePictureSelected() {
        if (mCurrentBitmap != null && !mIsCurrentImageSaved) {
            showDiscardImageWarningDialog(REQUEST_TAKE_PICTURE);
        } else {
            takePicture();
        }
    }

    /**
     * Called when load image is selected from menu
     */
    private void loadImageSelected() {
        if (mCurrentBitmap != null && !mIsCurrentImageSaved) {
            showDiscardImageWarningDialog(REQUEST_LOAD_IMAGE);
        } else {
            loadImage();
        }
    }

    /**
     * Called when undo is selected from menu
     */
    private void undoSelected() {
        mCurrentBitmap = getImageFromUri(mActionStack.pop());
        updateViews();
    }

    /**
     * Push new Uri onto action stack using Bitmap
     */
    public void pushToActionStack(Bitmap image) {
        mActionStack.pushFromBitmap(image);
    }

    /**
     * Pop most recent Uri from action stack
     */
    public Uri popFromActionStack() {
        return mActionStack.pop();
    }

    /**
     * Save currently loaded/warped image to external storage
     */
    private void saveCurrentImage() {
        if (mCurrentBitmap != null) {
            SaveImageTask saveTask = new SaveImageTask(this);
            saveTask.execute(mCurrentBitmap);
            mIsCurrentImageSaved = true;
            invalidateOptionsMenu();
        }
    }


    /**
     * Called when settings is selected from menu
     */
    private void launchPreferences() {
        Intent preferencesIntent = new Intent(WarpyActivity.this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }


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
                    setBitmap(getImageFromUri(returnedIntent.getData()), true);
                }
                break;
            // Request is image from camera
            case REQUEST_TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    // Retrieve taken picture and update views
                    setBitmap(getImageFromUri(mCurrentImageUri), true);
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
                mCurrentImageUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImageUri);
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
     * Update views belonging to this activity
     */
    private void updateViews() {
        if (mCurrentBitmap != null) {
            mImageView.setBackground(null);
            mImageView.setImageBitmap(mCurrentBitmap);
            mIsCurrentImageSaved = false;
        } else {
            mImageView.setBackground(getResources().getDrawable(R.drawable.inset_background));
            mIsCurrentImageSaved = false;
        }
        invalidateOptionsMenu();
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
}
