package com.chickenkiller.upods2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class DialogFragmentAddMediaItem extends DialogFragment {

    public static final String TAG = "add_media_item";
    private MediaItemType mediaItemType;


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainView = inflater.inflate(R.layout.dialog_fragment_add_media, null);

        String title = "";
        if (mediaItemType == MediaItemType.RADIO) {
            title = getString(R.string.add_radio_station);
        } else if (mediaItemType == MediaItemType.PODCAST) {
            title = getString(R.string.add_podcast);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).setView(mainView);
        return builder.create();
    }

    public void setMediaItemType(MediaItemType type) {
        this.mediaItemType = mediaItemType;
    }

}
