package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.CategoriesAdapter;
import com.chickenkiller.upods2.controllers.adaperts.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.models.Category;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.utils.ServerApi;
import com.chickenkiller.upods2.views.AutofitRecyclerView;
import com.chickenkiller.upods2.views.GridSpacingItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Can show category UI (Choose category + list of media items per category) for any media items
 */
public class FragmentMediaItemsCategories extends Fragment implements AdapterView.OnItemClickListener, ICustumziedBackPress {

    public static final String TAG;
    public static final int MEDIA_ITEMS_TYPES_COUNT = 2;

    private static final String LOG_TAG = "fragment_categories";
    private ListView lvCategories;
    private CategoriesAdapter categoriesAdapter;
    private ProgressBar pbLoadingMediaItems;
    private AutofitRecyclerView rvMain;
    private MediaItemsAdapter mediaItemsAdapter;
    private GridSpacingItemDecoration gridSpacingItemDecoration;

    private MediaItemType mediaItemType;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_media_items_categories" + String.valueOf(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        if (mediaItemType == null) {
            mediaItemType = MediaItemType.PODCAST;
        }

        //Listview categories
        LayoutInflater Li = LayoutInflater.from(getActivity());
        View categoriesHeader = Li.inflate(R.layout.category_item_header, null);
        ((TextView) categoriesHeader.findViewById(R.id.tvCategoriesHeader)).setText(getString(R.string.categories));

        pbLoadingMediaItems = (ProgressBar) view.findViewById(R.id.pbLoadingMediaItems);
        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        lvCategories = (ListView) view.findViewById(R.id.lvCategories);

        List<Category> categories = null;

        if (mediaItemType == MediaItemType.PODCAST) {
            categories = Category.getPodcastsCategoriesList();
        }

        categoriesAdapter = new CategoriesAdapter(getActivity(), R.layout.category_item, categories);
        lvCategories.addHeaderView(categoriesHeader, null, false);
        lvCategories.setHeaderDividersEnabled(false);
        lvCategories.setAdapter(categoriesAdapter);
        lvCategories.setOnItemClickListener(this);

        //Media items adapter
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_vertical,
                R.layout.media_item_title);
        if (getActivity() instanceof IFragmentsManager) {
            mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }

        //Recycle view
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
        rvMain.setVisibility(View.INVISIBLE);

        return view;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }


    private void loadMediaItems(final Category category) {
        lvCategories.setVisibility(View.GONE);
        pbLoadingMediaItems.setVisibility(View.VISIBLE);
        BackendManager.getInstance().sendRequest(ServerApi.PODCASTS_BY_CATEGORY + String.valueOf(category.getId()), new IRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final JSONObject jResponse) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<MediaItem> podcastsByCategory = new ArrayList<MediaItem>();
                                    MediaItemTitle mediaItemTitle = new MediaItemTitle(category.getName());
                                    mediaItemTitle.showButton = false;
                                    podcastsByCategory.add(mediaItemTitle);
                                    podcastsByCategory.addAll(Podcast.withJsonArray(jResponse.getJSONArray("result")));

                                    mediaItemsAdapter.clearItems();
                                    mediaItemsAdapter.addItems(podcastsByCategory);

                                    if (gridSpacingItemDecoration != null) {
                                        rvMain.removeItemDecoration(gridSpacingItemDecoration);
                                    }
                                    gridSpacingItemDecoration = new GridSpacingItemDecoration(rvMain.getSpanCount(), FragmentMainFeatured.MEDIA_ITEMS_CARDS_MARGIN, true);
                                    gridSpacingItemDecoration.setGridItemType(MediaItemsAdapter.ITEM);
                                    gridSpacingItemDecoration.setItemsTypesCount(MEDIA_ITEMS_TYPES_COUNT);
                                    rvMain.addItemDecoration(gridSpacingItemDecoration);

                                    pbLoadingMediaItems.setVisibility(View.GONE);
                                    rvMain.setVisibility(View.VISIBLE);
                                    Logger.printInfo(LOG_TAG, "Loaded " + podcastsByCategory.size() + "podcasts for " + category.getName());
                                } catch (JSONException e) {
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


    @Override
    public void onDestroy() {
        mediaItemsAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (rvMain.getVisibility() == View.VISIBLE || pbLoadingMediaItems.getVisibility() == View.VISIBLE) {
            pbLoadingMediaItems.setVisibility(View.GONE);
            rvMain.setVisibility(View.INVISIBLE);
            lvCategories.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Category category = (Category) lvCategories.getItemAtPosition(position);
        loadMediaItems(category);
    }
}
