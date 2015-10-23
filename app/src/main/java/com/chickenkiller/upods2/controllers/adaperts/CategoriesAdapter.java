package com.chickenkiller.upods2.controllers.adaperts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.Category;

import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class CategoriesAdapter extends ArrayAdapter<Category> {

    private int layaoutId;

    private static class ViewHolder {
        public TextView tvCategoryName;
    }

    public CategoriesAdapter(Context context, int layaoutId, List<Category> categories) {
        super(context, layaoutId, categories);
        this.layaoutId = layaoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Category category = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layaoutId, parent, false);
            viewHolder.tvCategoryName = (TextView) convertView.findViewById(R.id.tvCategoryName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvCategoryName.setText(category.getName());
        return convertView;
    }
}
