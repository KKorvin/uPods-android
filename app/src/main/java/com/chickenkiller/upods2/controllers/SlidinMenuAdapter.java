package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.SlidingMenuItem;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidinMenuAdapter extends ArrayAdapter<SlidingMenuItem> {

    private int layoutId;

    public SlidinMenuAdapter(Context context, ArrayList<SlidingMenuItem> slidingMenuItems) {
        super(context, R.layout.sliding_menu_item, slidingMenuItems);
        this.layoutId = R.layout.sliding_menu_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SlidingMenuItem slidingMenuItem = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutId, parent, false);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvSMenuTitle);
            viewHolder.imgIcon = (ImageView) convertView.findViewById(R.id.imgSMenutIcon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(slidingMenuItem.getTitle());
        viewHolder.imgIcon.setImageResource(slidingMenuItem.getIconId());

        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView tvTitle;
        ImageView imgIcon;
    }
}
