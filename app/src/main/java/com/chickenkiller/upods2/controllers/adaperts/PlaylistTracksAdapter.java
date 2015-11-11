package com.chickenkiller.upods2.controllers.adaperts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.models.Track;

import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class PlaylistTracksAdapter extends ArrayAdapter<Track> {

    private int layaoutId;
    private UniversalPlayer universalPlayer;

    private static class ViewHolder {
        public ImageButton btnPlPlay;
        public TextView tvPlTrackTitle;
        public TextView tvPlTrackSubTitle;
        public TextView tvPlTrackDuration;
    }

    public PlaylistTracksAdapter(Context context, int layaoutId, List<Track> categories) {
        super(context, layaoutId, categories);
        this.layaoutId = layaoutId;
        this.universalPlayer = UniversalPlayer.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Track track = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layaoutId, parent, false);
            viewHolder.btnPlPlay = (ImageButton) convertView.findViewById(R.id.btnPlPlay);
            viewHolder.tvPlTrackTitle = (TextView) convertView.findViewById(R.id.tvPlTrackTitle);
            viewHolder.tvPlTrackSubTitle = (TextView) convertView.findViewById(R.id.tvPlTrackSubTitle);
            viewHolder.tvPlTrackDuration = (TextView) convertView.findViewById(R.id.tvPlTrackDuration);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvPlTrackDuration.setText(track.getDuration());
        viewHolder.tvPlTrackTitle.setText(track.getTitle());
        viewHolder.tvPlTrackSubTitle.setText(track.getSubTitle());
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentTrack(track)) {
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_pause_white);
        } else {
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_play_white);
        }
        return convertView;
    }
}
