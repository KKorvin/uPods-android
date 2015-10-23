package com.chickenkiller.upods2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class DialogFragmentConfarmation extends DialogFragment {

    public static final String TAG = "df_track_info";
    private String message;
    private String title;
    private View.OnClickListener positiveClickListener;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        positiveClickListener.onClick(null);
                    }
                }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPositiveClickListener(View.OnClickListener positiveClickListener) {
        this.positiveClickListener = positiveClickListener;
    }
}
