package com.chickenkiller.upods2.controllers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.SlidingMenuItem;
import com.yqritc.recyclerviewflexibledivider.FlexibleDividerDecoration;

import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FlexibleDividerDecoration.VisibilityProvider {

    private static final int HEADER = 1;
    private static final int ITEMS_HEADER = 2;
    private static final int ITEM = 3;
    private static final int HEADER_LAYOUT = R.layout.sliding_menu_header;

    private List<SlidingMenuItem> items;
    private int itemLayout;

    private class ViewHolderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image;
        public TextView text;

        public ViewHolderItem(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.imgSMenutIcon);
            this.text = (TextView) itemView.findViewById(R.id.tvSMenuTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "TEST" + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }

    private class ViewHolderHeader extends RecyclerView.ViewHolder {
        public ImageView headerAvater;
        public TextView headerText;
        public TextView headerEmail;

        public ViewHolderHeader(View itemView, int type) {
            super(itemView);
            this.headerAvater = (ImageView) itemView.findViewById(R.id.imgSMHeaderAvatar);
            this.headerText = (TextView) itemView.findViewById(R.id.tvSMHeaderName);
            this.headerEmail = (TextView) itemView.findViewById(R.id.tvSMHeaderEmail);
        }
    }


    public SlidingMenuAdapter(List<SlidingMenuItem> items, int itemLayout) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
            viewHolder = new ViewHolderItem(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(HEADER_LAYOUT, parent, false);
            viewHolder = new ViewHolderHeader(view, viewType);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SlidingMenuItem item = items.get(position);
        if (holder instanceof ViewHolderItem) {
            ((ViewHolderItem) holder).text.setText(item.getTitle());
            ((ViewHolderItem) holder).image.setImageResource(item.getIconId());
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

    @Override
    public boolean shouldHideDivider(int i, RecyclerView recyclerView) {
        return !items.get(i).hasDevider;
    }
}
