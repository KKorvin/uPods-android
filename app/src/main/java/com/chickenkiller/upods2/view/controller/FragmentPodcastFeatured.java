package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.BackendManager;
import com.chickenkiller.upods2.controllers.GridSpacingItemDecoration;
import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.ServerApi;
import com.chickenkiller.upods2.views.AutofitRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentPodcastFeatured extends Fragment implements IContentLoadListener {

    public static final String TAG = "podcasts_featured";
    public static final int MEDIA_ITEMS_TYPES_COUNT = 2;

    private AutofitRecyclerView rvMain;
    private MediaItemsAdapter mediaItemsAdapter;
    private ProgressBar pbLoadingFeatured;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init fragments views
        View view = inflater.inflate(R.layout.fragment_podcasts_featured, container, false);
        pbLoadingFeatured = (ProgressBar) view.findViewById(R.id.pbLoadingFeatured);
        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);

        //Featured adapter
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item,
                R.layout.media_item_title);
        if (getActivity() instanceof IFragmentsManager) {
            mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }
        mediaItemsAdapter.setContentLoadListener(this);
        //Featured recycle view
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                return (viewType != MediaItemsAdapter.HEADER && viewType != MediaItemsAdapter.BANNERS_LAYOUT) ?
                        1 : rvMain.getSpanCount();
            }
        });
        mediaItemsAdapter.notifyContentLoadingStatus();
        rvMain.setVisibility(View.INVISIBLE);

        //Load tops from remote server
        showTops();

        return view;
    }

    private void showTops() {
        BackendManager.getInstance().loadTops(BackendManager.TopType.MAIN_PODCAST, ServerApi.PODCASTS_TOP, new INetworkUIupdater() {
                    @Override
                    public void updateUISuccess(final JSONObject jResponse) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<MediaItem> topPodcasts = new ArrayList<MediaItem>();
                                    topPodcasts.add(new MediaItemTitle(getString(R.string.top40_podcasts), getString(R.string.top40_chanels_subheader)));
                                    topPodcasts.addAll(Podcast.withJsonArray(jResponse.getJSONArray("result")));
                                    mediaItemsAdapter.addItems(topPodcasts);
                                    GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(rvMain.getSpanCount(), FragmentMainFeatured.MEDIA_ITEMS_CARDS_MARGIN, true);
                                    gridSpacingItemDecoration.setGridItemType(MediaItemsAdapter.ITEM);
                                    gridSpacingItemDecoration.setItemsTypesCount(MEDIA_ITEMS_TYPES_COUNT);
                                    rvMain.addItemDecoration(gridSpacingItemDecoration);
                                    mediaItemsAdapter.notifyContentLoadingStatus();
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

    @Override
    public void onDestroy() {
        mediaItemsAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onContentLoaded() {
        pbLoadingFeatured.setVisibility(View.GONE);
        rvMain.setVisibility(View.VISIBLE);
    }

}
