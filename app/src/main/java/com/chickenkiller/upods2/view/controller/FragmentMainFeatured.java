package com.chickenkiller.upods2.view.controller;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.BanerItemsAdapter;
import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.models.BanerItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.AutofitRecyclerView;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentMainFeatured extends Fragment {

    private AutofitRecyclerView rvMain;
    private RecyclerView rvBanners;
    private MediaItemsAdapter mediaItemsAdapter;
    private BanerItemsAdapter banerItemsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_featured, container, false);
        Activity mActivity = getActivity();
        mediaItemsAdapter = new MediaItemsAdapter(mActivity, R.layout.card_media_item,
                R.layout.media_item_title, RadioItem.generateDebugList(40, mActivity));
        banerItemsAdapter = new BanerItemsAdapter(mActivity, R.layout.baner_item, BanerItem.generateDebugList(1));

        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                return viewType != MediaItemsAdapter.HEADER ? 1 : rvMain.getSpanCount();
            }
        });

        rvBanners = (RecyclerView) view.findViewById(R.id.rvBanners);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        rvBanners.setLayoutManager(layoutManager);
        rvBanners.setHasFixedSize(true);
        rvBanners.setAdapter(banerItemsAdapter);

        layoutManager.scrollToPosition(banerItemsAdapter.MIDDLE);
        return view;
    }
}
