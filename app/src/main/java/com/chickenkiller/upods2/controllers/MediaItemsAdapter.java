package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.views.ImageViewSquare;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends ArrayAdapter<MediaItem> {

    private Context mContext;
    private int layoutId;
    private ArrayList<MediaItem> allItems;


    public MediaItemsAdapter(Context context, int layoutId, ArrayList<MediaItem> allItems) {
        super(context, layoutId, allItems);
        this.mContext = context;
        this.layoutId = layoutId;
        this.allItems = allItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        MediaItem currentItem = getItem(position);
        if (itemView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(layoutId, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imgSquare = (ImageViewSquare) itemView.findViewById(R.id.imgSquare);
            viewHolder.tvSquareTitle = (TextView) itemView.findViewById(R.id.tvSquareTitle);
            itemView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) itemView.getTag();
        //holder.imgSquare.setImageResource(R.drawable.abc_list_pressed_holo_light);
        Glide.with(mContext).load(currentItem.getImageUrl()).centerCrop().crossFade().into(holder.imgSquare);
        holder.tvSquareTitle.setText(currentItem.getName());

        return itemView;
    }

    @Override
    public int getCount() {
        return allItems.size();
    }

    private class ViewHolder {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;
    }
}
