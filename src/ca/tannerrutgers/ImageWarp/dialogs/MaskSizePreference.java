package ca.tannerrutgers.ImageWarp.dialogs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import ca.tannerrutgers.ImageWarp.R;
import ca.tannerrutgers.ImageWarp.models.ImageFilter;

/**
 * Created by Tanner on 1/23/14.
 */
public class MaskSizePreference extends DialogPreference {

    private static final int SIZE_DEFAULT = ImageFilter.SIZE_DEFAULT;
    public static final int SIZE_DEFAULT_MAX = 999;

    private SeekBar sizeBar;
    private TextView sizeLabel;

    private int mSelectedSize;
    private int mMaxSize;

    public MaskSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_mask_size);
        setDialogTitle(R.string.mask_size);
        setDialogIcon(R.drawable.ic_filter_size);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    /**
     * Set max size allowable for mask size/seek bar
     */
    public void setMaxSize(int maxSize) {
        this.mMaxSize = getValidSize(maxSize);
    }

    /**
     * Set size label to currently selected value, formatted
     */
    private void setSizeLabel() {
        if (sizeLabel != null) {
            sizeLabel.setText(mSelectedSize + " x " + mSelectedSize);
        }
    }

    /**
     * Transform the passed maskSize into a valid mask size and return
     * (Odd, and within min and max bounds)
     */
    private int getValidSize(int maskSize) {
        int validSize = maskSize;

        if (validSize < ImageFilter.SIZE_MIN) {
            validSize = ImageFilter.SIZE_MIN;
        } else if (validSize > mMaxSize && mMaxSize > 0) {
            validSize = mMaxSize;
        }

        if (validSize % 2 == 0 && validSize < mMaxSize) {
            validSize += 1;
        }

        return validSize;
    }

    /**
     * Called when dialog is displayed
     */
    @Override
    public void onBindDialogView(View view) {
        // Retrieve dialog views
        sizeBar = (SeekBar) view.findViewById(R.id.filterSizeBar);
        sizeLabel = (TextView) view.findViewById(R.id.filterSizeLabel);

        // Set values for dialog views
        mMaxSize = getValidSize(mMaxSize);
        sizeBar.setMax(mMaxSize);
        mSelectedSize = getValidSize(mSelectedSize);
        sizeBar.setProgress(mSelectedSize);
        setSizeLabel();

        // Setup listener for seek bar
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSelectedSize = getValidSize(sizeBar.getProgress());
                setSizeLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Called when preference dialog is first launched
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {    // Set size from persisted value
            mSelectedSize = getValidSize(this.getPersistedInt(SIZE_DEFAULT));
        } else {                        // Set size from default value
            mSelectedSize = getValidSize((Integer)defaultValue);
        }
    }

    /**
     * Called when retrieving default value for preference
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return SIZE_DEFAULT;
    }

    /**
     * Called when dialog is closed
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // If OK was pressed, save selected mask size
        if (positiveResult) {
            persistInt(mSelectedSize);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent, use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current setting value
        myState.value = mSelectedSize;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        mSelectedSize = myState.value;
        setSizeLabel();
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}

