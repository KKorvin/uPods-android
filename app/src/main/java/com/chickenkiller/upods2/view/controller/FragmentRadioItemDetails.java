package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentRadioItemDetails extends Fragment {
    public static String TAG = "media_details";

    private RadioItem radioItem;

    private LinearLayout lnDetailedTransparentHeader;
    private TextView tvDetailedDescription;
    private TextView tvDetailedHeader;
    private ImageView imgDetailedHeader;
    private ImageView imgDetailedTopCover;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_details, container, false);
        lnDetailedTransparentHeader = (LinearLayout) view.findViewById(R.id.lnDetailedTransparentHeader);
        tvDetailedDescription = (TextView) view.findViewById(R.id.tvDetailedDescription);
        tvDetailedHeader = (TextView) view.findViewById(R.id.tvDetailedHeader);
        imgDetailedHeader = (ImageView) view.findViewById(R.id.imgDetailedHeader);
        imgDetailedTopCover = (ImageView) view.findViewById(R.id.imgDetailedCover);


        if (radioItem != null) {
            Glide.with(getActivity()).load("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_4.png").centerCrop().crossFade().into(imgDetailedHeader);
            Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).centerCrop().crossFade().into(imgDetailedTopCover);
            tvDetailedHeader.setText(radioItem.getName());
            tvDetailedDescription.setText(radioItem.getDescription());
        }

        return view;
    }

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
    }
}
