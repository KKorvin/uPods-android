package com.chickenkiller.upods2.controllers;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.models.BannersLayoutItem;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentRadioItemDetails;
import com.chickenkiller.upods2.views.ImageViewSquare;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER = 1;
    public static final int ITEM = 2;
    public static final int BANNERS_LAYOUT = 3;

    private int itemLayout;
    private int titleLayout;

    private List<MediaItem> items;
    private Context mContext;
    private IFragmentsManager fragmentsManager;

    private class ViewHolderBannersLayout extends RecyclerView.ViewHolder {
        private RecyclerViewPager rvBanners;
        private BannerItemsAdapter bannerItemsAdapter;
        private LinearLayoutManager layoutManager;

        public ViewHolderBannersLayout(View view) {
            super(view);
            rvBanners = (RecyclerViewPager) view.findViewById(R.id.rvBanners);
            layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
            rvBanners.setLayoutManager(layoutManager);
            rvBanners.setHasFixedSize(true);
            loadBanners(view.getContext());
        }

        private void loadBanners(final Context mContext) {
            RadioTopManager.getInstance().loadTops(RadioTopManager.TopType.MAIN_BANNER, new INetworkUIupdater() {
                @Override
                public void updateUISuccess(final Response response) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jResponse = new JSONObject(response.body().string());
                                ArrayList<RadioItem> topRadioStations = RadioItem.withJsonArray(jResponse.getJSONArray("result"), mContext);
                                bannerItemsAdapter = new BannerItemsAdapter(mContext, R.layout.baner_item, topRadioStations);
                                if (fragmentsManager!=null) {
                                    bannerItemsAdapter.setFragmentsManager(fragmentsManager);
                                }
                                rvBanners.setAdapter(bannerItemsAdapter);
                                layoutManager.scrollToPosition(bannerItemsAdapter.MIDDLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void updateUIFailed() {

                }

            });
        }
    }

    private class ViewHolderCardItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;
        public RatingBar rbMediaItem;
        public CardView cvSquare;

        public ViewHolderCardItem(View view) {
            super(view);
            this.imgSquare = (ImageViewSquare) view.findViewById(R.id.imgSquare);
            this.tvSquareTitle = (TextView) view.findViewById(R.id.tvSquareTitle);
            this.rbMediaItem = (RatingBar) view.findViewById(R.id.rbMediaItem);
            this.cvSquare = (CardView) view;
            view.setOnClickListener(this);
            Context context = view.getContext();
            LayerDrawable stars = (LayerDrawable) rbMediaItem.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(context.getResources().getColor(R.color.starFullySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(context.getResources().getColor(R.color.starPartiallySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(context.getResources().getColor(R.color.starNotSelected), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public void onClick(View view) {
            FragmentRadioItemDetails fragmentRadioItemDetails = new FragmentRadioItemDetails();
            if (items.get(getAdapterPosition()) instanceof RadioItem) {
                fragmentRadioItemDetails.setRadioItem((RadioItem) items.get(getAdapterPosition()));
            }
            fragmentsManager.showFragment(R.id.fl_window, fragmentRadioItemDetails, FragmentRadioItemDetails.TAG,
                    IFragmentsManager.FragmentOpenType.OVERLAY, IFragmentsManager.FragmentAnimationType.BOTTOM_TOP);
        }

    }

    private class ViewHolderMediaItemTitle extends RecyclerView.ViewHolder {
        public TextView tvMediaCardTitle;
        public TextView tvMediaCardSubTitle;

        public ViewHolderMediaItemTitle(View view) {
            super(view);
            this.tvMediaCardTitle = (TextView) view.findViewById(R.id.tvMediaCardTitle);
            this.tvMediaCardSubTitle = (TextView) view.findViewById(R.id.tvMediaCardSubTitle);
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

    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == BANNERS_LAYOUT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_banners_layout, parent, false);
            viewHolder = new ViewHolderBannersLayout(view);
        } else if (viewType == ITEM) {
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
        if (holder instanceof ViewHolderCardItem) {
            RadioItem currentItem = (RadioItem) items.get(position);
            Glide.with(mContext).load(currentItem.getCoverImageUrl()).centerCrop().crossFade().into(((ViewHolderCardItem) holder).imgSquare);
            ((ViewHolderCardItem) holder).tvSquareTitle.setText(currentItem.getName());
            holder.itemView.setTag(currentItem);
        } else if (holder instanceof ViewHolderMediaItemTitle) {
            MediaItemTitle currentItem = (MediaItemTitle) items.get(position);
            ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.setText(currentItem.getTitle());
            ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setText(currentItem.getSubTitle());
            holder.itemView.setTag(currentItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof BannersLayoutItem)
            return BANNERS_LAYOUT;
        if (items.get(position) instanceof MediaItemTitle)
            return HEADER;

        return ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(ArrayList<MediaItem> items){
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

}
