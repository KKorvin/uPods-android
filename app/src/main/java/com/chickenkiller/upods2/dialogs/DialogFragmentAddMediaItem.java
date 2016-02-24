package com.chickenkiller.upods2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.StreamUrl;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class DialogFragmentAddMediaItem extends DialogFragment {

    public static final String TAG = "add_media_item";
    private static final int MIN_TITLE_LENGTH = 3;

    private MediaItemType mediaItemType;
    private EditText etMediaName;
    private EditText etMediaUrl;


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainView = inflater.inflate(R.layout.dialog_fragment_add_media, null);
        etMediaName = (EditText) mainView.findViewById(R.id.etMediaName);
        etMediaUrl = (EditText) mainView.findViewById(R.id.etMediaUrl);

        String title = "LOL123";
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
                                if (saveMediaItem()) {
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).setView(mainView);
        return builder.create();
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }


    private boolean saveMediaItem() {
        if (etMediaName.getText().toString().length() < MIN_TITLE_LENGTH) {
            Toast.makeText(getActivity(), getString(R.string.title_too_short), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!URLUtil.isValidUrl(etMediaUrl.getText().toString())) {
            Toast.makeText(getActivity(), getString(R.string.url_not_correct), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mediaItemType == MediaItemType.RADIO) {
            StreamUrl streamUrl = new StreamUrl(etMediaUrl.getText().toString());
            RadioItem radioItem = new RadioItem(etMediaName.getText().toString(), streamUrl, "");
            ProfileManager.getInstance().addSubscribedMediaItem(radioItem);
            Toast.makeText(getActivity(), getString(R.string.radio_added), Toast.LENGTH_SHORT).show();
        } else if (mediaItemType == MediaItemType.PODCAST) {
            Podcast podcast = new Podcast(etMediaName.getText().toString(), etMediaUrl.getText().toString());
            ProfileManager.getInstance().addSubscribedMediaItem(podcast);
            Toast.makeText(getActivity(), getString(R.string.podcast_added), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
