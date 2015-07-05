package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.BanerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/5/15.
 */
public class BanerItemsAdapter extends RecyclerView.Adapter<BanerItemsAdapter.ViewHolder> {

    private int itemLayout;
    private List<BanerItem> items;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgBaner;

        public ViewHolder(View view) {
            super(view);
            this.imgBaner = (ImageView) view.findViewById(R.id.imgBaner);
        }
    }

    public BanerItemsAdapter(Context mContext, int itemLayout, ArrayList<BanerItem> items) {
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
    public void onBindViewHolder(BanerItemsAdapter.ViewHolder holder, int position) {
        BanerItem currentItem = items.get(position);
        Glide.with(mContext).load(currentItem.getImageUrl()).centerCrop().crossFade().into(holder.imgBaner);
        holder.itemView.setTag(currentItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
