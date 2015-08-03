package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.chickenkiller.upods2.interfaces.IMovable;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.UIHelper;
import com.chickenkiller.upods2.views.DetailsScrollView;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentRadioItemDetails extends Fragment implements View.OnTouchListener, IMovable {
    private static final int MAGIC_NUMBER = -250; //Don't know what it does
    private static final float BOTTOM_SCROLL_BORDER_PERCENT = 0.35f;
    private static final float TOP_SCROLL_BORDER_PERCENT = 1f;
    private static final float COVER_SCALE_FACTOR = 2f;
    private static int bottomScrollBorder;
    private static int topScrollBorder;
    public static String TAG = "media_details";

    private RadioItem radioItem;

    private RelativeLayout rlDetailedContent;
    private DetailsScrollView svDetails;
    private TextView tvDetailedDescription;
    private TextView tvDetailedHeader;
    private TextView tvDetailedDesHeader;
    private View viewDetailedHeader;
    private View viewDetailsDevider;
    private ImageView imgDetailedTopCover;
    private ImageView imgBluredCover;
    private FloatingActionButton fbDetailsPlay;
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
        tvDetailedDesHeader = (TextView) view.findViewById(R.id.tvDetailedDesHeader);
        viewDetailedHeader = view.findViewById(R.id.viewDetailedHeader);
        viewDetailsDevider = view.findViewById(R.id.vDetailsDevider);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);
        imgBluredCover = (ImageView) view.findViewById(R.id.imgBluredCover);
        fbDetailsPlay = (FloatingActionButton) view.findViewById(R.id.fbDetailsPlay);
        svDetails = (DetailsScrollView) view.findViewById(R.id.svDetails);
        svDetails.setEnabled(false);
        svDetails.setIMovable(this);
        moveDeltaY = 0;

        if (radioItem != null) {
            initImagesColors();
            tvDetailedHeader.setText(radioItem.getName());
            tvDetailedDescription.setText(radioItem.getDescription());
        }
        rlDetailedContent.setOnTouchListener(this);
        view.setOnClickListener(frgamentCloseClickListener);
        initFragmentScrollConstants();

        fbDetailsPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), ActivityPlayer.class);
                myIntent.putExtra(ActivityPlayer.RADIO_ITEM_EXTRA, radioItem);
                getActivity().startActivity(myIntent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void initImagesColors() {
        Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).centerCrop().crossFade().into(new GlideDrawableImageViewTarget(imgDetailedTopCover) {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                super.onResourceReady(drawable, anim);
                Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                int dominantColor = UIHelper.getDominantColor(bitmap);
                viewDetailedHeader.setBackgroundColor(dominantColor);
                viewDetailsDevider.setBackgroundColor(dominantColor);
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

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
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
