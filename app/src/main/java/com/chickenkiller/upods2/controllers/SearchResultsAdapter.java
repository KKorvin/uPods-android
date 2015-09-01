package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentMediaItemDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/2/15.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int itemLayout;

    private List<MediaItem> items;
    private Context mContext;
    private IFragmentsManager fragmentsManager;


    private static class ViewHolderSearchResult extends RecyclerView.ViewHolder {
        public ImageView imgCover;
        public TextView tvTitle;
        public TextView tvCountry;
        private View rootView;

        public ViewHolderSearchResult(View view) {
            super(view);
            this.imgCover = (ImageView) view.findViewById(R.id.imgSearchRadioCover);
            this.tvTitle = (TextView) view.findViewById(R.id.tvSearchRadioTitle);
            this.tvCountry = (TextView) view.findViewById(R.id.tvSearchCountry);
            this.rootView = view;
        }

        public void setItemClickListener(View.OnClickListener clickListener) {
            rootView.setOnClickListener(clickListener);
        }
    }


    public SearchResultsAdapter(Context mContext, int itemLayout) {
        super();
        this.items = new ArrayList<>();
        this.itemLayout = itemLayout;
        this.mContext = mContext;
    }


    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        RecyclerView.ViewHolder viewHolder = new ViewHolderSearchResult(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderSearchResult) {
            MediaItem currentItem = items.get(position);
            if (currentItem instanceof RadioItem) {
                Glide.with(mContext).load(((RadioItem) currentItem).getCoverImageUrl()).centerCrop()
                        .crossFade().into(((ViewHolderSearchResult) holder).imgCover);
                ((ViewHolderSearchResult) holder).tvTitle.setText(((RadioItem) currentItem).getName());
                ((ViewHolderSearchResult) holder).tvCountry.setText(((RadioItem) currentItem).getCountry());
            } else {
                Glide.with(mContext).load(((Podcast) currentItem).getCoverImageUrl()).centerCrop()
                        .crossFade().into(((ViewHolderSearchResult) holder).imgCover);
                ((ViewHolderSearchResult) holder).tvTitle.setText(((Podcast) currentItem).getName());
                ((ViewHolderSearchResult) holder).tvCountry.setText(((Podcast) currentItem).getGenre());
            }

            ((ViewHolderSearchResult) holder).setItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentMediaItemDetails fragmentMediaItemDetails = new FragmentMediaItemDetails();
                    if (items.get(position) instanceof IPlayableMediaItem) {
                        fragmentMediaItemDetails.setPlayableItem((IPlayableMediaItem) items.get(position));
                    }
                    if (!fragmentsManager.hasFragment(FragmentMediaItemDetails.TAG)) {
                        SearchView searchView = (SearchView) ((IToolbarHolder) mContext).getToolbar().getMenu().findItem(R.id.action_search).getActionView();
                        searchView.clearFocus();
                        fragmentsManager.showFragment(R.id.fl_window, fragmentMediaItemDetails, FragmentMediaItemDetails.TAG,
                                IFragmentsManager.FragmentOpenType.OVERLAY, IFragmentsManager.FragmentAnimationType.BOTTOM_TOP);
                    }
                }
            });
            holder.itemView.setTag(currentItem);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(ArrayList<MediaItem> items) {
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
    }

}
