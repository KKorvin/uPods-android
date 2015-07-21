package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentRadioItemDetails extends Fragment implements View.OnTouchListener {
    private static final int MAGIC_NUMBER = -250; //Don't know what it does
    public static String TAG = "media_details";

    private RadioItem radioItem;

    private RelativeLayout rlDetailedContent;
    private TextView tvDetailedDescription;
    private TextView tvDetailedHeader;
    private ImageView imgDetailedHeader;
    private ImageView imgDetailedTopCover;
    private int moveDeltayY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_details, container, false);
        rlDetailedContent = (RelativeLayout) view.findViewById(R.id.rlDetailedContent);
        tvDetailedDescription = (TextView) view.findViewById(R.id.tvDetailedDescription);
        tvDetailedHeader = (TextView) view.findViewById(R.id.tvDetailedHeader);
        imgDetailedHeader = (ImageView) view.findViewById(R.id.imgDetailedHeader);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);
        moveDeltayY = 0;

        if (radioItem != null) {
            Glide.with(getActivity()).load("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_4.png").centerCrop().crossFade().into(imgDetailedHeader);
            Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).centerCrop().crossFade().into(imgDetailedTopCover);
            tvDetailedHeader.setText(radioItem.getName());
            tvDetailedDescription.setText(radioItem.getDescription());
        }
        rlDetailedContent.setOnTouchListener(this);
        return view;
    }

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int Y = (int) event.getRawY();
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) rlDetailedContent.getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveDeltayY = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                lParams.topMargin = Y - moveDeltayY;
                lParams.bottomMargin = MAGIC_NUMBER;
                view.setLayoutParams(lParams);
                break;
        }
        return true;
    }


}
