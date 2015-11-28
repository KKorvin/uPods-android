package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.player.MetaDataFetcher;
import com.chickenkiller.upods2.controllers.player.PlayerPositionUpdater;
import com.chickenkiller.upods2.controllers.player.Playlist;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.DataHolder;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment implements MediaPlayer.OnPreparedListener, IPlayerStateListener {
    public static String TAG = "fragmentPlayer";
    public static final long DEFAULT_RADIO_DURATIO = 1000000;
    private static final float TOOLBAR_TEXT_SIZE = 20f;
    private static final int COVER_IMAGE_SIZE = UIHelper.dpToPixels(100);

    private IPlayableMediaItem playableMediaItem;
    private UniversalPlayer universalPlayer;
    private Playlist playlist;
    private PlayerPositionUpdater playerPositionUpdater;

    private View rootView;
    private ImageButton btnPlay;
    private ImageButton btnRewindLeft;
    private ImageButton btnRewindRight;
    private ImageView imgPlayerCover;
    private RelativeLayout rlTopSectionBckg;
    private TextView tvPlayserSubtitle;
    private TextView tvPlayerTitle;
    private TextView tvTrackInfo;
    private TextView tvTrackCurrentTime;
    private TextView tvTrackDuration;
    private TextView tvTrackNumbers;
    private SeekBar sbPlayerProgress;
    private LinearLayout lnPlayerinfo;

    private long maxDuration = -1;
    private boolean isChangingProgress = false;
    private boolean isFirstRun = true;

    private View.OnClickListener btnPlayStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                universalPlayer.toggle();
                btnPlay.setImageResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
                playlist.updateTracks();
            }
        }
    };

    private View.OnClickListener btnForwardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                playlist.goForward();
            }
        }
    };

    private View.OnClickListener btnBackwardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                playlist.goBackward();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        btnRewindLeft = (ImageButton) view.findViewById(R.id.btnRewindLeft);
        btnRewindRight = (ImageButton) view.findViewById(R.id.btnRewindRight);
        btnPlay.setOnClickListener(btnPlayStopClickListener);
        btnRewindLeft.setOnClickListener(btnBackwardClickListener);
        btnRewindRight.setOnClickListener(btnForwardClickListener);
        rlTopSectionBckg = (RelativeLayout) view.findViewById(R.id.rlTopSectionBckg);
        imgPlayerCover = (ImageView) view.findViewById(R.id.imgPlayerCover);
        tvPlayerTitle = (TextView) view.findViewById(R.id.tvPlayerTitle);
        tvPlayserSubtitle = (TextView) view.findViewById(R.id.tvPlayserSubtitle);
        tvTrackInfo = (TextView) view.findViewById(R.id.tvTrackInfo);
        tvTrackDuration = (TextView) view.findViewById(R.id.tvTrackDuration);
        tvTrackCurrentTime = (TextView) view.findViewById(R.id.tvTrackCurrentTime);
        tvTrackNumbers = (TextView) view.findViewById(R.id.tvTrackNumbers);
        lnPlayerinfo = (LinearLayout) view.findViewById(R.id.lnPlayerInfo);
        sbPlayerProgress = (SeekBar) view.findViewById(R.id.sbPlayerProgress);

        universalPlayer = UniversalPlayer.getInstance();

        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.buffering);
        TextView toolbarTitle = UIHelper.getToolbarTextView(((IToolbarHolder) getActivity()).getToolbar());
        toolbarTitle.setTextSize(TOOLBAR_TEXT_SIZE);
        toolbarTitle.setTypeface(null, Typeface.NORMAL);

        if (playableMediaItem == null) {
            playableMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        final ViewTreeObserver observer = rootView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sbPlayerProgress.getLayoutParams();
                RelativeLayout rlPlayerUnderbar = (RelativeLayout) rootView.findViewById(R.id.rlPlayerUnderbar);
                params.bottomMargin = rlPlayerUnderbar.getHeight() - sbPlayerProgress.getHeight() / 2;
                sbPlayerProgress.requestLayout();
            }
        });
    }

    @Override
    public void onResume() {
        //If playableMediaItem was changed when fragment was in backround, replace it.
        if (!isFirstRun && universalPlayer.isPrepaired && universalPlayer.getPlayingMediaItem() != null &&
                !universalPlayer.isCurrentMediaItem(playableMediaItem)) {
            playableMediaItem = universalPlayer.getPlayingMediaItem();
        }
        isFirstRun = false;
        initPlayerUI();
        configurePlayer();
        setPlayerCallbacks();
        runPositionUpdater();
        super.onResume();
    }

    @Override
    public void onPause() {
        DataHolder.getInstance().remove(ActivityPlayer.MEDIA_ITEM_EXTRA);
        if (playerPositionUpdater != null) {
            playerPositionUpdater.cancel(false);
        }
        universalPlayer.removeListeners();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setPlayableItem(IPlayableMediaItem iPlayableMediaItem) {
        this.playableMediaItem = iPlayableMediaItem;
    }

    /**
     * Inits player UI accorfing to current MediaItem
     */
    private void initPlayerUI() {
        if (playableMediaItem instanceof ITrackable) {
            tvPlayerTitle.setText(((ITrackable) playableMediaItem).getSelectedTrack().getTitle());
        } else {
            tvPlayerTitle.setText(playableMediaItem.getName());
        }
        tvPlayserSubtitle.setText(playableMediaItem.getSubHeader());
        if (playableMediaItem.getCoverImageUrl() == null) {
            final LetterBitmap letterBitmap = new LetterBitmap(getActivity());
            Bitmap letterTile = letterBitmap.getLetterTile(playableMediaItem.getName(), playableMediaItem.getName(), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
            imgPlayerCover.setImageBitmap(letterTile);
            int dominantColor = UIHelper.getDominantColor(letterTile);
            rlTopSectionBckg.setBackgroundColor(dominantColor);
        } else {
            Glide.with(getActivity()).load(playableMediaItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgPlayerCover) {
                @Override
                public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                    super.onResourceReady(drawable, anim);
                    Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                    int dominantColor = UIHelper.getDominantColor(bitmap);
                    rlTopSectionBckg.setBackgroundColor(dominantColor);
                }
            });
        }
        if (playableMediaItem instanceof ITrackable) {
            Track selectedTrack = ((ITrackable) playableMediaItem).getSelectedTrack();
            tvTrackDuration.setText(selectedTrack.getDuration());
            sbPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isChangingProgress = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isChangingProgress = false;
                    int progress = seekBar.getProgress();
                    int position = (int) ((maxDuration * progress) / 100);
                    seekBar.setProgress(progress);
                    universalPlayer.seekTo(position);
                }
            });
        }
        if (playableMediaItem instanceof RadioItem) {
            sbPlayerProgress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }


    /**
     * Inits player UI accorfing to current player state, call it after configurePlayer().
     */
    private void initPlayerStateUI() {
        if (universalPlayer.isPrepaired && universalPlayer.isPlaying()) {
            btnPlay.setImageResource(R.drawable.ic_pause_white);

        } else {
            btnPlay.setImageResource(R.drawable.ic_play_white);
        }

        if (universalPlayer.isPrepaired) {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
            btnPlay.setImageResource(R.drawable.ic_pause_white);

        } else {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.buffering);
            btnPlay.setImageResource(R.drawable.ic_play_white);
        }
    }

    private void initTrackNumbersSection() {
        StringBuilder trackNumberString = new StringBuilder();
        trackNumberString.append(playlist.getCurrentTrackNumber() + 1);
        trackNumberString.append("/");
        trackNumberString.append(playlist.getTracksCount());
        tvTrackNumbers.setText(trackNumberString.toString());
    }

    private void configurePlayer() {
        if (universalPlayer.isPrepaired && universalPlayer.isCurrentMediaItem(playableMediaItem)) { //Player already running
            Logger.printInfo(TAG, "Configured from playing MediaItem");
            if (playlist == null) {
                createPlaylist();
            }
        } else {
            Logger.printInfo(TAG, "Starting new MediaItem");
            universalPlayer.releasePlayer();
            universalPlayer.setMediaItem(playableMediaItem);
            universalPlayer.prepare();
            createPlaylist();
        }
        initPlayerStateUI();
        playlist.updateTracks();
    }

    private void createPlaylist() {
        playlist = new Playlist(getActivity(), rootView, new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                playableMediaItem = (IPlayableMediaItem) DataHolder.getInstance().retrieve(ActivityPlayer.MEDIA_ITEM_EXTRA);
                if (playableMediaItem == null) {
                    playableMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
                    Log.e(TAG, "Error! Playlist callback -> can't retrieve mediaItem from DataHolder -> getting it from Player");
                }
                initPlayerUI();
                configurePlayer();
                initTrackNumbersSection();
            }
        });
        lnPlayerinfo.setOnClickListener(playlist.getPlaylistOpenClickListener());
        initTrackNumbersSection();
    }

    private void setPlayerCallbacks() {
        //Sets all callback to player
        universalPlayer.setPreparedListener(this);
        universalPlayer.setPlayerStateListener(this);
        universalPlayer.setOnMetaDataFetchedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                if (isAdded()) {
                    MetaDataFetcher.MetaData metaData = (MetaDataFetcher.MetaData) data;
                    metaData.bitrate = metaData.bitrate.replace("000", "");
                    metaData.bitrate += getString(R.string.kbps);
                    tvTrackInfo.setText(metaData.bitrate);
                }
            }
        });

        universalPlayer.setOnAutonomicTrackChangeCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                if (isAdded()) {
                    playableMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
                    initPlayerUI();
                    configurePlayer();
                    initTrackNumbersSection();
                }
            }
        });
    }

    private void runPositionUpdater() {
        playerPositionUpdater = (PlayerPositionUpdater) new PlayerPositionUpdater(new IOnPositionUpdatedCallback() {
            @Override
            public void poistionUpdated(int currentPoistion) {
                if (isAdded()) {
                    tvTrackCurrentTime.setText(MediaUtils.formatMsToTimeString(currentPoistion));
                    if (!isChangingProgress) {
                        if (maxDuration < 0) {
                            maxDuration = playableMediaItem instanceof RadioItem ? DEFAULT_RADIO_DURATIO
                                    : MediaUtils.timeStringToLong(tvTrackDuration.getText().toString());
                        }
                        int progress = (int) (currentPoistion * 100 / maxDuration);
                        sbPlayerProgress.setProgress(progress);
                    }
                }
            }

            @Override
            public void poistionUpdaterStoped() {

            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isAdded()) {
            if (((IToolbarHolder) getActivity()).getToolbar() != null) {
                ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
            }
            btnPlay.setImageResource(R.drawable.ic_pause_white);
            playlist.updateTracks();
        }
    }

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        if (isAdded()) {
            btnPlay.setImageResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
        }
    }
}
