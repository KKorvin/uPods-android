package com.chickenkiller.upods2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;

/**
 * Created by Alon Zilberman on 8/8/15.
 */
public class DialogFragmentMessage extends DialogFragment {

    public static final String TAG = "df_simple_message";
    private IOperationFinishCallback onOkClicked;
    private String message;
    private String title;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (onOkClicked != null) {
                            onOkClicked.operationFinished();
                        }
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

    public void setOnOkClicked(IOperationFinishCallback onOkClicked) {
        this.onOkClicked = onOkClicked;
    }
}
