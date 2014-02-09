package ca.tannerrutgers.ImageWarp.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ca.tannerrutgers.ImageWarp.R;

/**
 * Created by Tanner on 1/23/14.
 */
public class ImageSelectionDialog extends DialogFragment {

    private static final int LOAD_IMAGE = 0;
    private static final int TAKE_PICTURE = 1;

    /**
     * Interface that hosting activity must implement in order
     * to handle the selection of how to load an image
     */
    public interface ImageSelectionDialogListener {
        public void onLoadImageSelection();
        public void onTakePictureSelection();
    }

    ImageSelectionDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (ImageSelectionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ImageSelectionDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Construct image selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_image_selection)
                .setItems(R.array.image_selection, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // Load image has been selected
                            case LOAD_IMAGE:
                                mListener.onLoadImageSelection();
                                break;
                            // Take picture has been selected
                            case TAKE_PICTURE:
                                mListener.onTakePictureSelection();
                                break;
                        }
                    }
                });
        return builder.create();
    }
}
