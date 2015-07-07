package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.ImageViewSquare;

import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends RecyclerView.Adapter<MediaItemsAdapter.ViewHolder> {

    private int itemLayout;
    private List<MediaItem> items;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;

        public ViewHolder(View view) {
            super(view);
            this.imgSquare = (ImageViewSquare) view.findViewById(R.id.imgSquare);
            this.tvSquareTitle = (TextView) view.findViewById(R.id.tvSquareTitle);
        }
    }

    public MediaItemsAdapter(Context mContext, int itemLayout, List<MediaItem> items) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        ViewHolder viewHolder = null;
        view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MediaItemsAdapter.ViewHolder holder, int position) {
        RadioItem currentItem = (RadioItem)items.get(position);
        Glide.with(mContext).load(currentItem.getImageUrl()).centerCrop().crossFade().into(holder.imgSquare);
        holder.tvSquareTitle.setText(currentItem.getName());
        //holder.itemView.setTag(currentItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
