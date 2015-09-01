package com.chickenkiller.upods2.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentMediaItemDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/5/15.
 */
public class BannerItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HALF_MAX_VALUE = Integer.MAX_VALUE / 2;
    public final int MIDDLE;

    private int itemLayout;
    private List<RadioItem> items;
    private Context mContext;
    private DisplayMetrics displaymetrics;
    private IFragmentsManager fragmentsManager;


    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imgBaner;

        public ViewHolder(View view) {
            super(view);
            this.imgBaner = (ImageView) view.findViewById(R.id.imgBaner);
            this.imgBaner.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (fragmentsManager != null) {
                FragmentMediaItemDetails fragmentMediaItemDetails = new FragmentMediaItemDetails();
                fragmentMediaItemDetails.setPlayableItem(getItem(getAdapterPosition()));
                if (!fragmentsManager.hasFragment(FragmentMediaItemDetails.TAG)) {
                    fragmentsManager.showFragment(R.id.fl_window, fragmentMediaItemDetails, FragmentMediaItemDetails.TAG,
                            IFragmentsManager.FragmentOpenType.OVERLAY, IFragmentsManager.FragmentAnimationType.BOTTOM_TOP);
                }
            }
        }
    }

    public BannerItemsAdapter(Context mContext, int itemLayout, ArrayList<RadioItem> items) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
        this.mContext = mContext;
        this.displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        MIDDLE = HALF_MAX_VALUE - HALF_MAX_VALUE % items.size();

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RadioItem currentItem = getItem(position);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ((ViewHolder) holder).imgBaner.getLayoutParams();
        params.width = displaymetrics.widthPixels;
        Glide.with(mContext).load(currentItem.getBannerImageUrl()).into(((ViewHolder) holder).imgBaner);
        holder.itemView.setTag(currentItem);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public RadioItem getItem(int position) {
        return items.get(position % items.size());
    }

    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }
}
