package com.chickenkiller.upods2.controllers.adaperts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.player.Playlist;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.INowPlayingItemPosiontGetter;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;

import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class PlaylistTracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements INowPlayingItemPosiontGetter {

    private int layaoutId;
    private UniversalPlayer universalPlayer;
    private List<Track> tracks;
    private Playlist playlist;

    private static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public ImageButton btnPlPlay;
        public TextView tvPlTrackTitle;
        public TextView tvPlTrackSubTitle;
        public TextView tvPlTrackDuration;
        public RelativeLayout rlPlaylistItem;

        public PlaylistViewHolder(View view) {
            super(view);
            btnPlPlay = (ImageButton) view.findViewById(R.id.btnPlPlay);
            tvPlTrackTitle = (TextView) view.findViewById(R.id.tvPlTrackTitle);
            tvPlTrackSubTitle = (TextView) view.findViewById(R.id.tvPlTrackSubTitle);
            tvPlTrackDuration = (TextView) view.findViewById(R.id.tvPlTrackDuration);
            rlPlaylistItem = (RelativeLayout) view.findViewById(R.id.rlPlaylistItem);
        }

    }

    public PlaylistTracksAdapter(Playlist playlist, int layaoutId, List<Track> tracks) {
        super();
        this.playlist = playlist;
        this.layaoutId = layaoutId;
        this.tracks = tracks;
        this.universalPlayer = UniversalPlayer.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layaoutId, parent, false);
        PlaylistViewHolder viewHolder = new PlaylistViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PlaylistViewHolder viewHolder = (PlaylistViewHolder) holder;
        Track track = tracks.get(position);
        viewHolder.tvPlTrackTitle.setText(track.getTitle());
        viewHolder.tvPlTrackSubTitle.setText(track.getSubTitle());
        viewHolder.tvPlTrackDuration.setText(track.getDuration());
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentTrack(track)) {
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_pause_white);
        } else {
            viewHolder.btnPlPlay.setImageResource(R.drawable.ic_play_white);
        }
        viewHolder.rlPlaylistItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist.changeTrack(position);
            }
        });
        holder.itemView.setTag(track);
    }


    @Override
    public int getNowPlayingItemPosition() {
        Track currentTrack = ((Podcast) universalPlayer.getPlayingMediaItem()).getSelectedTrack();
        for (int i = 0; i < tracks.size(); i++) {
            if (currentTrack.getTitle().equals(tracks.get(i).getTitle())) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public Track getItem(int position) {
        return tracks.get(position);
    }

}
