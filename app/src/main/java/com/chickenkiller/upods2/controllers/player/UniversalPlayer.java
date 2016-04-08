package com.chickenkiller.upods2.controllers.player;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.webkit.URLUtil;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.models.MediaItem;
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

    public enum State {PLAYING, PAUSED, END_REACHED}

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

    private MediaItem mediaItem;
    private PlayerNotificationPanel notificationPanel;

    private TimerTask autoReconector;

    public boolean isLinkReadyForPlaying;
    public boolean isPrepaired;

    private UniversalPlayer() {
    }

    public static synchronized UniversalPlayer getInstance() {
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
    public void setMediaItem(MediaItem mediaItem) {
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

    /**
     * Checks if given type of media item is supported, if yes copies it and save instance in player.
     *
     * @param needReset - if true will reset the player before changing the mediaItem
     * @param mediaItem
     */
    public void setMediaItem(MediaItem mediaItem, boolean needReset) {
        if (needReset && !isCurrentMediaItem(mediaItem)) {
            resetPlayer();
        }
        setMediaItem(mediaItem);
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
                    if (notificationPanel != null) {
                        notificationPanel.notificationCancel();
                    }
                    notificationPanel = new DefaultNotificationPanel(UpodsApplication.getContext(), mediaItem);
                    notificationPanel.updateNotificationStatus(State.PLAYING);
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
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
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


    public MediaItem getPlayingMediaItem() {
        return mediaItem;
    }


    public boolean isCurrentMediaItem(MediaItem mediaItem) {
        if (this.mediaItem == null) {
            return false;
        }
        if (this.mediaItem instanceof Podcast && mediaItem instanceof Podcast) {
            return ((Podcast) this.mediaItem).getSelectedTrack().getTitle().equals(((Podcast) mediaItem).getSelectedTrack().getTitle());
        }

        return this.mediaItem.getName().equals(mediaItem.getName());

    }

    public boolean isCurrentTrack(Track track) {
        if (this.mediaItem == null || !(this.mediaItem instanceof Podcast)) {
            return false;
        }
        return ((Podcast) this.mediaItem).getSelectedTrack().getTitle().equals(track.getTitle());
    }

    public void seekTo(int ms) {
        if (mediaPlayer != null && ms >= 0) {
            mediaPlayer.setTime(ms);
        }
    }

    public void changeTrackToDirection(Direction direction) {
        if (universalPlayer.getPlayingMediaItem() instanceof Podcast) {
            changeTrackToDirectionTrackable(direction);
        } else if (universalPlayer.getPlayingMediaItem() instanceof RadioItem) {
            changeTrackToDirectionRadio(direction);
        }
        if (onAutonomicTrackChangeCallback != null) {
            onAutonomicTrackChangeCallback.operationFinished();
        }
    }

    private void changeTrackToDirectionTrackable(Direction direction) {
        Track currentTrack = ((Podcast) mediaItem).getSelectedTrack();
        ArrayList<? extends Track> tracks = ((Podcast) mediaItem).getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            if (currentTrack.getTitle().equals(tracks.get(i).getTitle())) {
                int changeTo = MediaUtils.calculateNextTrackNumber(direction, i, tracks.size() - 1);
                ((Podcast) mediaItem).selectTrack(tracks.get(changeTo));
                resetPlayer();
                setMediaItem(mediaItem);
                prepare();
                break;
            }
        }
    }

    private void changeTrackToDirectionRadio(Direction direction) {
        List<? extends MediaItem> mediaItems = ProfileManager.getInstance().getRecentRadioItems();
        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem iPlayableMediaItem = mediaItems.get(i);
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
        if (!isPrepaired && mediaItem instanceof Podcast) {//Try to continue for last position
            int lastPosition = ProfileManager.getInstance().getTrackPosition(((Podcast) mediaItem).getSelectedTrack());
            if (lastPosition > 0) {
                seekTo(lastPosition);
            }
        }
        if (event.type == MediaPlayer.Event.EndReached) {
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.END_REACHED);
            }
        }
        if (event.type == MediaPlayer.Event.Playing) {
            isPrepaired = true;
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PLAYING);

                //To make sure notification panel is in correct state
                if (notificationPanel != null && notificationPanel.getCurrentState() != State.PLAYING) {
                    notificationPanel.updateNotificationStatus(State.PLAYING);
                }
            }
        } else if (event.type == MediaPlayer.Event.Paused) {
            isPrepaired = true;
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PAUSED);

                //To make sure notification panel is in correct state
                if (notificationPanel != null && notificationPanel.getCurrentState() != State.PAUSED) {
                    notificationPanel.updateNotificationStatus(State.PAUSED);
                }
            }
        } else if (event.type == MediaPlayer.Event.EndReached) {
            if (mediaPlayer.getLength() == 0) {
                Logger.printInfo(PLAYER_LOG, "Failed to call play...");
                if (onPlayingFailedCallback != null) {
                    onPlayingFailedCallback.operationFinished();
                }
            } /*else {
                changeTrackToDirection(Direction.RIGHT);
            }*/
        } else if (event.type == MediaPlayer.Event.EncounteredError) {
            if (!GlobalUtils.isInternetConnected()) {
                runReconnectTask();
            }
        }
        //Logger.printInfo(PLAYER_LOG, "VLC event" + String.valueOf(event.type));
    }


}
