package com.chickenkiller.upods2.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayableTrack;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.interfaces.IUIProgressUpdater;
import com.chickenkiller.upods2.models.Track;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class TracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int itemLayout;

    private List<IPlayableTrack> tracks;
    private IPlayableMediaItem iPlayableMediaItem;
    private Context mContext;
    private IFragmentsManager fragmentsManager;


    private static class ViewHolderTrack extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvSubTitle;
        public TextView tvDate;
        public Button btnDownload;
        private View rootView;
        private CircleProgressView cvDownloadProgress;

        public ViewHolderTrack(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTrackTitle);
            this.tvSubTitle = (TextView) view.findViewById(R.id.tvTrackSubTitle);
            this.tvDate = (TextView) view.findViewById(R.id.tvTrackDate);
            this.btnDownload = (Button) view.findViewById(R.id.btnDownload);
            this.cvDownloadProgress = (CircleProgressView) view.findViewById(R.id.cvDownloadProgress);
            this.rootView = view;
        }

        public void setClickListner(View.OnClickListener clickListner) {
            rootView.setOnClickListener(clickListner);
        }


        public void setDownloadBtnClickListener(View.OnClickListener clickListner) {
            btnDownload.setOnClickListener(clickListner);
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderTrack) {
            final IPlayableTrack currentTrack = tracks.get(position);
            ((ViewHolderTrack) holder).tvTitle.setText(currentTrack.getTitle());
            ((ViewHolderTrack) holder).tvSubTitle.setText(currentTrack.getSubTitle());
            ((ViewHolderTrack) holder).tvDate.setText(currentTrack.getDate());
            ((ViewHolderTrack) holder).setClickListner(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IPlayableTrack iPlayableTrack = tracks.get(position);
                    if (iPlayableTrack instanceof ITrackable && iPlayableTrack instanceof Track) {
                        ((ITrackable) iPlayableTrack).selectTrack((Track) iPlayableTrack);
                    }
                    Intent myIntent = new Intent(mContext, ActivityPlayer.class);
                    myIntent.putExtra(ActivityPlayer.MEDIA_ITEM_EXTRA, iPlayableMediaItem);
                    mContext.startActivity(myIntent);
                    ((Activity) mContext).finish();
                }
            });

            final boolean isDownloaed = ProfileManager.getInstance().isDownloaded(iPlayableMediaItem, currentTrack);
            if (isDownloaed) {
                ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
                ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.GONE);
                ((ViewHolderTrack) holder).btnDownload.setText(mContext.getString(R.string.play));
                //Play audeo
            } else if (DownloadMaster.getInstance().isItemDownloading(currentTrack.getTitle())) {
                ((ViewHolderTrack) holder).btnDownload.setVisibility(View.GONE);
                ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.VISIBLE);
            } else {
                ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.GONE);
                ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
                ((ViewHolderTrack) holder).btnDownload.setText(mContext.getString(R.string.download));
                ((ViewHolderTrack) holder).btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DownloadMaster.DownloadTask downloadTask = new DownloadMaster.DownloadTask();
                        downloadTask.mediaItem = iPlayableMediaItem;
                        downloadTask.track = currentTrack;
                        downloadTask.progressUpdater = new IUIProgressUpdater() {
                            @Override
                            public void updateProgressUI(double progress) {
                                if (((ViewHolderTrack) holder).cvDownloadProgress != null) {
                                    ((ViewHolderTrack) holder).cvDownloadProgress.setValue((float) progress);
                                }
                            }
                        };
                        DownloadMaster.getInstance().download(downloadTask);
                        ((ViewHolderTrack) holder).btnDownload.setVisibility(View.GONE);
                        ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.VISIBLE);
                    }
                });
            }
            holder.itemView.setTag(currentTrack);
        }
    }


    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void addItems(ArrayList<? extends IPlayableTrack> tracks) {
        this.tracks.clear();
        this.tracks.addAll(tracks);
        this.notifyDataSetChanged();
    }

    public void clear() {
        tracks.clear();
    }

}
