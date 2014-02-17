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

package ca.tannerrutgers.Warpy.dialogs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import ca.tannerrutgers.Warpy.R;

/**
 * Preference dialog class using a number picker to choose undo size.
 * Created with help from:
 *      http://www.lukehorvat.com/blog/android-numberpickerdialogpreference/
 */
public class UndoSizePreference extends DialogPreference {

    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 10;
    public static final int DEFAULT_VALUE = 5;

    private int mMinValue;
    private int mMaxValue;
    private int mValue;

    private NumberPicker mNumberPicker;

    public UndoSizePreference(Context context)
    {
        this(context, null);
    }

    public UndoSizePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Get & set attributes specified in XML
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerDialogPreference, 0, 0);
        try
        {
            setMinValue(a.getInteger(R.styleable.NumberPickerDialogPreference_min, DEFAULT_MIN_VALUE));
            setMaxValue(a.getInteger(R.styleable.NumberPickerDialogPreference_android_max, DEFAULT_MAX_VALUE));
        }
        finally
        {
            a.recycle();
        }

        // Set layout
        setDialogLayoutResource(R.layout.preference_number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    /**
     * Set initial value of preference
     */
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue)
    {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    /**
     * Retrieve default value of preference
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return a.getInt(index, DEFAULT_VALUE);
    }

    /**
     * Called on creation of preference dialog
     */
    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        // Set dialog message
        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());

        // Instantiate number picker
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mValue);
    }

    /**
     * Return minimum allowable choice
     */
    public int getMinValue()
    {
        return mMinValue;
    }

    /**
     * Set minimum allowable choice
     */
    public void setMinValue(int minValue)
    {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    /**
     * Return maximum allowbale choice
     */
    public int getMaxValue()
    {
        return mMaxValue;
    }

    /**
     * Set maximum allowable choice
     */
    public void setMaxValue(int maxValue)
    {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    /**
     * Return currently selected value
     */
    public int getValue()
    {
        return mValue;
    }

    /**
     * Set current value to given value
     */
    public void setValue(int value)
    {
        value = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (value != mValue)
        {
            mValue = value;
            persistInt(value);
            setSummary(String.valueOf(value));
            notifyChanged();
        }
    }

    /**
     * Called when the dialog is closed
     */
    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new value
        if (positiveResult)
        {
            int numberPickerValue = mNumberPicker.getValue();
            if (callChangeListener(numberPickerValue))
            {
                setValue(numberPickerValue);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // Save the instance state so that it will survive events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // Set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.minValue = getMinValue();
        myState.maxValue = getMaxValue();
        myState.value = getValue();

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        // Check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class))
        {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the state
        SavedState myState = (SavedState) state;
        setMinValue(myState.minValue);
        setMaxValue(myState.maxValue);
        setValue(myState.value);

        super.onRestoreInstanceState(myState.getSuperState());
    }

    private static class SavedState extends BaseSavedState
    {
        int minValue;
        int maxValue;
        int value;

        public SavedState(Parcelable superState)
        {
            super(superState);
        }

        public SavedState(Parcel source)
        {
            super(source);

            minValue = source.readInt();
            maxValue = source.readInt();
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(minValue);
            dest.writeInt(maxValue);
            dest.writeInt(value);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }
}

