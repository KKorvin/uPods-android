package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.BannersLayoutItem;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.ViewHolderBannersLayout;
import com.chickenkiller.upods2.utils.ContextMenuType;
import com.chickenkiller.upods2.view.controller.FragmentMediaItemDetails;
import com.chickenkiller.upods2.views.ImageViewSquare;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used for any layout which shows MediaItems (or only cards with IPlayableMediaItem)
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER = 1;
    public static final int ITEM = 2;
    public static final int BANNERS_LAYOUT = 3;
    private static final int MAX_CONTENT_LEVEL = 2; //count of items to load (banner, main cards)

    private int itemLayout;
    private int titleLayout;
    private int currentContentLevel;
    private boolean needDestroy;

    private List<MediaItem> items;
    private Context mContext;
    private IFragmentsManager fragmentsManager;
    private IContentLoadListener iContentLoadListener;

    private static class ViewHolderCardItem extends RecyclerView.ViewHolder {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;
        public TextView tvItemStatus;
        public TextView tvSquareSubTitle;
        public RatingBar rbMediaItem;
        public CardView cvSquare;
        public ImageView imgCardMenuVert;

        public ViewHolderCardItem(View view) {
            super(view);
            this.imgSquare = (ImageViewSquare) view.findViewById(R.id.imgSquare);
            this.imgCardMenuVert = (ImageView) view.findViewById(R.id.imgCardMenuVert);
            this.tvItemStatus = (TextView) view.findViewById(R.id.tvItemStatus);
            this.tvSquareTitle = (TextView) view.findViewById(R.id.tvSquareTitle);
            this.tvSquareSubTitle = (TextView) view.findViewById(R.id.tvSquareSubTitle);
            this.rbMediaItem = (RatingBar) view.findViewById(R.id.rbMediaItem);
            this.cvSquare = (CardView) view;
            Context context = view.getContext();
            LayerDrawable stars = (LayerDrawable) rbMediaItem.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(context.getResources().getColor(R.color.starFullySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(context.getResources().getColor(R.color.starPartiallySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(context.getResources().getColor(R.color.starNotSelected), PorterDuff.Mode.SRC_ATOP);
        }

        public void setCardClickListener(View.OnClickListener cardClickListener) {
            cvSquare.setOnClickListener(cardClickListener);
        }

        public void setCardMenuClickListener(View.OnClickListener cardMenuClickListener) {
            if(imgCardMenuVert!=null) {
                imgCardMenuVert.setOnClickListener(cardMenuClickListener);
            }
        }

    }

    private static class ViewHolderMediaItemTitle extends RecyclerView.ViewHolder {
        public TextView tvMediaCardTitle;
        public TextView tvMediaCardSubTitle;
        public Button btnMediaTitleMore;

        public ViewHolderMediaItemTitle(View view) {
            super(view);
            this.tvMediaCardTitle = (TextView) view.findViewById(R.id.tvMediaCardTitle);
            this.tvMediaCardSubTitle = (TextView) view.findViewById(R.id.tvMediaCardSubTitle);
            this.btnMediaTitleMore = (Button) view.findViewById(R.id.btnMediaTitleMore);
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

    public MediaItemsAdapter(Context mContext, int itemLayout, int titleLayout) {
        this(mContext, itemLayout, new ArrayList<MediaItem>());
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
            IContentLoadListener contentLoadListener = new IContentLoadListener() {
                @Override
                public void onContentLoaded() {
                    notifyContentLoadingStatus();
                }
            };
            viewHolder = new ViewHolderBannersLayout(view, fragmentsManager, contentLoadListener);
            currentContentLevel = 0;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderCardItem) {//Card
            IPlayableMediaItem currentItem = (IPlayableMediaItem) items.get(position);
            Glide.with(mContext).load(currentItem.getCoverImageUrl()).centerCrop()
                    .crossFade().into(((ViewHolderCardItem) holder).imgSquare);
            ((ViewHolderCardItem) holder).tvSquareTitle.setText(currentItem.getName());
            if (((ViewHolderCardItem) holder).tvSquareSubTitle != null) {
                ((ViewHolderCardItem) holder).tvSquareSubTitle.setText(currentItem.getSubHeader());
            }
            if (((ViewHolderCardItem) holder).tvItemStatus != null) {
                String status = mContext.getString(ProfileManager.getInstance().getItemStatus(currentItem));
                ((ViewHolderCardItem) holder).tvItemStatus.setText(status);
            }

            holder.itemView.setTag(currentItem);
            ((ViewHolderCardItem) holder).setCardClickListener(getCardClickListener(position));
            ((ViewHolderCardItem) holder).setCardMenuClickListener(getCardMenuClickListener(position));
        } else if (holder instanceof ViewHolderMediaItemTitle) {//Title
            MediaItemTitle currentItem = (MediaItemTitle) items.get(position);
            ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.setText(currentItem.getTitle());
            if (currentItem.getSubTitle().isEmpty()) {
                ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setVisibility(View.GONE);
            } else {
                ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setVisibility(View.VISIBLE);
                ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setText(currentItem.getSubTitle());
            }
            if (!currentItem.showButton) {
                ((ViewHolderMediaItemTitle) holder).btnMediaTitleMore.setVisibility(View.GONE);
            }
            holder.itemView.setTag(currentItem);
        }
    }

    private View.OnClickListener getCardClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentMediaItemDetails fragmentMediaItemDetails = new FragmentMediaItemDetails();
                if (items.get(position) instanceof IPlayableMediaItem) {
                    fragmentMediaItemDetails.setPlayableItem((IPlayableMediaItem) items.get(position));
                }
                if (!fragmentsManager.hasFragment(FragmentMediaItemDetails.TAG)) {
                    fragmentsManager.showFragment(R.id.fl_window, fragmentMediaItemDetails, FragmentMediaItemDetails.TAG,
                            IFragmentsManager.FragmentOpenType.OVERLAY, IFragmentsManager.FragmentAnimationType.BOTTOM_TOP);
                }
            }
        };
    }

    private View.OnClickListener getCardMenuClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (items.get(position) instanceof IPlayableMediaItem && mContext instanceof IContextMenuManager) {
                    ((IContextMenuManager) mContext).openContextMenu(view, ContextMenuType.CARD_DEFAULT, items.get(position), null);
                }
            }
        };
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

    public void addItems(ArrayList<MediaItem> items) {
        this.items.addAll(items);
        this.notifyDataSetChanged();
    }

    public void clearItems() {
        this.items.clear();
    }

    public MediaItem getItemAt(int position) {
        return this.items.get(position);
    }

    public void setContentLoadListener(IContentLoadListener contentLoadListener) {
        this.iContentLoadListener = contentLoadListener;
    }


    /**
     * Function to synchronize adapter content loading . Call it every time, when part of content was loaded.
     * When all content will be loaded  IContentLoadListener callback will fire.
     */
    public void notifyContentLoadingStatus() {
        currentContentLevel++;
        if (currentContentLevel == MAX_CONTENT_LEVEL && iContentLoadListener != null) {
            iContentLoadListener.onContentLoaded();
        }
    }

    public void destroy() {
        this.needDestroy = true;
    }

}
