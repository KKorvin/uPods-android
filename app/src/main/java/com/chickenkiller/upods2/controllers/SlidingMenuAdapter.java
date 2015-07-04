package com.chickenkiller.upods2.controllers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.SlidingMenuItem;

import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenuAdapter extends RecyclerView.Adapter<SlidingMenuAdapter.ViewHolder> {

    private List<SlidingMenuItem> items;
    private int itemLayout;

    public SlidingMenuAdapter(List<SlidingMenuItem> items, int itemLayout) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SlidingMenuItem item = items.get(position);
        holder.text.setText(item.getTitle());
        holder.image.setImageResource(item.getIconId());
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.imgSMenutIcon);
            text = (TextView) itemView.findViewById(R.id.tvSMenuTitle);
        }
    }


}
