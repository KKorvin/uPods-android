package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.GridSpacingItemDecoration;
import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.RadioTopManager;
import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.AutofitRecyclerView;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentMainFeatured extends Fragment {

    private AutofitRecyclerView rvMain;
    private MediaItemsAdapter mediaItemsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_featured, container, false);

        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        rvMain.addItemDecoration(new GridSpacingItemDecoration(3, 25, true));

        showTops();

        return view;
    }

    private void showTops() {
        RadioTopManager.getInstance().loadTops(RadioTopManager.TopType.MAIN_FEATURED, new INetworkUIupdater() {
            @Override
            public void updateUISuccess(final Response response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jResponse = new JSONObject(response.body().string());
                            ArrayList<MediaItem> topRadioStations = RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity());
                            mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item,
                                    R.layout.media_item_title, topRadioStations);
                            rvMain.setAdapter(mediaItemsAdapter);
                            rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                @Override
                                public int getSpanSize(int position) {
                                    int viewType = mediaItemsAdapter.getItemViewType(position);
                                    return (viewType != MediaItemsAdapter.HEADER && viewType != MediaItemsAdapter.BANNERS_LAYOUT) ?
                                            1 : rvMain.getSpanCount();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void updateUIFailed() {

            }

        });
    }

}
