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
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.ImageViewSquare;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER = 1;
    public static final int ITEM = 2;

    private int itemLayout;
    private int titleLayout;
    private List<MediaItem> items;
    private Context mContext;

    public static class ViewHolderCardItem extends RecyclerView.ViewHolder {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;

        public ViewHolderCardItem(View view) {
            super(view);
            this.imgSquare = (ImageViewSquare) view.findViewById(R.id.imgSquare);
            this.tvSquareTitle = (TextView) view.findViewById(R.id.tvSquareTitle);
        }
    }

    public static class ViewHolderMediaItemTitle extends RecyclerView.ViewHolder {
        public TextView tvMediaCardTitle;

        public ViewHolderMediaItemTitle(View view) {
            super(view);
            this.tvMediaCardTitle = (TextView) view.findViewById(R.id.tvMediaCardTitle);
        }
    }

    public MediaItemsAdapter(Context mContext, int itemLayout, List<MediaItem> items) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
        this.mContext = mContext;
    }

    public MediaItemsAdapter(Context mContext, int itemLayout, int titleLayout, ArrayList<MediaItem> items) {
        this(mContext, itemLayout, items);
        this.titleLayout = titleLayout;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            viewHolder = new ViewHolderCardItem(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(titleLayout, parent, false);
            viewHolder = new ViewHolderMediaItemTitle(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (items.get(position) instanceof RadioItem) {
            RadioItem currentItem = (RadioItem) items.get(position);
            Glide.with(mContext).load(currentItem.getImageUrl()).centerCrop().crossFade().into(((ViewHolderCardItem) holder).imgSquare);
            ((ViewHolderCardItem) holder).tvSquareTitle.setText(currentItem.getName());
        } else {
            MediaItemTitle currentItem = (MediaItemTitle) items.get(position);
            ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.setText(currentItem.getTitle());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof MediaItemTitle)
            return HEADER;

        return ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
