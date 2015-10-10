package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.BackendManager;
import com.chickenkiller.upods2.controllers.SearchResultsAdapter;
import com.chickenkiller.upods2.controllers.SmallPlayer;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IRequestHandler;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.ServerApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class FragmentSearch extends Fragment implements SearchView.OnQueryTextListener {

    public enum SearchType {RADIO, PODCAST}

    ;

    public static final String TAG = "search_results";

    private RecyclerView rvSearchResults;
    private SmallPlayer smallPlayer;
    private SearchResultsAdapter searchResultsAdapter;
    private ProgressBar pbLoadingSearch;
    private TextView tvSearchNoResults;
    private TextView tvStartTyping;
    private String lastQuery = "";
    private SearchType searchType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init fragments views
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        pbLoadingSearch = (ProgressBar) view.findViewById(R.id.pbLoadingSearch);
        rvSearchResults = (RecyclerView) view.findViewById(R.id.rvSearchResults);
        tvSearchNoResults = (TextView) view.findViewById(R.id.tvSearchNoResults);
        tvStartTyping = (TextView) view.findViewById(R.id.tvSearchStart);
        smallPlayer = new SmallPlayer(view, getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        //Toolbar
        MenuItem searchMenuItem = ((IToolbarHolder) getActivity()).getToolbar().getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.search_results);
        ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.radio_main));

        //Featured adapter
        searchResultsAdapter = new SearchResultsAdapter(getActivity(), R.layout.radio_search_results_item);
        if (getActivity() instanceof IFragmentsManager) {
            searchResultsAdapter.setFragmentsManager((IFragmentsManager) getActivity());
        }

        //Featured recycle view
        rvSearchResults.setAdapter(searchResultsAdapter);
        rvSearchResults.setLayoutManager(layoutManager);
        rvSearchResults.setVisibility(View.INVISIBLE);
        tvStartTyping.setVisibility(View.VISIBLE);
        pbLoadingSearch.setVisibility(View.GONE);
        return view;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    private void loadSearchResults(String query) {
        lastQuery = query;
        rvSearchResults.setVisibility(View.GONE);
        pbLoadingSearch.setVisibility(View.VISIBLE);
        if (searchType == SearchType.RADIO) {
            query = ServerApi.RADIO_SEARCH + query;
        } else {
            query = ServerApi.PODCAST_SEARCH + query + ServerApi.PODCAST_SEARCH_PARAM;
        }
        BackendManager.getInstance().doSearch(query, new IRequestHandler() {
                    @Override
                    public void updateUISuccess(final JSONObject jResponse) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<MediaItem> resultMediaItems = new ArrayList<MediaItem>();
                                    if (searchType == SearchType.RADIO) {
                                        resultMediaItems.addAll(RadioItem.withJsonArray(jResponse.getJSONArray("result"), getActivity()));
                                    }else{
                                        resultMediaItems.addAll(Podcast.withJsonArray(jResponse.getJSONArray("results")));
                                    }
                                    searchResultsAdapter.clear();
                                    searchResultsAdapter.addItems(resultMediaItems);
                                    pbLoadingSearch.setVisibility(View.GONE);
                                    rvSearchResults.setVisibility(View.VISIBLE);
                                    if (resultMediaItems.size() == 0) {
                                        tvSearchNoResults.setVisibility(View.VISIBLE);
                                    }
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
        smallPlayer.destroy();
        BackendManager.getInstance().clearSearchQueue();
        super.onDestroy();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        tvSearchNoResults.setVisibility(View.GONE);
        tvStartTyping.setVisibility(View.GONE);
        if (query.equals(lastQuery) || query.isEmpty()) {
            return false;
        }
        loadSearchResults(query);
        return false;
    }
}
