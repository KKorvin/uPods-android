package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.adaperts.TracksAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.controllers.internet.EpisodesXMLHandler;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IMovable;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Feed;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;
import com.chickenkiller.upods2.views.DetailsScrollView;
import com.github.clans.fab.FloatingActionButton;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentMediaItemDetails extends Fragment implements View.OnTouchListener, IMovable {
    private static final int MAGIC_NUMBER = -250; //Don't know what it does
    private static final float BOTTOM_SCROLL_BORDER_PERCENT = 0.35f;
    private static final float COVER_SCALE_FACTOR = 2f;
    private static final int COVER_IMAGE_SIZE = UIHelper.dpToPixels(80);
    private static final int STATUS_BAR_HEIGHT = UIHelper.dpToPixels(24);
    private static int bottomScrollBorder;
    private static int topScrollBorder;
    public static String TAG = "media_details";

    private MediaItem playableItem;

    private RelativeLayout rlDetailedContent;
    private LinearLayout lnInternetError;
    private DetailsScrollView svDetails;
    private RecyclerView rvTracks;
    private TextView tvDetailedDescription;
    private TextView tvDetailedSubHeader;
    private TextView tvDetailedHeader;
    private TextView tvBottomHeader;
    private TextView tvDetailedDesHeader;
    private View viewDetailedHeader;
    private View viewStatusBar;
    private Button btnSubscribe;
    private ImageButton btnMediaMore;
    private ImageView imgDetailedTopCover;
    private ImageView imgBluredCover;
    private FloatingActionButton fbDetailsPlay;
    private TracksAdapter tracksAdapter;
    private ProgressBar pbTracks;

    private int moveDeltaY;
    private int screenHeight;
    private MediaItemType mediaItemType;


    private View.OnClickListener frgamentCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().onBackPressed();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_details, container, false);
        lnInternetError = (LinearLayout) view.findViewById(R.id.lnInternetError);
        rlDetailedContent = (RelativeLayout) view.findViewById(R.id.rlDetailedContent);
        viewStatusBar = view.findViewById(R.id.viewStatusBar);
        tvDetailedDescription = (TextView) view.findViewById(R.id.tvDetailedDescription);
        tvDetailedHeader = (TextView) view.findViewById(R.id.tvDetailedHeader);
        tvDetailedSubHeader = (TextView) view.findViewById(R.id.tvDetailedSubHeader);
        tvDetailedDesHeader = (TextView) view.findViewById(R.id.tvDetailedDesHeader);
        tvBottomHeader = (TextView) view.findViewById(R.id.tvDetailedBottomHeader);
        viewDetailedHeader = view.findViewById(R.id.viewDetailedHeader);
        btnSubscribe = (Button) view.findViewById(R.id.btnSubscribe);
        btnMediaMore = (ImageButton) view.findViewById(R.id.btnMediaMore);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);
        imgBluredCover = (ImageView) view.findViewById(R.id.imgBluredCover);
        fbDetailsPlay = (FloatingActionButton) view.findViewById(R.id.fbDetailsPlay);
        svDetails = (DetailsScrollView) view.findViewById(R.id.svDetails);
        rvTracks = (RecyclerView) view.findViewById(R.id.rvTracks);
        pbTracks = (ProgressBar) view.findViewById(R.id.pbTracks);
        moveDeltaY = 0;

        if (playableItem != null) {
            initImagesColors();
            tvDetailedHeader.setText(playableItem.getName());
            tvDetailedSubHeader.setText(playableItem.getSubHeader());
            tvBottomHeader.setText(playableItem.getBottomHeader());
            btnSubscribe.setText(playableItem.isSubscribed ? getString(R.string.unsubscribe) : getString(R.string.subscribe));
            initSubscribeBtn();

            if (playableItem.hasTracks()) {
                initTrackable();
            } else {
                initNotTrackable();
            }
        }

        rlDetailedContent.setOnTouchListener(this);
        view.setOnClickListener(frgamentCloseClickListener);
        initFragmentScrollConstants();

        return view;
    }

    /**
     * Init playable item with tracks (i.e podcast)
     */
    private void initTrackable() {
        //Tracks recycle view
        tracksAdapter = new TracksAdapter(playableItem, getActivity(), R.layout.track_item);
        tracksAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        tracksAdapter.setMediaItemType(mediaItemType);
        rvTracks.setAdapter(tracksAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        rvTracks.setLayoutManager(layoutManager);
        rvTracks.setVisibility(View.INVISIBLE);
        pbTracks.setVisibility(View.VISIBLE);
        svDetails.setVisibility(View.GONE);
        fbDetailsPlay.setVisibility(View.GONE);

        loadTracks();

        if (playableItem instanceof Podcast) {
            btnMediaMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IContextMenuManager) getActivity()).openContextMenu(v, ContextMenuType.PODCAST_MIDDLE_SCREEN, playableItem, new IOperationFinishCallback() {
                        @Override
                        public void operationFinished() {
                            if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED && !((Podcast) playableItem).isDownloaded) {
                                getActivity().onBackPressed();
                            }
                            tracksAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    /**
     * Init playable without tracks (i.e radio station)
     */
    private void initNotTrackable() {
        rvTracks.setVisibility(View.GONE);
        btnMediaMore.setVisibility(View.GONE);
        svDetails.setVisibility(View.VISIBLE);
        svDetails.setEnabled(false);
        svDetails.setIMovable(this);
        tvDetailedDescription.setText(playableItem.getDescription());
        fbDetailsPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalUtils.isInternetConnected()) {
                    UniversalPlayer.getInstance().setMediaItem(playableItem, true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ActivityPlayer.openWithIntent(getActivity());
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_internet_access), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Init colors and settings of header images
     */
    private void initImagesColors() {
        if (playableItem.getCoverImageUrl() == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final LetterBitmap letterBitmap = new LetterBitmap(getActivity());
                    Bitmap letterTile = letterBitmap.getLetterTile(playableItem.getName(), playableItem.getName(), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
                    imgDetailedTopCover.setImageBitmap(letterTile);
                    final int dominantColor = UIHelper.getDominantColor(letterTile);
                    final Bitmap scaledBitmap = UIHelper.createScaledBitmap(letterTile, COVER_SCALE_FACTOR);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgBluredCover.setImageBitmap(scaledBitmap);
                            viewStatusBar.setBackgroundColor(dominantColor);
                            viewDetailedHeader.setBackgroundColor(dominantColor);
                            tvDetailedDesHeader.setTextColor(dominantColor);
                            btnMediaMore.setBackground(UIHelper.getAdaptiveRippleDrawable(dominantColor, dominantColor));
                        }
                    });
                }
            }).run();
        } else {
            Glide.with(getActivity()).load(playableItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgDetailedTopCover) {
                @Override
                public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                    super.onResourceReady(drawable, anim);
                    Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                    int dominantColor = UIHelper.getDominantColor(bitmap);
                    viewStatusBar.setBackgroundColor(dominantColor);
                    viewDetailedHeader.setBackgroundColor(dominantColor);
                    tvDetailedDesHeader.setTextColor(dominantColor);
                    imgBluredCover.setImageBitmap(UIHelper.createScaledBitmap(bitmap, COVER_SCALE_FACTOR));
                    btnMediaMore.setBackground(UIHelper.getAdaptiveRippleDrawable(dominantColor, dominantColor));
                }
            });
        }
    }

    private void initSubscribeBtn() {
        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playableItem.isSubscribed) {
                    ProfileManager.getInstance().removeSubscribedMediaItem(playableItem);
                    btnSubscribe.setText(getString(R.string.subscribe));
                    if (playableItem instanceof Podcast) {
                        Feed.saveAsFeed(((Podcast) playableItem).getFeedUrl(), ((Podcast) playableItem).getEpisodes(), true);
                    }
                } else {
                    ProfileManager.getInstance().addSubscribedMediaItem(playableItem);
                    btnSubscribe.setText(getString(R.string.unsubscribe));
                }
            }
        });
    }


    private void initFragmentScrollConstants() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        bottomScrollBorder = screenHeight - (int) (screenHeight * BOTTOM_SCROLL_BORDER_PERCENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topScrollBorder = STATUS_BAR_HEIGHT;
            Logger.printInfo("topScrollBorder", topScrollBorder);
        } else {
            topScrollBorder = 0;
        }
    }

    private void loadTracks() {
        if (playableItem instanceof Podcast && mediaItemType == MediaItemType.PODCAST_DOWNLOADED && !((Podcast) playableItem).getEpisodes().isEmpty()) {
            playableItem.syncWithDB();
            tracksAdapter.addItems(((Podcast) playableItem).getEpisodes());
            rvTracks.setVisibility(View.VISIBLE);
            pbTracks.setVisibility(View.GONE);
            return;
        }

        BackendManager.getInstance().sendRequest(((Podcast) playableItem).getTracksFeed(), new ISimpleRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final String response) {
                        if (isAdded()) {
                            final ArrayList<Episode> parsedEpisodes = new ArrayList<Episode>();
                            try {
                                SAXParserFactory spf = SAXParserFactory.newInstance();
                                SAXParser sp = spf.newSAXParser();
                                XMLReader xr = sp.getXMLReader();
                                EpisodesXMLHandler episodesXMLHandler = new EpisodesXMLHandler();
                                xr.setContentHandler(episodesXMLHandler);
                                //TODO could be encoding problem
                                InputSource inputSource = new InputSource(new StringReader(response));
                                xr.parse(inputSource);
                                parsedEpisodes.addAll(episodesXMLHandler.getParsedEpisods());

                                for (Episode episode : ((Podcast) playableItem).getEpisodes()) {
                                    Episode episodeInList = Episode.getEpisodByTitle(parsedEpisodes, episode.getTitle());
                                    if (episodeInList != null) {
                                        episodeInList.isDownloaded = episode.isDownloaded;
                                        episodeInList.isNew = episode.isNew;
                                        episodeInList.isExistsInDb = episode.isExistsInDb;
                                        episodeInList.id = episode.id;
                                    }
                                }

                                if (playableItem instanceof Podcast) {
                                    Podcast podcast = ((Podcast) playableItem);
                                    podcast.setDescription(episodesXMLHandler.getPodcastSummary());
                                    podcast.setTrackCount(String.valueOf(parsedEpisodes.size()));
                                    podcast.setTracks(parsedEpisodes);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tracksAdapter != null) {
                                        tracksAdapter.addItems(parsedEpisodes);
                                        rvTracks.setVisibility(View.VISIBLE);
                                        pbTracks.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onRequestFailed() {
                        rvTracks.setVisibility(View.GONE);
                        pbTracks.setVisibility(View.GONE);
                        lnInternetError.setVisibility(View.VISIBLE);
                    }
                }

        );

    }


    public void setPlayableItem(MediaItem mediaItem) {
        this.playableItem = mediaItem;
    }

    private void correctOverlayLevel(int margin) {
        if (getActivity() instanceof IOverlayable) {
            // 100 - percent
            // 255 - max alpha
            int alpha = margin * 100 / screenHeight;
            alpha = 255 * alpha / 100;
            alpha = 255 - alpha;
            ((IOverlayable) getActivity()).setOverlayAlpha(alpha);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        onMove(event, true);
        return true;
    }

    @Override
    public void onMove(MotionEvent event, boolean applyMovement) {
        final int Y = (int) event.getRawY();
        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) rlDetailedContent.getLayoutParams();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveDeltaY = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP: {
                if (lParams.topMargin >= bottomScrollBorder) {
                    getActivity().onBackPressed();
                } else {
                    if (!(playableItem instanceof Podcast)) {
                        fbDetailsPlay.setVisibility(View.VISIBLE);
                    }
                    svDetails.setEnabled(false);
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (applyMovement) {
                    int newMargin = Y - moveDeltaY < 0 ? 0 : Y - moveDeltaY;
                    lParams.topMargin = newMargin;
                    lParams.bottomMargin = MAGIC_NUMBER;
                    rlDetailedContent.setLayoutParams(lParams);
                    if (lParams.topMargin <= topScrollBorder) {
                        lParams.bottomMargin = 0;
                        lParams.topMargin = 0;
                        rlDetailedContent.setLayoutParams(lParams);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            viewStatusBar.setVisibility(View.VISIBLE);
                        }
                        svDetails.setEnabled(true);
                    } else {
                        viewStatusBar.setVisibility(View.GONE);
                        fbDetailsPlay.setVisibility(View.INVISIBLE);
                    }
                    correctOverlayLevel(newMargin);
                }
                break;
        }
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }
}
