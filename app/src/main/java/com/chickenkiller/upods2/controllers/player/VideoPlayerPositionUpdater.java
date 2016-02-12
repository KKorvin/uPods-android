package com.chickenkiller.upods2.controllers.player;

import android.util.Pair;

import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.interfaces.ITrackable;
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
                            universalPlayer.getPlayingMediaItem() instanceof ITrackable && position % SAVE_POSITION_RATE == 0) {
                        Pair<String, String> trackPosition = new Pair<>(((ITrackable) universalPlayer.getPlayingMediaItem()).getSelectedTrack().getTitle(),
                                String.valueOf(position));
                        SettingsManager.getInstace().putSettingsValue(SettingsManager.JS_EPISODS_POSITIONS, trackPosition);
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
