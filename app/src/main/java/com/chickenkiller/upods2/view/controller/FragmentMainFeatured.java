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
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.AutofitRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentMainFeatured extends Fragment {

    public static final String TAG = "main_featured";
    public final int MEDIA_ITEMS_CARDS_MARGIN = 25;
    public static final int MEDIA_ITEMS_TYPES_COUNT = 3;


    private AutofitRecyclerView rvMain;
    private MediaItemsAdapter mediaItemsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_featured, container, false);

        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item,
                R.layout.media_item_title, RadioItem.withOnlyBannersHeader());
        if (getActivity() instanceof IFragmentsManager) {
            mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                return (viewType != MediaItemsAdapter.HEADER && viewType != MediaItemsAdapter.BANNERS_LAYOUT) ?
                        1 : rvMain.getSpanCount();
            }
        });
        showTops();
        return view;
    }

    private void showTops() {
        RadioTopManager.getInstance().loadTops(RadioTopManager.TopType.MAIN_FEATURED, new INetworkUIupdater() {
                    @Override
                    public void updateUISuccess(final JSONObject jResponse) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<MediaItem> topRadioStations = new ArrayList<MediaItem>();
                                    topRadioStations.add(new MediaItemTitle(getString(R.string.top40_chanels), getString(R.string.top40_chanels_subheader)));
                                    topRadioStations.addAll(RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity()));
                                    mediaItemsAdapter.addItems(topRadioStations);
                                    GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(rvMain.getSpanCount(), MEDIA_ITEMS_CARDS_MARGIN, true);
                                    gridSpacingItemDecoration.setGridItemType(MediaItemsAdapter.ITEM);
                                    gridSpacingItemDecoration.setItemsTypesCount(MEDIA_ITEMS_TYPES_COUNT);
                                    rvMain.addItemDecoration(gridSpacingItemDecoration);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void updateUIFailed() {

                    }

                }
        );
    }

}
