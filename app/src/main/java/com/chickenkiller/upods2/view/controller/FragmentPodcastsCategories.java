package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.CategoriesAdapter;
import com.chickenkiller.upods2.models.Category;

import java.util.List;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentPodcastsCategories extends Fragment {

    public static final String TAG = "fragment_podcasts_categories";
    private ListView lvCategories;
    private CategoriesAdapter categoriesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        LayoutInflater Li = LayoutInflater.from(getActivity());
        View categoriesHeader = Li.inflate(R.layout.category_item_header, null);
        ((TextView) categoriesHeader.findViewById(R.id.tvCategoriesHeader)).setText(getString(R.string.categories));
        lvCategories = (ListView) view.findViewById(R.id.lvCategories);
        List<Category> categories = Category.getPodcastsCategoriesList();
        categoriesAdapter = new CategoriesAdapter(getActivity(), R.layout.category_item, categories);
        lvCategories.addHeaderView(categoriesHeader);
        lvCategories.setAdapter(categoriesAdapter);
        return view;
    }

}
