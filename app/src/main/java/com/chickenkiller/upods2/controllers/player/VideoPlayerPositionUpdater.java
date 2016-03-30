package com.chickenkiller.upods2.controllers.player;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Logger;

import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Created by alonzilberman on 11/24/15.
 */
public class VideoPlayerPositionUpdater extends PlayerPositionUpdater {

    public static final float TIME_CONVERTER_FACTOR = 1000;
    private WeakReference<MediaPlayer> mediaPlayer;

    public VideoPlayerPositionUpdater(IOnPositionUpdatedCallback positionUpdatedCallback, MediaPlayer mPlayer) {
        this(positionUpdatedCallback);
        this.mediaPlayer = new WeakReference<MediaPlayer>(mPlayer);
    }

    public VideoPlayerPositionUpdater(IOnPositionUpdatedCallback positionUpdatedCallback) {
        super(positionUpdatedCallback);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (!isCancelled() && positionUpdatedCallback != null) {
                if (mediaPlayer.get() != null) {
                    int position = (int) (mediaPlayer.get().getPosition() * TIME_CONVERTER_FACTOR);
                    position = (int) TimeUnit.SECONDS.toMillis(position);

                    if (position > 0) {
                        publishProgress(position);
                    }
                    if (universalPlayer.getPlayingMediaItem() != null && position > 0 &&
                            universalPlayer.getPlayingMediaItem() instanceof Podcast && position % SAVE_POSITION_RATE == 0) {
                        Track track = ((Podcast) universalPlayer.getPlayingMediaItem()).getSelectedTrack();
                        ProfileManager.getInstance().saveTrackPosition(track, position);
                    }

                    Thread.sleep(POSITION_UPDATE_RATE);
                }
            }
        } catch (Exception e) {
            Logger.printError("PlayerPositionUpdater", "Failed to update progress:");
            e.printStackTrace();
        }
        return null;
    }
}
