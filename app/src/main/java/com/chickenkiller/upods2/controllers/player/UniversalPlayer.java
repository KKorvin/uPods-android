package com.chickenkiller.upods2.controllers.player;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {


    private static final long RECONNECT_RATE = 5000;
    private int positionOffset = 0;

    public enum State {PLAYING, PAUSED}

    public static final String INTENT_ACTION_PLAY = "com.chickenkiller.upods2.player.PLAY";
    public static final String INTENT_ACTION_PAUSE = "com.chickenkiller.upods2.player.PAUSE";
    public static final String INTENT_ACTION_FORWARD = "com.chickenkiller.upods2.player.FORWARD";
    public static final String INTENT_ACTION_BACKWARD = "com.chickenkiller.upods2.player.BACKWARD";
    private static final String PLAYER_LOG = "UniversalPlayer";

    private static UniversalPlayer universalPlayer;
    private MediaPlayer mediaPlayer;

    private MediaPlayer.OnPreparedListener preparedListener;
    private IOperationFinishCallback onAutonomicTrackChangeCallback;
    private IOperationFinishCallback onPlayingFailedCallback;
    private IPlayerStateListener playerStateListener;

    private IPlayableMediaItem mediaItem;
    private PlayerNotificationPanel notificationPanel;

    private TimerTask autoReconector;

    public boolean isPrepaired;
    public boolean isLinkReadyForPlaying;


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

    public void setPreparedListener(MediaPlayer.OnPreparedListener preparedListener) {
        this.preparedListener = preparedListener;
    }

    public void setPlayerStateListener(IPlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public void setOnAutonomicTrackChangeCallback(IOperationFinishCallback onAutonomicTrackChangeCallback) {
        this.onAutonomicTrackChangeCallback = onAutonomicTrackChangeCallback;
    }

    public void prepare(IPlayableMediaItem mediaItem, MediaPlayer.OnPreparedListener preparedListener) {
        setPreparedListener(preparedListener);
        setMediaItem(mediaItem);
        prepare();
    }

    /**
     * Main prepare method. Prepares media item to be played.
     */
    public void prepare() {
        if (mediaItem == null) {
            throw new RuntimeException("MediaItem is not set. Call setMediaItem before prepare.");
        }
        if (!isPrepaired) {
            try {
                if (!isLinkReadyForPlaying && !prepareLinkForPlaying()) {
                    return;
                }
                Logger.printInfo(PLAYER_LOG, "Trying to play: " + mediaItem.getAudeoLink());

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(mediaItem.getAudeoLink());
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.setOnInfoListener(this);
                mediaPlayer.setOnBufferingUpdateListener(this);
                mediaPlayer.setOnCompletionListener(this);
                Logger.printInfo(PLAYER_LOG, "Calling  prepareAsync");
                mediaPlayer.prepareAsync();
                Logger.printInfo(PLAYER_LOG, "Prepared called");
            } catch (Exception e) {
                Logger.printInfo(PLAYER_LOG, "Failed to prepare player...");
                if (onPlayingFailedCallback != null) {
                    onPlayingFailedCallback.operationFinished();
                }
                e.printStackTrace();
            }
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
        if (mediaPlayer != null && isPrepaired) {
            mediaPlayer.start();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PLAYING);
            }
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PLAYING);
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPrepaired && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PAUSED);
            }
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PAUSED);
            }
        }
    }

    public void toggle() {
        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            start();
        }

    }

    public boolean isPlaying() {
        if (mediaPlayer != null && isPrepaired) {
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
            mediaPlayer.release();
            mediaPlayer = null;
            mediaItem = null;
            isPrepaired = false;
            isLinkReadyForPlaying = false;
            positionOffset = 0;
        }
        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
    }

    public void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            isPrepaired = false;
            isLinkReadyForPlaying = false;
        }
        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
    }

    public void removeListeners() {
        preparedListener = null;
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
        if (isPrepaired && ms >= 0) {
            mediaPlayer.seekTo(ms);
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
        return isPrepaired && mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
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
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPrepaired = true;
        Logger.printInfo(PLAYER_LOG, "mediaPlayer.start();");
        mediaPlayer.start();
        Logger.printInfo(PLAYER_LOG, "onPrepared - music should start playing here");
        if (mediaItem instanceof RadioItem) {
            ProfileManager.getInstance().addRecentMediaItem(mediaItem);
        }

        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
        notificationPanel = new DefaultNotificationPanel(UpodsApplication.getContext(), mediaItem);

        if (preparedListener != null) {
            preparedListener.onPrepared(mediaPlayer);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.printInfo(PLAYER_LOG, "Error code: " + String.valueOf(what));
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Logger.printInfo(PLAYER_LOG, "Info code: " + String.valueOf(what));
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START && !GlobalUtils.isInternetConnected()) {
            runReconnectTask();
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Log.i(PLAYER_LOG, "Player buffer: " + String.valueOf(percent));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Logger.printInfo(PLAYER_LOG, "Completed current track, switchin to nex in playlit");
        changeTrackToDirection(Direction.RIGHT);
    }
}
