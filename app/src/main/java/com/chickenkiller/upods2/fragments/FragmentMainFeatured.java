package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.adaperts.CategoriesAdapter;
import com.chickenkiller.upods2.controllers.adaperts.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IMediaItemView;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.Category;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.RoundedButtonsLayoutItem;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.views.AutofitRecyclerView;
import com.chickenkiller.upods2.views.GridSpacingItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentMainFeatured extends Fragment implements IContentLoadListener, AdapterView.OnItemClickListener, ICustumziedBackPress {

    public static final String TAG = "main_featured";
    public static final int MEDIA_ITEMS_CARDS_MARGIN = 25;
    public static final int MEDIA_ITEMS_TYPES_COUNT = 4;
    private static final int RADIO_BUTTON_MODE_MEDIA_ITEMS_TYPES_COUNT = 2;

    private AutofitRecyclerView rvMain;
    private GridSpacingItemDecoration gridSpacingItemDecoration;
    private ListView lvMain;
    private CategoriesAdapter categoriesAdapter;
    private MediaItemsAdapter mediaItemsAdapter;
    private ProgressBar pbLoadingFeatured;
    private View listViewHeader;
    private LinearLayout lnInternetError;

    private int currentRoundBtnMode;


    private IOperationFinishWithDataCallback iRoundButtonClicked = new IOperationFinishWithDataCallback() {
        @Override
        public void operationFinished(Object data) {
            int btnClicked = (int) data;
            currentRoundBtnMode = btnClicked;
            rvMain.setVisibility(View.INVISIBLE);
            pbLoadingFeatured.setVisibility(View.VISIBLE);
            loadListView();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.VISIBLE);

        //Init fragments views
        View view = inflater.inflate(R.layout.fragment_main_featured, container, false);
        lnInternetError = (LinearLayout) view.findViewById(R.id.lnInternetError);
        pbLoadingFeatured = (ProgressBar) view.findViewById(R.id.pbLoadingFeatured);
        rvMain = (AutofitRecyclerView) view.findViewById(R.id.rvMain);
        lvMain = (ListView) view.findViewById(R.id.lvMain);
        lvMain.setOnItemClickListener(this);

        //Toolbar
        if (getActivity() instanceof IToolbarHolder) {
            MenuItem searchMenuItem = ((IToolbarHolder) getActivity()).getToolbar().getMenu().findItem(R.id.action_search);
            searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    FragmentSearch fragmentSearch = new FragmentSearch();
                    fragmentSearch.setSearchType(MediaItemType.RADIO);
                    ((IFragmentsManager) getActivity()).showFragment(R.id.fl_content, fragmentSearch, FragmentSearch.TAG);
                    return false;
                }
            });
        }

        //Featured adapter
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_vertical,
                R.layout.media_item_title, RadioItem.withOnlyBannersHeader());
        if (getActivity() instanceof IFragmentsManager) {
            mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }
        mediaItemsAdapter.setRoundedButtonsLayout(R.layout.rounded_buttons_item);
        mediaItemsAdapter.setContentLoadListener(this);
        mediaItemsAdapter.setiRoundButtonClicked(iRoundButtonClicked);

        //Featured recycle view
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                if (currentRoundBtnMode > 0) {
                    return viewType != MediaItemsAdapter.HEADER ? 1 : rvMain.getSpanCount();
                } else {
                    return (viewType != MediaItemsAdapter.HEADER && viewType != MediaItemsAdapter.BANNERS_LAYOUT
                            && viewType != MediaItemsAdapter.ROUNDED_BUTTONS) ? 1 : rvMain.getSpanCount();
                }
            }
        });
        rvMain.setVisibility(View.INVISIBLE);

        //Load tops from remote server
        showTops();

        //Open search fragment backed from other activity which was started from search
        if (getActivity().getIntent().hasExtra(ActivityPlayer.ACTIVITY_STARTED_FROM_IN_DEPTH)) {
            FragmentSearch.openFromIntent(getActivity());
        }

        currentRoundBtnMode = -1;

        return view;
    }

    private void loadListView() {
        BackendManager.getInstance().sendRequest(getCurrentRoundBtnModeListUrl(), new IRequestCallback() {
            @Override
            public void onRequestSuccessed(final JSONObject jResponse) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Category> categories = null;
                        try {
                            categories = Category.getCategoriesListFromJSON(jResponse.getJSONArray("result").toString());
                            categoriesAdapter = new CategoriesAdapter(getActivity(), R.layout.category_item, categories);

                            if (listViewHeader != null && lvMain.getHeaderViewsCount() > 0) {
                                lvMain.removeHeaderView(listViewHeader);
                            }

                            LayoutInflater Li = LayoutInflater.from(getActivity());
                            listViewHeader = Li.inflate(R.layout.category_item_header, null);
                            ((TextView) listViewHeader.findViewById(R.id.tvCategoriesHeader)).setText(getCurrentRoundBtnModeTitle());

                            pbLoadingFeatured.setVisibility(View.GONE);
                            lvMain.setVisibility(View.VISIBLE);
                            lvMain.addHeaderView(listViewHeader, null, false);
                            lvMain.setAdapter(categoriesAdapter);
                        } catch (JSONException e) {
                            Logger.printError(TAG, "Can't load listview with categories (from round btn)");
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onRequestFailed() {
                showNoInternetView();
            }
        });
    }

    private void showTops() {
        Logger.printInfo("PERFORMANCE", "showTops2");
        BackendManager.getInstance().loadTops(BackendManager.TopType.MAIN_FEATURED, ServerApi.RADIO_TOP, new IRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final JSONObject jResponse) {
                        try {
                            final ArrayList<IMediaItemView> topRadioStations = new ArrayList<IMediaItemView>();
                            ArrayList<RadioItem> topItems = RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity());
                            RadioItem.syncWithDb(topItems);
                            MediaItemTitle mediaItemTitle = new MediaItemTitle(getString(R.string.top40_chanels), getString(R.string.top40_chanels_subheader));
                            mediaItemTitle.showButton = true;
                            RoundedButtonsLayoutItem roundedButtonsLayoutItem = new RoundedButtonsLayoutItem();
                            topRadioStations.add(roundedButtonsLayoutItem);
                            topRadioStations.add(mediaItemTitle);
                            topRadioStations.addAll(topItems);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Logger.printInfo("PERFORMANCE", "showTops3");
                                    updateMediaItems(topRadioStations);
                                    Logger.printInfo("PERFORMANCE", "showTops4");
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onRequestFailed() {
                        showNoInternetView();
                    }

                }
        );
        Logger.printInfo("PERFORMANCE", "showTops1");
    }

    public void notifyMediaItemChanges(ProfileManager.ProfileUpdateEvent profileUpdateEvent) {
        if (mediaItemsAdapter != null && profileUpdateEvent.mediaItem instanceof Podcast) {
            mediaItemsAdapter.updateMediaItem(profileUpdateEvent.mediaItem);
        }
    }

    private void updateMediaItems(ArrayList<IMediaItemView> radioStations) {
        mediaItemsAdapter.addItems(radioStations);
        mediaItemsAdapter.notifyContentLoadingStatus();
        rvMain.post(new Runnable() {
            @Override
            public void run() {
                if (gridSpacingItemDecoration != null) {
                    rvMain.removeItemDecoration(gridSpacingItemDecoration);
                }
                gridSpacingItemDecoration = new GridSpacingItemDecoration(rvMain.getSpanCount(), MEDIA_ITEMS_CARDS_MARGIN, true);
                gridSpacingItemDecoration.setGridItemType(MediaItemsAdapter.ITEM);
                gridSpacingItemDecoration.setItemsTypesCount(currentRoundBtnMode > 0 ? RADIO_BUTTON_MODE_MEDIA_ITEMS_TYPES_COUNT : MEDIA_ITEMS_TYPES_COUNT);
                rvMain.addItemDecoration(gridSpacingItemDecoration);
            }
        });
    }

    private void loadMediaItems(final Category category) {
        BackendManager.getInstance().sendRequest(getCurrentRoundBtnModeMediasUrl(category), new IRequestCallback() {
            @Override
            public void onRequestSuccessed(final JSONObject jResponse) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<RadioItem> radioItems = RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity());
                            RadioItem.syncWithDb(radioItems);
                            ArrayList<IMediaItemView> radiosStations = new ArrayList<IMediaItemView>();
                            MediaItemTitle mediaItemTitle = new MediaItemTitle(category.getName());
                            mediaItemTitle.showButton = false;
                            radiosStations.add(mediaItemTitle);
                            radiosStations.addAll(radioItems);
                            updateMediaItems(radiosStations);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onRequestFailed() {
                showNoInternetView();
            }
        });
    }

    private String getCurrentRoundBtnModeTitle() {
        switch (currentRoundBtnMode) {
            case RoundedButtonsLayoutItem.ROUND_BTN_COUNTRIES:
                return getString(R.string.countries);
            case RoundedButtonsLayoutItem.ROUND_BTN_GENRES:
                return getString(R.string.genres);
            case RoundedButtonsLayoutItem.ROUND_BTN_LAGUAGES:
                return getString(R.string.languages);
            default:
                return "";
        }
    }

    private String getCurrentRoundBtnModeListUrl() {
        switch (currentRoundBtnMode) {
            case RoundedButtonsLayoutItem.ROUND_BTN_COUNTRIES:
                return ServerApi.RADIO_COUNTRIES;
            case RoundedButtonsLayoutItem.ROUND_BTN_GENRES:
                return ServerApi.RADIO_CATEGORIES;
            case RoundedButtonsLayoutItem.ROUND_BTN_LAGUAGES:
                return ServerApi.RADIO_LANGUAGES;
            default:
                return "";
        }
    }

    private String getCurrentRoundBtnModeMediasUrl(Category category) {
        switch (currentRoundBtnMode) {
            case RoundedButtonsLayoutItem.ROUND_BTN_COUNTRIES:
                return ServerApi.RADIO_BY_COUNTRY + category.getName();
            case RoundedButtonsLayoutItem.ROUND_BTN_GENRES:
                return ServerApi.RADIO_BY_CATEGORIES + category.getId();
            case RoundedButtonsLayoutItem.ROUND_BTN_LAGUAGES:
                return ServerApi.RADIO_BY_LANGUAGE + category.getName();
            default:
                return "";
        }
    }

    private void showNoInternetView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbLoadingFeatured.setVisibility(View.GONE);
                rvMain.setVisibility(View.GONE);
                lvMain.setVisibility(View.GONE);
                lnInternetError.setVisibility(View.VISIBLE);
            }
        });
    }

    public void notifyDataChanged() {
        if (mediaItemsAdapter != null) {
            mediaItemsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onContentLoaded() {
        pbLoadingFeatured.setVisibility(View.GONE);
        rvMain.setVisibility(View.VISIBLE);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Category category = (Category) lvMain.getItemAtPosition(position);
        mediaItemsAdapter.clearItems();
        mediaItemsAdapter.clearCurrentContentLevel();
        mediaItemsAdapter.notifyContentLoadingStatus();
        lvMain.setVisibility(View.GONE);
        pbLoadingFeatured.setVisibility(View.VISIBLE);
        loadMediaItems(category);

    }

    @Override
    public boolean onBackPressed() {
        if (currentRoundBtnMode > 0 && rvMain.getVisibility() == View.VISIBLE) {//In button mode + media items are visiable
            rvMain.setVisibility(View.INVISIBLE);
            pbLoadingFeatured.setVisibility(View.VISIBLE);
            loadListView();
            return false;
        } else if (currentRoundBtnMode > 0) {
            currentRoundBtnMode = -1;
            lvMain.setVisibility(View.GONE);
            mediaItemsAdapter.clearCurrentContentLevel();
            mediaItemsAdapter.notifyContentLoadingStatus();
            mediaItemsAdapter.clearItems();
            mediaItemsAdapter.addItems(RadioItem.withOnlyBannersHeader());
            pbLoadingFeatured.setVisibility(View.VISIBLE);
            showTops();
            return false;
        }
        return true;
    }
}
