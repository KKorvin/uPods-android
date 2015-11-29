package com.chickenkiller.upods2.controllers.adaperts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.fragments.FragmentMediaItemsGrid;
import com.chickenkiller.upods2.fragments.FragmentProfile;
import com.chickenkiller.upods2.fragments.FragmentSettings;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.ISlidingMenuManager;
import com.chickenkiller.upods2.models.SlidingMenuHeader;
import com.chickenkiller.upods2.models.SlidingMenuItem;
import com.chickenkiller.upods2.models.SlidingMenuRow;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 1;
    private static final int ITEM = 2;
    private static final int HEADER_LAYOUT = R.layout.sliding_menu_header;

    private int itemLayout;
    private List<SlidingMenuItem> items;
    private IFragmentsManager fragmentsManager;
    private ISlidingMenuManager slidingMenuManager;

    private static class ViewHolderItem extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;
        public LinearLayout lnSlidingMenuItem;
        private View rootView;

        public ViewHolderItem(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.imgSMenutIcon);
            this.text = (TextView) itemView.findViewById(R.id.tvSMenuTitle);
            this.lnSlidingMenuItem = (LinearLayout) itemView.findViewById(R.id.lnSlidingMenuItem);
            this.rootView = itemView;
        }

        public void setItemClickListener(View.OnClickListener itemClickListener) {
            this.rootView.setOnClickListener(itemClickListener);
        }
    }

    public void setSelectedRow(String itemName) {
        clearRowSelections();
        for (SlidingMenuItem item : items) {
            if (item instanceof SlidingMenuRow && ((SlidingMenuRow) (item)).getTitle().equals(itemName)) {
                ((SlidingMenuRow) (item)).isSelected = true;
                notifyDataSetChanged();
                return;
            }
        }
    }

    private class ViewHolderHeader extends RecyclerView.ViewHolder {
        public ImageView headerAvater;
        public TextView headerText;
        public TextView headerEmail;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            this.headerAvater = (ImageView) itemView.findViewById(R.id.imgSMHeaderAvatar);
            this.headerText = (TextView) itemView.findViewById(R.id.tvSMHeaderName);
            this.headerEmail = (TextView) itemView.findViewById(R.id.tvSMHeaderEmail);
        }
    }

    public SlidingMenuAdapter(List<SlidingMenuItem> items, int itemLayout, ISlidingMenuManager slidingMenuManager,
                              IFragmentsManager fragmentsManager) {
        this(items, itemLayout, slidingMenuManager);
        this.fragmentsManager = fragmentsManager;
    }

    public SlidingMenuAdapter(List<SlidingMenuItem> items, int itemLayout, ISlidingMenuManager slidingMenuManager) {
        super();
        this.items = items;
        this.itemLayout = itemLayout;
        this.slidingMenuManager = slidingMenuManager;
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
            viewHolder = new ViewHolderHeader(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SlidingMenuItem item = items.get(position);
        if (holder instanceof ViewHolderItem) {
            Context mContext = ((ViewHolderItem) holder).text.getContext();
            ((ViewHolderItem) holder).text.setText(((SlidingMenuRow) item).getTitle());
            if (((SlidingMenuRow) item).isSelected) {
                ((ViewHolderItem) holder).text.setTextColor(mContext.getResources().getColor(R.color.pink_A400));
                ((ViewHolderItem) holder).lnSlidingMenuItem.setBackgroundResource(R.drawable.selector_sliding_menu_selected_item);
                ((ViewHolderItem) holder).image.setImageResource(((SlidingMenuRow) item).getPressedIconId());
            } else {
                ((ViewHolderItem) holder).text.setTextColor(mContext.getResources().getColor(R.color.gray_202020));
                ((ViewHolderItem) holder).lnSlidingMenuItem.setBackgroundResource(R.drawable.selector_sliding_menu_item);
                ((ViewHolderItem) holder).image.setImageResource(((SlidingMenuRow) item).getMainIconId());
            }
            setSlidingMenuItemClick((ViewHolderItem) holder, position);
        }
        holder.itemView.setTag(item);
    }

    private void setSlidingMenuItemClick(ViewHolderItem viewHolderItem, final int position) {
        viewHolderItem.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (items.get(position) instanceof SlidingMenuRow) {
                    clearRowSelections();
                    SlidingMenuRow clickedMenuItem = (SlidingMenuRow) items.get(position);
                    clickedMenuItem.isSelected = true;
                    Context context = view.getContext();
                    slidingMenuManager.toggle();
                    notifyDataSetChanged();
                    if (clickedMenuItem.getTitle().equals(context.getString(R.string.main_settings))) {
                        FragmentSettings settingsFragment = new FragmentSettings();
                        fragmentsManager.showFragment(R.id.fl_content, settingsFragment, FragmentSettings.TAG);
                    } else if (clickedMenuItem.getTitle().equals(context.getString(R.string.podcasts_main))) {
                        FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
                        fragmentMediaItemsGrid.setMediaItemType(MediaItemType.PODCAST);
                        fragmentsManager.showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
                    } else if (clickedMenuItem.getTitle().equals(context.getString(R.string.radio_main))) {
                        FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
                        fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
                        fragmentsManager.showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
                    } else if (clickedMenuItem.getTitle().equals(context.getString(R.string.profile_my_profile))) {
                        FragmentProfile fragmentProfile = new FragmentProfile();
                        fragmentsManager.showFragment(R.id.fl_content, fragmentProfile, FragmentProfile.TAG);
                    } else {
                        Toast.makeText(context, "TEST" + clickedMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof SlidingMenuHeader)
            return HEADER;

        return ITEM;
    }

    public boolean shouldHideDivider(int i) {
        return !items.get(i).hasDevider;
    }

    public void clearRowSelections() {
        for (SlidingMenuItem row : items) {
            if (row instanceof SlidingMenuRow) {
                ((SlidingMenuRow) row).isSelected = false;
            }
        }
    }
}
