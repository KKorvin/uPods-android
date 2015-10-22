package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.BackendManager;
import com.chickenkiller.upods2.controllers.EpisodsXMLHandler;
import com.chickenkiller.upods2.controllers.ProfileManager;
import com.chickenkiller.upods2.controllers.TracksAdapter;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IMovable;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.interfaces.OnActionFinished;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.ContextMenuType;
import com.chickenkiller.upods2.utils.UIHelper;
import com.chickenkiller.upods2.views.DetailsScrollView;

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
    private static final float TOP_SCROLL_BORDER_PERCENT = 1f;
    private static final float COVER_SCALE_FACTOR = 2f;
    private static int bottomScrollBorder;
    private static int topScrollBorder;
    public static String TAG = "media_details";

    private IPlayableMediaItem playableItem;

    private RelativeLayout rlDetailedContent;
    private DetailsScrollView svDetails;
    private RecyclerView rvTracks;
    private TextView tvDetailedDescription;
    private TextView tvDetailedSubHeader;
    private TextView tvDetailedHeader;
    private TextView tvBottomHeader;
    private TextView tvDetailedDesHeader;
    private View viewDetailedHeader;
    private Button btnSubscribe;
    private ImageView imgDetailedTopCover;
    private ImageView imgBluredCover;
    private ImageView imgMediaMore;
    private FloatingActionButton fbDetailsPlay;
    private TracksAdapter tracksAdapter;
    private ProgressBar pbTracks;
    private int moveDeltaY;
    private int screenHeight;

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
        rlDetailedContent = (RelativeLayout) view.findViewById(R.id.rlDetailedContent);
        tvDetailedDescription = (TextView) view.findViewById(R.id.tvDetailedDescription);
        tvDetailedHeader = (TextView) view.findViewById(R.id.tvDetailedHeader);
        tvDetailedSubHeader = (TextView) view.findViewById(R.id.tvDetailedSubHeader);
        tvDetailedDesHeader = (TextView) view.findViewById(R.id.tvDetailedDesHeader);
        tvBottomHeader = (TextView) view.findViewById(R.id.tvDetailedBottomHeader);
        viewDetailedHeader = view.findViewById(R.id.viewDetailedHeader);
        btnSubscribe = (Button) view.findViewById(R.id.btnSubscribe);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);
        imgBluredCover = (ImageView) view.findViewById(R.id.imgBluredCover);
        imgMediaMore = (ImageView) view.findViewById(R.id.imgMediaMore);
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
            btnSubscribe.setText(ProfileManager.getInstance().isSubscribedToMediaItem((Podcast) playableItem) ? getString(R.string.unsubscribe) : getString(R.string.subscribe));
            btnSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ProfileManager.getInstance().isSubscribedToMediaItem((Podcast) playableItem)) {
                        ProfileManager.getInstance().removeSubscribedMediaItem((Podcast) playableItem);
                        btnSubscribe.setText(getString(R.string.subscribe));
                    } else {
                        ProfileManager.getInstance().addSubscribedMediaItem((Podcast) playableItem);
                        btnSubscribe.setText(getString(R.string.unsubscribe));
                    }
                }
            });

            imgMediaMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IContextMenuManager) getActivity()).openContextMenu(v, ContextMenuType.PODCAST_MIDDLE_SCREEN, (Podcast) playableItem, new OnActionFinished() {
                        @Override
                        public void onActionFinished() {
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
        svDetails.setVisibility(View.VISIBLE);
        svDetails.setEnabled(false);
        svDetails.setIMovable(this);
        btnSubscribe.setVisibility(View.GONE);
        tvDetailedDescription.setText(playableItem.getDescription());
        fbDetailsPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), ActivityPlayer.class);
                myIntent.putExtra(ActivityPlayer.MEDIA_ITEM_EXTRA, playableItem);
                getActivity().startActivity(myIntent);
                getActivity().finish();
            }
        });
    }

    /**
     * Init colors and settings of header images
     */
    private void initImagesColors() {
        Glide.with(getActivity()).load(playableItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgDetailedTopCover) {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                super.onResourceReady(drawable, anim);
                Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                int dominantColor = UIHelper.getDominantColor(bitmap);
                viewDetailedHeader.setBackgroundColor(dominantColor);
                //viewDetailsDevider.setBackgroundColor(dominantColor);
                tvDetailedDesHeader.setTextColor(dominantColor);
                imgBluredCover.setImageBitmap(UIHelper.createScaledBitmap(bitmap, COVER_SCALE_FACTOR));
            }
        });
    }

    private void initFragmentScrollConstants() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        bottomScrollBorder = screenHeight - (int) (screenHeight * BOTTOM_SCROLL_BORDER_PERCENT);
        topScrollBorder = screenHeight - (int) (screenHeight * TOP_SCROLL_BORDER_PERCENT);
    }

    private void loadTracks() {
        BackendManager.getInstance().sendRequest(((ITrackable) playableItem).getTracksFeed(), new ISimpleRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final String response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SAXParserFactory spf = SAXParserFactory.newInstance();
                                    SAXParser sp = spf.newSAXParser();
                                    XMLReader xr = sp.getXMLReader();
                                    EpisodsXMLHandler episodsXMLHandler = new EpisodsXMLHandler();
                                    xr.setContentHandler(episodsXMLHandler);
                                    //TODO could be encoding problem
                                    InputSource inputSource = new InputSource(new StringReader(response));
                                    xr.parse(inputSource);
                                    ArrayList<Episod> parsedEpisods = episodsXMLHandler.getParsedEpisods();
                                    if (playableItem instanceof ITrackable) {
                                        ((ITrackable) playableItem).setTracks(parsedEpisods);
                                    }
                                    if (playableItem instanceof Podcast) {
                                        ((Podcast) playableItem).setDescription(episodsXMLHandler.getPodcastSummary());
                                    }
                                    tracksAdapter.addItems(parsedEpisods);
                                    rvTracks.setVisibility(View.VISIBLE);
                                    pbTracks.setVisibility(View.GONE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed() {

                    }
                }

        );

    }


    public void setPlayableItem(IPlayableMediaItem mediaItem) {
        this.playableItem = mediaItem;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        onMove(event, true);
        return true;
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
    public void onMove(MotionEvent event, boolean applyMovement) {
        final int Y = (int) event.getRawY();
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) rlDetailedContent.getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveDeltaY = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP: {
                if (lParams.topMargin >= bottomScrollBorder) {
                    getActivity().onBackPressed();
                } else if (lParams.topMargin <= topScrollBorder) {
                    lParams.topMargin = 0;
                    lParams.bottomMargin = 0;
                    rlDetailedContent.setLayoutParams(lParams);
                    svDetails.setEnabled(true);
                } else {
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
                    correctOverlayLevel(newMargin);
                }
                break;
        }
    }
}
