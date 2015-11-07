package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.player.MetaDataFetcher;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.ui.UIHelper;
import com.chickenkiller.upods2.views.Playlist;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment implements MediaPlayer.OnPreparedListener, IPlayerStateListener {
    public static String TAG = "fragmentPlayer";
    private static final float TOOLBAR_TEXT_SIZE = 20f;

    private ImageButton btnPlay;
    private IPlayableMediaItem playableMediaItem;
    private ImageView imgPlayerCover;
    private RelativeLayout rlTopSectionBckg;
    private UniversalPlayer universalPlayer;
    private TextView tvPlayserSubtitle;
    private TextView tvPlayerTitle;
    private TextView tvTrackInfo;
    private LinearLayout lnPlayerinfo;
    private Playlist playlist;

    private View.OnClickListener btnPlayStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                universalPlayer.toggle();
                btnPlay.setImageResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
            } else {
                runPlayer();
            }
        }
    };

    private View.OnClickListener imgClosePlayerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            universalPlayer.releasePlayer();
            getActivity().onBackPressed();
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
        lnPlayerinfo = (LinearLayout) view.findViewById(R.id.lnPlayerInfo);

        if (playableMediaItem == null) {
            playableMediaItem = (IPlayableMediaItem) savedInstanceState.get(ActivityPlayer.MEDIA_ITEM_EXTRA);
        }
        if (playableMediaItem != null) {
            initPlayerUI(view);
            universalPlayer = UniversalPlayer.getInstance();
            runPlayer();
        }

        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.buffering);
        TextView toolbarTitle = UIHelper.getToolbarTextView(((IToolbarHolder) getActivity()).getToolbar());
        toolbarTitle.setTextSize(TOOLBAR_TEXT_SIZE);
        toolbarTitle.setTypeface(null, Typeface.NORMAL);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Playlist
        playlist = new Playlist(getActivity(), view);
        lnPlayerinfo.setOnClickListener(playlist.getPlaylistOpenClickListener());
        super.onViewCreated(view, savedInstanceState);
    }

    public void initPlayerUI(View view) {
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

    }

    public void setPlayableItem(IPlayableMediaItem iPlayableMediaItem) {
        this.playableMediaItem = iPlayableMediaItem;
    }

    private void runPlayer() {
        universalPlayer.setPreparedListener(this);
        universalPlayer.setPlayerStateListener(this);
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentMediaItem(playableMediaItem)) {
            btnPlay.setImageResource(R.drawable.ic_pause_white);
            return;
        } else if (universalPlayer.isCurrentMediaItem(playableMediaItem)) {
            btnPlay.setImageResource(R.drawable.ic_play_white);
            return;
        }
        universalPlayer.resetPlayer();
        universalPlayer.setMediaItem(playableMediaItem);
        universalPlayer.setOnMetaDataFetchedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                MetaDataFetcher.MetaData metaData = (MetaDataFetcher.MetaData) data;
                metaData.bitrate = metaData.bitrate.replace("000", "");
                metaData.bitrate += getString(R.string.kbps);
                tvTrackInfo.setText(metaData.bitrate);
            }
        });
        universalPlayer.prepare();
        btnPlay.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        btnPlay.setImageResource(R.drawable.ic_pause_white);
        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.now_paying);
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

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        btnPlay.setImageResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
    }
}
