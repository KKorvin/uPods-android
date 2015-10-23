package com.chickenkiller.upods2.controllers.adaperts;

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
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.internet.DownloadMaster;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.dialogs.DialogFragmentTrackInfo;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.interfaces.IUIProgressUpdater;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.ContextMenuType;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class TracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int itemLayout;

    private List<Track> tracks;
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

        public void setLongClickListner(View.OnLongClickListener clickListner) {
            rootView.setOnLongClickListener(clickListner);
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
            final Track currentTrack = tracks.get(position);
            ((ViewHolderTrack) holder).tvTitle.setText(currentTrack.getTitle());
            ((ViewHolderTrack) holder).tvSubTitle.setText(currentTrack.getSubTitle());
            ((ViewHolderTrack) holder).tvDate.setText(currentTrack.getDate());
            ((ViewHolderTrack) holder).setClickListner(getShowInfoClick(fragmentsManager, currentTrack, position));
            ((ViewHolderTrack) holder).setLongClickListner(getTrackLongClickListener(currentTrack));
            initDownloadBtn(holder, currentTrack, position);
            holder.itemView.setTag(currentTrack);
        }
    }

    /**
     * Handles logic of download button and progress bar. Including all click listener and progress updates.
     *
     * @param holder
     * @param currentTrack
     * @param position
     */
    private void initDownloadBtn(final RecyclerView.ViewHolder holder, final Track currentTrack, final int position) {
        final boolean isDownloaed = ProfileManager.getInstance().isDownloaded(iPlayableMediaItem, currentTrack);
        if (isDownloaed) {//Already downloaded item
            ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
            ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.INVISIBLE);
            ((ViewHolderTrack) holder).btnDownload.setText(mContext.getString(R.string.play));
            ((ViewHolderTrack) holder).btnDownload.setOnClickListener(getPlayClickListener(position));
            //Play audeo
        } else if (DownloadMaster.getInstance().isItemDownloading(currentTrack.getTitle())) {//still in downloading progress
            ((ViewHolderTrack) holder).btnDownload.setVisibility(View.INVISIBLE);
            ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.VISIBLE);
            DownloadMaster.DownloadTask task = DownloadMaster.getInstance().getTaskByName(currentTrack.getTitle());
            if (task != null) {
                ((ViewHolderTrack) holder).cvDownloadProgress.setValue((float) task.lastProgress);
                task.progressUpdater = getDownloadProgressUpdater(holder);
                task.contentLoadListener = getContentLoadListener(holder, position);
            }
        } else {//normal state
            ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.INVISIBLE);
            ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
            ((ViewHolderTrack) holder).btnDownload.setText(mContext.getString(R.string.download));
            ((ViewHolderTrack) holder).btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadMaster.DownloadTask downloadTask = new DownloadMaster.DownloadTask();
                    downloadTask.mediaItem = iPlayableMediaItem;
                    downloadTask.track = currentTrack;
                    downloadTask.progressUpdater = getDownloadProgressUpdater(holder);
                    downloadTask.contentLoadListener = getContentLoadListener(holder, position);
                    DownloadMaster.getInstance().download(downloadTask);
                    ((ViewHolderTrack) holder).btnDownload.setVisibility(View.INVISIBLE);
                    ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.VISIBLE);
                }
            });
        }
        ((ViewHolderTrack) holder).cvDownloadProgress.setOnClickListener(getDownloadCancellListener(holder, currentTrack));
    }

    private IUIProgressUpdater getDownloadProgressUpdater(final RecyclerView.ViewHolder holder) {
        return new IUIProgressUpdater() {
            @Override
            public void updateProgressUI(double progress) {
                if (((ViewHolderTrack) holder).cvDownloadProgress != null) {
                    ((ViewHolderTrack) holder).cvDownloadProgress.setValue((float) progress);
                }
            }
        };
    }

    private IContentLoadListener getContentLoadListener(final RecyclerView.ViewHolder holder, final int position) {
        return new IContentLoadListener() {
            @Override
            public void onContentLoaded() {
                ((ViewHolderTrack) holder).btnDownload.setText(mContext.getString(R.string.play));
                ((ViewHolderTrack) holder).btnDownload.setOnClickListener(getPlayClickListener(position));
                ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
                ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.GONE);
            }
        };
    }

    private View.OnClickListener getPlayClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadMaster.getInstance().cleanProgressInterfaces();
                Track track = tracks.get(position);
                if (iPlayableMediaItem instanceof ITrackable) {
                    ((ITrackable) iPlayableMediaItem).selectTrack(track);
                }
                Intent myIntent = new Intent(mContext, ActivityPlayer.class);
                UniversalPlayer.getInstance().setMediaItem(iPlayableMediaItem);
                mContext.startActivity(myIntent);
                ((Activity) mContext).finish();
            }
        };
    }

    private View.OnClickListener getDownloadCancellListener(final RecyclerView.ViewHolder holder, final Track currentTrack) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadMaster.DownloadTask task = DownloadMaster.getInstance().getTaskByName(currentTrack.getTitle());
                if (task != null) {
                    DownloadMaster.getInstance().cancelDownload(currentTrack.getTitle());
                    ((ViewHolderTrack) holder).btnDownload.setVisibility(View.VISIBLE);
                    ((ViewHolderTrack) holder).cvDownloadProgress.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    private View.OnClickListener getShowInfoClick(final IFragmentsManager fragmentsManager, final Track track, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragmentTrackInfo dialogFragmentTrackInfo = new DialogFragmentTrackInfo();
                dialogFragmentTrackInfo.setTrack(track);
                dialogFragmentTrackInfo.setStreamClickListener(getPlayClickListener(position));
                fragmentsManager.showDialogFragment(dialogFragmentTrackInfo);
            }
        };
    }

    private View.OnLongClickListener getTrackLongClickListener(final Track currentTrack) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                IOperationFinishCallback operationFinishCallback = new IOperationFinishCallback() {
                    @Override
                    public void operationFinished() {
                        notifyDataSetChanged();
                    }
                };
                MediaItem.MediaItemBucket bucket = new MediaItem.MediaItemBucket();
                bucket.mediaItem = (MediaItem) iPlayableMediaItem;
                bucket.track = (Track) currentTrack;
                ((IContextMenuManager) mContext).openContextMenu(v, ContextMenuType.EPISOD_MIDDLE_SCREEN,
                        bucket, operationFinishCallback);
                return true;
            }
        };
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void addItems(ArrayList<? extends Track> tracks) {
        this.tracks.clear();
        this.tracks.addAll(tracks);
        this.notifyDataSetChanged();
    }

    public void clear() {
        tracks.clear();
    }

}
