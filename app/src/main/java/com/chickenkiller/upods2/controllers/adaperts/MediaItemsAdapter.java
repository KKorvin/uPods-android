package com.chickenkiller.upods2.controllers.adaperts;

import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.fragments.FragmentMediaItemDetails;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.BannersLayoutItem;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RoundedButtonsLayoutItem;
import com.chickenkiller.upods2.models.ViewHolderBannersLayout;
import com.chickenkiller.upods2.utils.decorators.DelayedOnClickListener;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;
import com.chickenkiller.upods2.views.ImageViewSquare;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used for any layout which shows MediaItems (or only cards with IPlayableMediaItem). Supports synchronise content loading.
 * Created by alonzilberman on 7/2/15.
 */
public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER = 1;
    public static final int ITEM = 2;
    public static final int BANNERS_LAYOUT = 3;
    public static final int ROUNDED_BUTTONS = 4;
    private static final int MAX_CONTENT_LEVEL = 2; //count of items to load (banner, main cards)
    private static final int COVER_IMAGE_SIZE = UIHelper.dpToPixels(80);

    private int itemLayout;
    private int titleLayout;
    private int roundedButtonsLayout;
    private int currentContentLevel;

    private List<MediaItem> items;
    private Context mContext;
    private IFragmentsManager fragmentsManager;
    private IContentLoadListener iContentLoadListener;
    private IOperationFinishWithDataCallback iRoundButtonClicked;
    private MediaItemType mediaItemType;


    private static class ViewHolderCardItem extends RecyclerView.ViewHolder {
        public ImageViewSquare imgSquare;
        public TextView tvSquareTitle;
        public TextView tvItemStatus;
        public TextView tvItemCount;
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
            this.tvItemCount = (TextView) view.findViewById(R.id.tvItemCount);
            this.rbMediaItem = (RatingBar) view.findViewById(R.id.rbMediaItem);
            this.cvSquare = (CardView) view;
            Context context = view.getContext();
            LayerDrawable stars = (LayerDrawable) rbMediaItem.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(context.getResources().getColor(R.color.starFullySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(context.getResources().getColor(R.color.starPartiallySelected), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(context.getResources().getColor(R.color.starNotSelected), PorterDuff.Mode.SRC_ATOP);
        }

        public void setCardClickListener(View.OnClickListener cardClickListener) {
            cvSquare.setOnClickListener(new DelayedOnClickListener(cardClickListener));
        }

        public void setCardMenuClickListener(View.OnClickListener cardMenuClickListener) {
            if (imgCardMenuVert != null) {
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

    private static class ViewHolderRoundedButtons extends RecyclerView.ViewHolder {

        public ViewHolderRoundedButtons(View view, final IOperationFinishWithDataCallback iRoundButtonClicked) {
            super(view);
            if (iRoundButtonClicked != null) {
                view.findViewById(R.id.btnRoundedGenres).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        iRoundButtonClicked.operationFinished(RoundedButtonsLayoutItem.ROUND_BTN_GENRES);
                    }
                });
                view.findViewById(R.id.btnRoundedCountries).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        iRoundButtonClicked.operationFinished(RoundedButtonsLayoutItem.ROUND_BTN_COUNTRIES);
                    }
                });

                view.findViewById(R.id.btnRoundedLanguages).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        iRoundButtonClicked.operationFinished(RoundedButtonsLayoutItem.ROUND_BTN_LAGUAGES);
                    }
                });
            }
        }

    }

    public MediaItemsAdapter(Context mContext, int itemLayout, List<MediaItem> items) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
        this.mContext = mContext;
        this.mediaItemType = MediaItemType.DEFAULT;
    }

    public MediaItemsAdapter(Context mContext, int itemLayout, int titleLayout, ArrayList<MediaItem> items) {
        this(mContext, itemLayout, items);
        this.titleLayout = titleLayout;
    }

    public MediaItemsAdapter(Context mContext, int itemLayout, int titleLayout) {
        this(mContext, itemLayout, new ArrayList<MediaItem>());
        this.titleLayout = titleLayout;
    }

    public void setRoundedButtonsLayout(int roundedButtonsLayout) {
        this.roundedButtonsLayout = roundedButtonsLayout;
    }

    public void setFragmentsManager(IFragmentsManager fragmentsManager) {
        this.fragmentsManager = fragmentsManager;
    }

    public void setiRoundButtonClicked(IOperationFinishWithDataCallback iRoundButtonClicked) {
        this.iRoundButtonClicked = iRoundButtonClicked;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
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
        } else if (viewType == ROUNDED_BUTTONS) {
            view = LayoutInflater.from(parent.getContext()).inflate(roundedButtonsLayout, parent, false);
            viewHolder = new ViewHolderRoundedButtons(view, iRoundButtonClicked);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(titleLayout, parent, false);
            viewHolder = new ViewHolderMediaItemTitle(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderCardItem) {//Card
            bindCardViewHolder(holder, position);
        } else if (holder instanceof ViewHolderMediaItemTitle) {//Title
            bindTitleViewHolder(holder, position);
        }
    }

    private void bindTitleViewHolder(RecyclerView.ViewHolder holder, int position) {
        MediaItemTitle currentItem = (MediaItemTitle) items.get(position);
        ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.setText(currentItem.getTitle());
        if (currentItem.getSubTitle().isEmpty()) {
            ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setVisibility(View.GONE);
            RelativeLayout.LayoutParams llp = (RelativeLayout.LayoutParams) ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.getLayoutParams();
            // Left // Top // Right // Bottom
            llp.setMargins(UIHelper.dpToPixels(15), UIHelper.dpToPixels(10), UIHelper.dpToPixels(15), UIHelper.dpToPixels(3));
            ((ViewHolderMediaItemTitle) holder).tvMediaCardTitle.setLayoutParams(llp);
        } else {
            ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setVisibility(View.VISIBLE);
            ((ViewHolderMediaItemTitle) holder).tvMediaCardSubTitle.setText(currentItem.getSubTitle());
        }
        if (!currentItem.showButton) {
            ((ViewHolderMediaItemTitle) holder).btnMediaTitleMore.setVisibility(View.GONE);
        }
        holder.itemView.setTag(currentItem);
    }

    private void bindCardViewHolder(RecyclerView.ViewHolder holder, int position) {
        IPlayableMediaItem currentItem = (IPlayableMediaItem) items.get(position);
        if (currentItem.getCoverImageUrl() == null) {
            final LetterBitmap letterBitmap = new LetterBitmap(mContext);
            Bitmap letterTile = letterBitmap.getLetterTile(currentItem.getName(), currentItem.getName(), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
            ((ViewHolderCardItem) holder).imgSquare.setImageBitmap(letterTile);
        } else {
            Glide.with(mContext).load(currentItem.getCoverImageUrl()).centerCrop()
                    .crossFade().into(((ViewHolderCardItem) holder).imgSquare);
        }
        ((ViewHolderCardItem) holder).tvSquareTitle.setText(currentItem.getName());
        if (((ViewHolderCardItem) holder).tvSquareSubTitle != null) {
            ((ViewHolderCardItem) holder).tvSquareSubTitle.setText(currentItem.getSubHeader());
        }
        if (((ViewHolderCardItem) holder).tvItemStatus != null) {
            initStatusBlock((ViewHolderCardItem) holder, currentItem);
        }
        if (((ViewHolderCardItem) holder).tvItemCount != null) {
            initCountBlock((ViewHolderCardItem) holder, currentItem);
        }
        holder.itemView.setTag(currentItem);
        ((ViewHolderCardItem) holder).setCardClickListener(getCardClickListener(position));
        ((ViewHolderCardItem) holder).setCardMenuClickListener(getCardMenuClickListener(position));
    }

    /**
     * Inits status block (icon + text, i.e downloaded)
     */
    private void initStatusBlock(ViewHolderCardItem holder, IPlayableMediaItem currentItem) {
        if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED &&
                ProfileManager.getInstance().isDownloaded(currentItem)) {
            holder.tvItemStatus.setText(R.string.downloaded);
            holder.tvItemStatus.setVisibility(View.VISIBLE);

        } else {
            holder.tvItemStatus.setVisibility(View.GONE);
        }
    }

    /**
     * Inits cpunt block (number in circle, i.e new episods)
     */
    private void initCountBlock(ViewHolderCardItem holder, IPlayableMediaItem currentItem) {
        if (mediaItemType == MediaItemType.PODCAST_FAVORITE && currentItem instanceof Podcast &&
                ((Podcast) currentItem).getNewEpisodsCount() > 0) {//only for podcasts in favorites
            int newEpisodesCount = ((Podcast) currentItem).getNewEpisodsCount();
            holder.tvItemCount.setText(String.valueOf(newEpisodesCount));
            holder.tvItemCount.setVisibility(View.VISIBLE);
        } else {
            holder.tvItemCount.setVisibility(View.GONE);
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
        if (items.get(position) instanceof RoundedButtonsLayoutItem)
            return ROUNDED_BUTTONS;
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

    public void clearCurrentContentLevel() {
        currentContentLevel = 0;
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

}
