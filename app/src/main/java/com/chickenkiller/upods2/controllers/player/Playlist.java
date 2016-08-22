package com.chickenkiller.upods2.controllers.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.PlaylistMediaItemsAdapter;
import com.chickenkiller.upods2.controllers.adaperts.PlaylistTracksAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.INowPlayingItemPosiontGetter;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Analytics;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.utils.enums.Direction;
import com.chickenkiller.upods2.views.PlayPauseView;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

/**
 * Created by Alon Zilberman on 11/6/15.
 */
public class Playlist implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Playlist";
    private static final long PLAYLIST_ANIMATION_DURATION = 400;
    private static final long BUTTON_ANIMATION_DURATION = 400;
    private static final float BTN_POSITION_MULTIPLYER = 0.76f;
    private static final float BUTTON_ANIMATION_ANGLE = 60f;

    private IOperationFinishCallback playlistTrackSelected;

    private LinearLayout lnPlaylist;
    private LinearLayout lnPlayerContorls;
    private RelativeLayout rlPlayerInfoSection;
    private ListView lvPlaylist;
    private RelativeLayout rlPlayerUnderbar;
    private SeekBar sbPlayerProgress;
    private PlayPauseView btnPlay;
    private Context mContext;
    private ArrayAdapter playlistAdapter;
    private ImageButton btnPlPlay;
    private TextView tvTrackCurrentTime;
    private TextView tvTrackDuration;
    private TextView tvTrackInfo;

    private int initialPlayListMargin;
    private float btnFinalY;
    private float btnFinalX;
    private float btnInitialX;
    private float btnInitialY;
    private boolean animationFirstRun;
    private boolean isOpen;

    public Playlist(Context mContext, View rootView, IOperationFinishCallback playlistTrackSelected) {
        this.mContext = mContext;
        this.lnPlaylist = (LinearLayout) rootView.findViewById(R.id.lnPlaylist);
        this.btnPlay = (PlayPauseView) rootView.findViewById(R.id.btnPlay);
        this.lnPlayerContorls = (LinearLayout) rootView.findViewById(R.id.lnPlayerContorls);
        this.rlPlayerUnderbar = (RelativeLayout) rootView.findViewById(R.id.rlPlayerUnderbar);
        this.sbPlayerProgress = (SeekBar) rootView.findViewById(R.id.sbPlayerProgress);
        this.rlPlayerInfoSection = (RelativeLayout) rootView.findViewById(R.id.rlPlayerInfoSection);
        this.tvTrackDuration = (TextView) rootView.findViewById(R.id.tvTrackDuration);
        this.tvTrackCurrentTime = (TextView) rootView.findViewById(R.id.tvTrackCurrentTime);
        this.tvTrackInfo = (TextView) rootView.findViewById(R.id.tvTrackInfo);
        this.lvPlaylist = (ListView) rootView.findViewById(R.id.lvPlaylist);
        this.btnPlPlay = (ImageButton) rootView.findViewById(R.id.btnPlPlay);
        this.animationFirstRun = true;
        this.playlistTrackSelected = playlistTrackSelected;
        initPlaylist();

    }

    public View.OnClickListener getPlaylistOpenClickListener() {
        isOpen = false;
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(Analytics.PLAYLIST_TOGGLE);
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
        if (universalPlayer.getPlayingMediaItem() instanceof Podcast) {
            List<Track> tracks = (List<Track>) ((Podcast) universalPlayer.getPlayingMediaItem()).getTracks();
            playlistAdapter = new PlaylistTracksAdapter(mContext, R.layout.playlist_item, tracks);

        } else if (universalPlayer.getPlayingMediaItem() instanceof RadioItem) {
            List<MediaItem> mediaItems = new ArrayList<>();
            mediaItems.addAll(ProfileManager.getInstance().getRecentRadioItems());
            MediaItem currentPlayingItem = MediaItem.getMediaItemByName((ArrayList<? extends MediaItem>) mediaItems, universalPlayer.getPlayingMediaItem());
            if (currentPlayingItem != null) {
                mediaItems.remove(currentPlayingItem);
                mediaItems.add(0, currentPlayingItem);
            }
            playlistAdapter = new PlaylistMediaItemsAdapter(mContext, R.layout.playlist_item, mediaItems);
        }
        lvPlaylist.setAdapter(playlistAdapter);
        lvPlaylist.setOnItemClickListener(this);
    }

    private void runOpenPlaylistAnimation() {
        sbPlayerProgress.setVisibility(View.INVISIBLE);
        tvTrackDuration.setVisibility(View.INVISIBLE);
        tvTrackCurrentTime.setVisibility(View.INVISIBLE);
        tvTrackInfo.setVisibility(View.INVISIBLE);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lnPlaylist.getLayoutParams();

        if (animationFirstRun) {
            final RelativeLayout.LayoutParams btnPlayParams = (RelativeLayout.LayoutParams) btnPlay.getLayoutParams();
            initialPlayListMargin = params.bottomMargin;

            int cords[] = new int[2];
            btnPlay.getLocationOnScreen(cords);

            btnInitialX = cords[0] + btnPlay.getWidth() / 2;
            btnInitialY = cords[1] - (btnPlay.getHeight() / 2) - btnPlayParams.bottomMargin;

            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

            btnFinalY = btnInitialY - lnPlaylist.getHeight() + rlPlayerUnderbar.getHeight() + btnPlayParams.bottomMargin;
            btnFinalX = metrics.widthPixels * BTN_POSITION_MULTIPLYER;
        }

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
        runPlayButtonAnimation(btnFinalX, btnFinalY, Side.LEFT);
    }

    private void runOpenInfoSectionAnimation(long duration) {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPlayerInfoSection.getLayoutParams();

        if (animationFirstRun) {
            animationFirstRun = false;
        }

        int animateMarginTo = -(lnPlaylist.getHeight() - lnPlayerContorls.getHeight() - rlPlayerUnderbar.getHeight()); //should me minus value
        ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, animateMarginTo);
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
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sbPlayerProgress.setVisibility(View.VISIBLE);
                tvTrackDuration.setVisibility(View.VISIBLE);
                tvTrackInfo.setVisibility(View.VISIBLE);
                tvTrackCurrentTime.setVisibility(View.VISIBLE);
            }
        });
        animator.setDuration(PLAYLIST_ANIMATION_DURATION);
        animator.start();
        runCloseInfoSectionAnimation(PLAYLIST_ANIMATION_DURATION);

        //Button animation
        runPlayButtonAnimation(btnInitialX, btnInitialY, Side.RIGHT);
    }

    private void runPlayButtonAnimation(float toXDelta, float toYDelta, Side side) {
        ArcAnimator.createArcAnimator(btnPlay, toXDelta, Math.abs(toYDelta), BUTTON_ANIMATION_ANGLE, side)
                .setDuration(BUTTON_ANIMATION_DURATION)
                .start();
    }

    private void runCloseInfoSectionAnimation(long duration) {
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rlPlayerInfoSection.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, 0);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        YandexMetrica.reportEvent(Analytics.PLAYLIST_ITEM_CLICK);
        changeTrack(position);
    }

    public void goForward() {
        int changeTo = MediaUtils.calculateNextTrackNumber(Direction.RIGHT, getCurrentTrackNumber(), getTracksCount() - 1);
        changeTrack(changeTo);
    }

    public void goBackward() {
        int changeTo = MediaUtils.calculateNextTrackNumber(Direction.LEFT, getCurrentTrackNumber(), getTracksCount() - 1);
        changeTrack(changeTo);
    }

    private void changeTrack(int position) {
        UniversalPlayer universalPlayer = UniversalPlayer.getInstance();
        if (playlistAdapter instanceof PlaylistMediaItemsAdapter) {
            MediaItem clickedIPlayableMediaItem = ((PlaylistMediaItemsAdapter) playlistAdapter).getItem(position);
            if (universalPlayer.isCurrentMediaItem(clickedIPlayableMediaItem)) {
                universalPlayer.toggle();
                Logger.printInfo(LOG_TAG, "Clicked on current trcack -> toogling it");
            } else {
                universalPlayer.resetPlayer();
                UniversalPlayer.getInstance().setMediaItem(clickedIPlayableMediaItem);
                Logger.printInfo(LOG_TAG, "Track switched to: " + clickedIPlayableMediaItem.getName());
            }
        } else if (playlistAdapter instanceof PlaylistTracksAdapter) {
            Track clieckedTrack = ((PlaylistTracksAdapter) playlistAdapter).getItem(position);
            if (universalPlayer.isCurrentTrack(clieckedTrack)) {
                universalPlayer.toggle();
                Logger.printInfo(LOG_TAG, "Clicked on current trcack -> toogling it");
            } else {
                Podcast trackable = (Podcast) universalPlayer.getPlayingMediaItem();
                trackable.selectTrack(clieckedTrack);
                universalPlayer.resetPlayer();
                UniversalPlayer.getInstance().setMediaItem(trackable);
                Logger.printInfo(LOG_TAG, "Track switched to: " + clieckedTrack.getTitle());
            }
        }
        if (playlistTrackSelected == null) {
            Logger.printError(LOG_TAG, "Attention! Playlist callback not set, UI will not be updated");
        } else {
            playlistTrackSelected.operationFinished();
        }
    }

    public int getTracksCount() {
        return playlistAdapter.getCount() + 1;
    }

    public int getCurrentTrackNumber() {
        if (playlistAdapter instanceof INowPlayingItemPosiontGetter) {
            return ((INowPlayingItemPosiontGetter) playlistAdapter).getNowPlayingItemPosition();
        }
        return 0;
    }
}
