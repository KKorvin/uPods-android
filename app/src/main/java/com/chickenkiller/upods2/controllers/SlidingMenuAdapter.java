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

    private static final int HEADER = 1;
    private static final int ITEM = 2;
    private static final int HEADER_LAYOUT = R.layout.sliding_menu_header;

    private List<SlidingMenuItem> items;
    private int itemLayout;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int type;
        public ImageView image;
        public TextView text;

        public ImageView headerAvater;
        public TextView headerText;
        public TextView headerEmail;

        public ViewHolder(View itemView, int type) {
            super(itemView);
            this.type = type;
            if (type == ITEM) {
                this.image = (ImageView) itemView.findViewById(R.id.imgSMenutIcon);
                this.text = (TextView) itemView.findViewById(R.id.tvSMenuTitle);
            } else {
                this.headerAvater = (ImageView) itemView.findViewById(R.id.imgSMHeaderAvatar);
                this.headerText = (TextView) itemView.findViewById(R.id.tvSMHeaderName);
                this.headerEmail = (TextView) itemView.findViewById(R.id.tvSMHeaderEmail);
            }
        }
    }


    public SlidingMenuAdapter(List<SlidingMenuItem> items, int itemLayout) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(HEADER_LAYOUT, parent, false);
        }
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SlidingMenuItem item = items.get(position);
        if (holder.type == ITEM) {
            holder.text.setText(item.getTitle());
            holder.image.setImageResource(item.getIconId());
        }
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (this.isPositionHeader(position))
            return HEADER;

        return ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}
