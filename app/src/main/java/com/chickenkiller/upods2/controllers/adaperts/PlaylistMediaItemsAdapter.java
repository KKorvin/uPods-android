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
import com.chickenkiller.upods2.interfaces.INowPlayingItemPosiontGetter;
import com.chickenkiller.upods2.models.MediaItem;

import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class PlaylistMediaItemsAdapter extends ArrayAdapter<MediaItem> implements INowPlayingItemPosiontGetter {

    private int layaoutId;
    private UniversalPlayer universalPlayer;
    private List<? extends MediaItem> mediaItems;


    private static class ViewHolder {
        public ImageButton btnPlPlay;
        public TextView tvPlTrackTitle;
        public TextView tvPlTrackSubTitle;
        public TextView tvPlTrackDuration;
        public EqualizerView eqRadio;
    }

    public PlaylistMediaItemsAdapter(Context context, int layaoutId, List<MediaItem> mediaItems) {
        super(context, layaoutId, mediaItems);
        this.layaoutId = layaoutId;
        this.universalPlayer = UniversalPlayer.getInstance();
        this.mediaItems = mediaItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MediaItem mediaItem = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layaoutId, parent, false);
            viewHolder.btnPlPlay = (ImageButton) convertView.findViewById(R.id.btnPlPlay);
            viewHolder.tvPlTrackTitle = (TextView) convertView.findViewById(R.id.tvPlTrackTitle);
            viewHolder.tvPlTrackSubTitle = (TextView) convertView.findViewById(R.id.tvPlTrackSubTitle);
            viewHolder.tvPlTrackDuration = (TextView) convertView.findViewById(R.id.tvPlTrackDuration);
            viewHolder.eqRadio = (EqualizerView) convertView.findViewById(R.id.eqRadio);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvPlTrackTitle.setText(mediaItem.getName());
        viewHolder.tvPlTrackSubTitle.setText(mediaItem.getSubHeader());
        viewHolder.eqRadio.setVisibility(View.VISIBLE);
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentMediaItem(mediaItem)) {
            viewHolder.eqRadio.setVisibility(View.VISIBLE);
            viewHolder.eqRadio.animateBars();
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_pause_white);
        } else {
            viewHolder.eqRadio.setVisibility(View.GONE);
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_play_white);
        }
        return convertView;
    }

    @Override
    public int getNowPlayingItemPosition() {
        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem iPlayableMediaItem = mediaItems.get(i);
            if (iPlayableMediaItem.getName().equals(universalPlayer.getPlayingMediaItem().getName())) {
                return i;
            }
        }
        return 0;
    }

}
