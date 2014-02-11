package ca.tannerrutgers.ImageWarp.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import ca.tannerrutgers.ImageWarp.R;

/**
 * Created by Tanner on 2/9/14.
 */
public class DiscardImageWarningDialog extends DialogFragment {

    /**
     * Interface that hosting activity must implement in order
     * to handle the selection of how to load an image
     */
    public interface DiscardImageWarningDialogListener {
        public void onDiscardImageSelection(int requestType);
    }

    private int requestType;

    public DiscardImageWarningDialog(int requestType) {
        this.requestType = requestType;
    }

    DiscardImageWarningDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            mListener = (DiscardImageWarningDialogListener) activity;
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
        builder.setMessage(R.string.dialog_discard_image)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDiscardImageSelection(requestType);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DiscardImageWarningDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
