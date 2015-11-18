package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.utils.ui.UIHelper;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment implements MediaPlayer.OnPreparedListener, IPlayerStateListener {
    public static String TAG = "fragmentPlayer";
    public static final long DEFAULT_RADIO_DURATIO = 1000000;
    private static final int SB_PROGRESS_TOP_MARGIN_CORECTOR = 50;
    private static final float TOOLBAR_TEXT_SIZE = 20f;

    private IPlayableMediaItem playableMediaItem;
    private IOperationFinishCallback playlistTrackSelected;
    private UniversalPlayer universalPlayer;
    private Playlist playlist;

    private ImageButton btnPlay;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(btnPlayStopClickListener);
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
            playableMediaItem = (IPlayableMediaItem) savedInstanceState.get(ActivityPlayer.MEDIA_ITEM_EXTRA);
        }

        if (playableMediaItem != null) {
            initPlayerUI();
            runPlayer();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Playlist
        playlist = new Playlist(getActivity(), view, new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                playableMediaItem = universalPlayer.getPlayingMediaItem();
                initPlayerUI();
                runPlayer();
                playlist.updateTracks();
                initTrackNumbersSection();
            }
        });

        super.onViewCreated(view, savedInstanceState);
        lnPlayerinfo.setOnClickListener(playlist.getPlaylistOpenClickListener());
        initTrackNumbersSection();
        setSeekbarPosition(view);
    }

    @Override
    public void onDestroy() {
        if (universalPlayer != null) {
            universalPlayer.removeListeners();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ActivityPlayer.MEDIA_ITEM_EXTRA, playableMediaItem);
        super.onSaveInstanceState(outState);
    }

    private void initPlayerUI() {
        tvPlayerTitle.setText(playableMediaItem.getName());
        tvPlayserSubtitle.setText(playableMediaItem.getSubHeader());
        Glide.with(getActivity()).load(playableMediaItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgPlayerCover) {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                super.onResourceReady(drawable, anim);
                Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                int dominantColor = UIHelper.getDominantColor(bitmap);
                rlTopSectionBckg.setBackgroundColor(dominantColor);
            }
        });
        if (playableMediaItem instanceof ITrackable) {
            Track selectedTrack = ((ITrackable) playableMediaItem).getSelectedTrack();
            tvTrackDuration.setText(selectedTrack.getDuration());
        }
        if (playableMediaItem instanceof RadioItem) {
            sbPlayerProgress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        if (universalPlayer.isPrepaired && universalPlayer.isPlaying()) {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
            btnPlay.setImageResource(R.drawable.ic_pause_white);

        } else {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.buffering);
            btnPlay.setImageResource(R.drawable.ic_play_white);
        }
    }

    private void initTrackNumbersSection() {
        StringBuilder trackNumberString = new StringBuilder();
        trackNumberString.append(playlist.getCurrentTrackNumber());
        trackNumberString.append("/");
        trackNumberString.append(playlist.getTracksCount());
        tvTrackNumbers.setText(trackNumberString.toString());
    }

    private void runPlayer() {
        if (universalPlayer.isPrepaired && universalPlayer.isCurrentMediaItem(playableMediaItem)) { //Player already running
            Logger.printInfo(TAG, "Already playing");
            setPositionUpdateCallback();
            universalPlayer.setPreparedListener(this);
        } else {
            universalPlayer.releasePlayer();
            universalPlayer.setMediaItem(playableMediaItem);
            universalPlayer.setPreparedListener(this);
            universalPlayer.prepare();
        }

        universalPlayer.setPlayerStateListener(this);
        universalPlayer.setOnMetaDataFetchedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                MetaDataFetcher.MetaData metaData = (MetaDataFetcher.MetaData) data;
                metaData.bitrate = metaData.bitrate.replace("000", "");
                metaData.bitrate += getString(R.string.kbps);
                tvTrackInfo.setText(metaData.bitrate);
            }
        });

    }

    public void setPlayableItem(IPlayableMediaItem iPlayableMediaItem) {
        this.playableMediaItem = iPlayableMediaItem;
    }

    private void setSeekbarPosition(final View root) {
        final ViewTreeObserver observer = root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sbPlayerProgress.getLayoutParams();
                RelativeLayout rlPlayerUnderbar = (RelativeLayout) root.findViewById(R.id.rlPlayerUnderbar);
                params.bottomMargin = rlPlayerUnderbar.getHeight() - SB_PROGRESS_TOP_MARGIN_CORECTOR;
                sbPlayerProgress.requestLayout();
            }
        });
    }

    private void setPositionUpdateCallback() {
        universalPlayer.setPositionUpdatedCallback(new IOnPositionUpdatedCallback() {
            @Override
            public void poistionUpdated(final int currentPoistion) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });
            }
        });

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

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        btnPlay.setImageResource(R.drawable.ic_pause_white);
        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
        setPositionUpdateCallback();
        playlist.updateTracks();
    }

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        btnPlay.setImageResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
        if(state == UniversalPlayer.State.PLAYING){
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
        }
    }
}
