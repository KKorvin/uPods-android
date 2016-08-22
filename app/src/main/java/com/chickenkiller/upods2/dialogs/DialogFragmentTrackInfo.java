package com.chickenkiller.upods2.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.Track;

/**
 * Created by Alon Zilberman on 8/8/15.
 */
public class DialogFragmentTrackInfo extends DialogFragment {

    public static final String TAG = "df_track_info";
    public boolean enableStream = true;

    private View.OnClickListener streamClickListener;
    private Track track;


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainView = inflater.inflate(R.layout.dialog_fragment_track_info, null);
        WebView wbSummary = (WebView) mainView.findViewById(R.id.wbTrackInfo);
        wbSummary.getSettings().setJavaScriptEnabled(true);
        wbSummary.loadDataWithBaseURL(
                "file://" + Environment.getExternalStorageDirectory(),
                track.getInfo(), "text/html", "utf-8", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(track.getTitle())
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        if (enableStream) {
            builder.setNegativeButton(R.string.stream,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            streamClickListener.onClick(null);
                        }
                    }).setView(mainView);
        }
        return builder.create();
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public void setStreamClickListener(View.OnClickListener streamClickListener) {
        this.streamClickListener = streamClickListener;
    }
}
