package com.chickenkiller.upods2.controllers.player;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.webkit.URLUtil;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.utils.enums.Direction;
import com.chickenkiller.upods2.views.DefaultNotificationPanel;
import com.chickenkiller.upods2.views.PlayerNotificationPanel;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.EventListener {


    private static final long RECONNECT_RATE = 5000;

    public enum State {PLAYING, PAUSED}

    public static final String INTENT_ACTION_PLAY = "com.chickenkiller.upods2.player.PLAY";
    public static final String INTENT_ACTION_PAUSE = "com.chickenkiller.upods2.player.PAUSE";
    public static final String INTENT_ACTION_FORWARD = "com.chickenkiller.upods2.player.FORWARD";
    public static final String INTENT_ACTION_BACKWARD = "com.chickenkiller.upods2.player.BACKWARD";
    private static final String PLAYER_LOG = "UniversalPlayer";

    private static UniversalPlayer universalPlayer;
    private LibVLC mLibVLC = null;
    private MediaPlayer mediaPlayer = null;


    private IOperationFinishCallback onAutonomicTrackChangeCallback;
    private IOperationFinishCallback onPlayingFailedCallback;
    private IPlayerStateListener playerStateListener;

    private IPlayableMediaItem mediaItem;
    private PlayerNotificationPanel notificationPanel;

    private TimerTask autoReconector;

    public boolean isLinkReadyForPlaying;
    public boolean isPrepaired;

    private UniversalPlayer() {
    }

    public static UniversalPlayer getInstance() {
        if (universalPlayer == null) {
            universalPlayer = new UniversalPlayer();
        }
        return universalPlayer;
    }

    /**
     * Checks if given type of media item is supported, if yes copies it and save instance in player.
     *
     * @param mediaItem
     */
    public void setMediaItem(IPlayableMediaItem mediaItem) {
        if (isCurrentMediaItem(mediaItem)) {
            return;
        }
        if (mediaItem instanceof RadioItem) {
            this.mediaItem = new RadioItem((RadioItem) mediaItem);
        } else if (mediaItem instanceof Podcast) {
            this.mediaItem = new Podcast((Podcast) mediaItem);
        } else {
            throw new RuntimeException("Unsupported type of MediaItem");
        }
    }

    public void setOnPlayingFailedCallback(IOperationFinishCallback onPlayingFailedCallback) {
        this.onPlayingFailedCallback = onPlayingFailedCallback;
    }

    public void setPlayerStateListener(IPlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public void setOnAutonomicTrackChangeCallback(IOperationFinishCallback onAutonomicTrackChangeCallback) {
        this.onAutonomicTrackChangeCallback = onAutonomicTrackChangeCallback;
    }

    /**
     * Main prepare method. Prepares media item to be played.
     */
    public void prepare() {
        if (mediaItem == null) {
            throw new RuntimeException("MediaItem is not set. Call setMediaItem before prepare.");
        }

        try {
            if (!isLinkReadyForPlaying && !prepareLinkForPlaying()) {
                return;
            }

            String audeoLink = mediaItem.getAudeoLink();

            Logger.printInfo(PLAYER_LOG, "Trying to play: " + audeoLink);
            if (mLibVLC == null) {
                mLibVLC = new LibVLC();
            }
            if (mediaPlayer == null || mediaPlayer.isReleased()) {
                mediaPlayer = new MediaPlayer(mLibVLC);
                mediaPlayer.setEventListener(this);
            }

            isPrepaired = false;

            if (mediaItem instanceof RadioItem || URLUtil.isValidUrl(audeoLink)) {
                Media m = new Media(mLibVLC, Uri.parse(audeoLink));
                mediaPlayer.setMedia(m);
                mediaPlayer.play();
                Logger.printInfo(PLAYER_LOG, "Play called for URL");
            } else {
                Media m = new Media(mLibVLC, audeoLink);
                mediaPlayer.setMedia(m);
                mediaPlayer.play();
                isPrepaired = true;
                Logger.printInfo(PLAYER_LOG, "Play called for local file");
            }

            Handler mainHandler = new Handler(UpodsApplication.getContext().getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaItem instanceof RadioItem) {
                        ProfileManager.getInstance().addRecentMediaItem(mediaItem);
                    }

                    if (notificationPanel != null) {
                        notificationPanel.notificationCancel();
                    }
                    notificationPanel = new DefaultNotificationPanel(UpodsApplication.getContext(), mediaItem);
                }
            };
            mainHandler.post(myRunnable);

        } catch (Exception e) {
            Logger.printInfo(PLAYER_LOG, "Failed to call play...");
            if (onPlayingFailedCallback != null) {
                onPlayingFailedCallback.operationFinished();
            }
            e.printStackTrace();
        }

    }

    /**
     * Call it in the start of prepare  to manage different media formats
     * and broken links. It will try to select working link and  convert format to
     * mp3 if it is not supported.
     *
     * @return true - if prepare should continues regulary, false if it will call atomatycly by callback
     */
    private boolean prepareLinkForPlaying() {
        if (mediaItem instanceof RadioItem) {
            ((RadioItem) mediaItem).fixAudeoLinks(new IOperationFinishCallback() {
                @Override
                public void operationFinished() { //Succeed to fetch valid URL
                    isLinkReadyForPlaying = true;
                    prepare();
                }
            }, new IOperationFinishCallback() {
                @Override
                public void operationFinished() {  //Failed to fetch valid URL
                    isLinkReadyForPlaying = true;
                    if (onPlayingFailedCallback != null) {
                        onPlayingFailedCallback.operationFinished();
                    }
                }
            });
            return false;
        }
        isLinkReadyForPlaying = true;
        return true;
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PLAYING);
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PAUSED);
            }
        }
    }

    public void toggle() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                start();
            }
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }


    /**
     * Restarts player in the soft way (didn't release all resurces and callbacks)
     */
    public void softRestart() {
        if (mediaPlayer != null) {
            resetPlayer();
            prepare();
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaItem = null;
            isPrepaired = false;
            isLinkReadyForPlaying = false;
        }
        NotificationManager nMgr = (NotificationManager) UpodsApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    public void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isLinkReadyForPlaying = false;
            isPrepaired = false;
        }
        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
    }

    public void removeListeners() {
        playerStateListener = null;
        onAutonomicTrackChangeCallback = null;
        onPlayingFailedCallback = null;
    }


    public IPlayableMediaItem getPlayingMediaItem() {
        return mediaItem;
    }


    public boolean isCurrentMediaItem(IPlayableMediaItem mediaItem) {
        if (this.mediaItem == null) {
            return false;
        }
        if (this.mediaItem instanceof ITrackable && mediaItem instanceof ITrackable) {
            return ((ITrackable) this.mediaItem).getSelectedTrack().getTitle().equals(((ITrackable) mediaItem).getSelectedTrack().getTitle());
        }

        return this.mediaItem.getName().equals(mediaItem.getName());

    }

    public boolean isCurrentTrack(Track track) {
        if (this.mediaItem == null || !(this.mediaItem instanceof ITrackable)) {
            return false;
        }
        return ((ITrackable) this.mediaItem).getSelectedTrack().getTitle().equals(track.getTitle());
    }

    public void seekTo(int ms) {
        if (mediaPlayer != null && ms >= 0) {
            mediaPlayer.setTime(ms);
        }
    }

    public void changeTrackToDirection(Direction direction) {
        if (universalPlayer.getPlayingMediaItem() instanceof ITrackable) {
            changeTrackToDirectionTrackable(direction);
        } else if (universalPlayer.getPlayingMediaItem() instanceof RadioItem) {
            changeTrackToDirectionRadio(direction);
        }
        if (onAutonomicTrackChangeCallback != null) {
            onAutonomicTrackChangeCallback.operationFinished();
        }
    }

    private void changeTrackToDirectionTrackable(Direction direction) {
        Track currentTrack = ((ITrackable) mediaItem).getSelectedTrack();
        ArrayList<? extends Track> tracks = ((ITrackable) mediaItem).getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            if (currentTrack.getTitle().equals(tracks.get(i).getTitle())) {
                int changeTo = MediaUtils.calculateNextTrackNumber(direction, i, tracks.size() - 1);
                ((ITrackable) mediaItem).selectTrack(tracks.get(changeTo));
                resetPlayer();
                setMediaItem(mediaItem);
                prepare();
                break;
            }
        }
    }

    private void changeTrackToDirectionRadio(Direction direction) {
        List<? extends IPlayableMediaItem> mediaItems = ProfileManager.getInstance().getRecentRadioItems();
        for (int i = 0; i < mediaItems.size(); i++) {
            IPlayableMediaItem iPlayableMediaItem = mediaItems.get(i);
            if (iPlayableMediaItem.getName().equals(mediaItem.getName())) {
                int changeTo = MediaUtils.calculateNextTrackNumber(direction, i, mediaItems.size() - 1);
                resetPlayer();
                setMediaItem(mediaItems.get(changeTo));
                prepare();
                break;
            }
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? (int) mediaPlayer.getTime() : 0;
    }


    private void runReconnectTask() {
        if (autoReconector == null) {
            autoReconector = new TimerTask() {
                @Override
                public void run() {
                    if (GlobalUtils.isInternetConnected()) {
                        Logger.printInfo(PLAYER_LOG, "Reconnector -> got internet connection -> restarting player...");
                        softRestart();
                        autoReconector.cancel();
                        autoReconector = null;
                    } else {
                        Logger.printInfo(PLAYER_LOG, "Reconnector -> no internet connection -> will try again later...");
                    }
                }
            };
        }
        new Timer().scheduleAtFixedRate(autoReconector, 0, RECONNECT_RATE);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        if (!isPrepaired && mediaItem instanceof ITrackable) {//Try to continue for last position
            String lastPosition = SettingsManager.getInstace().getPareSettingValue(SettingsManager.JS_EPISODS_POSITIONS,
                    ((ITrackable) mediaItem).getSelectedTrack().getTitle());
            if (!lastPosition.isEmpty()) {
                seekTo(Integer.valueOf(lastPosition));
            }
        }
        if (event.type == MediaPlayer.Event.Playing) {
            isPrepaired = true;
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PLAYING);
            }
        } else if (event.type == MediaPlayer.Event.Paused) {
            isPrepaired = true;
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PAUSED);
            }
        } else if (event.type == MediaPlayer.Event.EndReached) {
            if (mediaPlayer.getLength() == 0) {
                Logger.printInfo(PLAYER_LOG, "Failed to call play...");
                if (onPlayingFailedCallback != null) {
                    onPlayingFailedCallback.operationFinished();
                }
            } else {
                changeTrackToDirection(Direction.RIGHT);
            }
        } else if (event.type == MediaPlayer.Event.EncounteredError) {
            if (!GlobalUtils.isInternetConnected()) {
                runReconnectTask();
            }
        }
        //Logger.printInfo(PLAYER_LOG, "VLC event" + String.valueOf(event.type));
    }


}
