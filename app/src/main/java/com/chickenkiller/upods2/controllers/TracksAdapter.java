package com.chickenkiller.upods2.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayableTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class TracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int itemLayout;

    private List<IPlayableTrack> tracks;
    private IPlayableMediaItem iPlayableMediaItem;
    private Context mContext;
    private IFragmentsManager fragmentsManager;


    private class ViewHolderTrack extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvTitle;
        public TextView tvSubTitle;

        public ViewHolderTrack(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTrackTitle);
            this.tvSubTitle = (TextView) view.findViewById(R.id.tvTrackSubTitle);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            IPlayableTrack iPlayableTrack = tracks.get(getAdapterPosition());
            Intent myIntent = new Intent(mContext, ActivityPlayer.class);
            myIntent.putExtra(ActivityPlayer.MEDIA_ITEM_EXTRA, iPlayableMediaItem);
            mContext.startActivity(myIntent);
            ((Activity) mContext).finish();
        }

    }


    public TracksAdapter(IPlayableMediaItem iPlayableMediaItem, Context mContext, int itemLayout) {
        super();
        this.iPlayableMediaItem = iPlayableMediaItem;
        this.tracks = new ArrayList<>();
        this.itemLayout = itemLayout;
        this.mContext = mContext;
    }


    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        RecyclerView.ViewHolder viewHolder = new ViewHolderTrack(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderTrack) {
            IPlayableTrack currentTrack = tracks.get(position);
            ((ViewHolderTrack) holder).tvTitle.setText(currentTrack.getTitle());
            ((ViewHolderTrack) holder).tvSubTitle.setText(currentTrack.getSubTitle());
            holder.itemView.setTag(currentTrack);
        }
    }


    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void addItems(ArrayList<? extends IPlayableTrack> tracks) {
        this.tracks.addAll(tracks);
        this.notifyDataSetChanged();
    }

    public void clear() {
        tracks.clear();
    }

}
