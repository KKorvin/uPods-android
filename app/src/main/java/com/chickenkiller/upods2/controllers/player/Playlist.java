package com.chickenkiller.upods2.controllers.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.PlaylistMediaItemsAdapter;
import com.chickenkiller.upods2.controllers.adaperts.PlaylistTracksAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.ui.ArcTranslateAnimation;

import java.util.List;

/**
 * Created by alonzilberman on 11/6/15.
 */
public class Playlist {

    private static final long PLAYLIST_ANIMATION_DURATION = 400;
    private static final long BUTTON_ANIMATION_DURATION = 400;
    private static final float BTN_POSITION_MULTIPLYER = 0.82f;
    private static final int BTN_Y_POISTION_CORRECTOR = 40;
    private static final float INFO_START_POINT_CORRECTOR = 1.4f;
    private static final float INFO_ANIMATION_MARGIN_CORECTOR = 0.92f;

    private int pInfoAnimationStartPoint; //When start to animate player info section


    private LinearLayout lnPlaylist;
    private LinearLayout lnPlayerContorls;
    private RelativeLayout rlPlayerInfoSection;
    private ListView lvPlaylist;
    private RelativeLayout rlPlayerUnderbar;
    private SeekBar sbPlayerProgress;
    private ImageButton btnPlay;
    private Context mContext;
    private ArrayAdapter playlistAdapter;

    private int initialPlayListMargin;
    private int pInfoAnimationMargin;
    private int initialPlayPInfo;
    private float btnFinalY;
    private float btnFinalX;
    private boolean animationFirstRun;
    private boolean isOpen;

    public Playlist(Context mContext, View rootView) {
        this.mContext = mContext;
        this.lnPlaylist = (LinearLayout) rootView.findViewById(R.id.lnPlaylist);
        this.btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        this.lnPlayerContorls = (LinearLayout) rootView.findViewById(R.id.lnPlayerContorls);
        this.rlPlayerUnderbar = (RelativeLayout) rootView.findViewById(R.id.rlPlayerUnderbar);
        this.sbPlayerProgress = (SeekBar) rootView.findViewById(R.id.sbPlayerProgress);
        this.rlPlayerInfoSection = (RelativeLayout) rootView.findViewById(R.id.rlPlayerInfoSection);
        this.lvPlaylist = (ListView) rootView.findViewById(R.id.lvPlaylist);
        this.animationFirstRun = true;
        initPlaylist();
    }

    public View.OnClickListener getPlaylistOpenClickListener() {
        isOpen = false;
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    runCloseAnimation();
                } else {
                    runOpenPlaylistAnimation();
                }
                isOpen = !isOpen;
            }
        };
    }

    private void initPlaylist() {
        UniversalPlayer universalPlayer = UniversalPlayer.getInstance();
        if (universalPlayer.getPlayingMediaItem() instanceof ITrackable) {
            List<Track> tracks = (List<Track>) ((ITrackable) universalPlayer.getPlayingMediaItem()).getTracks();
            playlistAdapter = new PlaylistTracksAdapter(mContext, R.layout.playlist_item, tracks);

        } else if (universalPlayer.getPlayingMediaItem() instanceof RadioItem) {
            List<? extends IPlayableMediaItem> mediaItems = ProfileManager.getInstance().getRecentRadioItems();
            playlistAdapter = new PlaylistMediaItemsAdapter(mContext, R.layout.playlist_item, (List<IPlayableMediaItem>) mediaItems);
        }
        lvPlaylist.setAdapter(playlistAdapter);
    }

    private void runOpenPlaylistAnimation() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lnPlaylist.getLayoutParams();

        pInfoAnimationStartPoint = lnPlayerContorls.getHeight() + rlPlayerUnderbar.getHeight() + sbPlayerProgress.getHeight();
        pInfoAnimationStartPoint *= INFO_START_POINT_CORRECTOR;

        if (animationFirstRun) {
            initialPlayListMargin = params.bottomMargin;
            btnFinalY = Math.abs(params.bottomMargin) - btnPlay.getWidth();
            btnFinalY = -btnFinalY - BTN_Y_POISTION_CORRECTOR;
            btnFinalX = btnPlay.getX() * BTN_POSITION_MULTIPLYER;
        }

        //Layout animation
        final ValueAnimator animator = ValueAnimator.ofInt(params.bottomMargin, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int currentValue = (Integer) valueAnimator.getAnimatedValue();
                params.bottomMargin = currentValue;
                lnPlaylist.requestLayout();
            }
        });
        animator.setDuration(PLAYLIST_ANIMATION_DURATION);
        animator.start();
        runOpenInfoSectionAnimation(PLAYLIST_ANIMATION_DURATION);

        //Button animation
        ArcTranslateAnimation anim = new ArcTranslateAnimation(0, btnFinalX, 0, btnFinalY);
        anim.setDuration(BUTTON_ANIMATION_DURATION);
        anim.setFillAfter(true);
        btnPlay.startAnimation(anim);
    }

    private void runOpenInfoSectionAnimation(long duration) {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPlayerInfoSection.getLayoutParams();

        if (animationFirstRun) {
            initialPlayPInfo = params.topMargin;
            pInfoAnimationMargin = params.topMargin - pInfoAnimationStartPoint;
            pInfoAnimationMargin *= INFO_ANIMATION_MARGIN_CORECTOR;
            animationFirstRun = false;
        }

        ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, pInfoAnimationMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                rlPlayerInfoSection.requestLayout();
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    private void runCloseAnimation() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lnPlaylist.getLayoutParams();

        final ValueAnimator animator = ValueAnimator.ofInt(0, initialPlayListMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int currentValue = (Integer) valueAnimator.getAnimatedValue();
                params.bottomMargin = currentValue;
                lnPlaylist.requestLayout();
            }
        });
        animator.setDuration(PLAYLIST_ANIMATION_DURATION);
        animator.start();
        runCloseInfoSectionAnimation(PLAYLIST_ANIMATION_DURATION);

        //Button animation
        ArcTranslateAnimation anim = new ArcTranslateAnimation(btnFinalX, 0, btnFinalY, 0);
        anim.setDuration(BUTTON_ANIMATION_DURATION);
        anim.setFillAfter(true);
        btnPlay.startAnimation(anim);
    }

    private void runCloseInfoSectionAnimation(long duration) {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPlayerInfoSection.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(pInfoAnimationMargin, initialPlayPInfo);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                rlPlayerInfoSection.requestLayout();
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public void updateTracks() {
        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }

}
