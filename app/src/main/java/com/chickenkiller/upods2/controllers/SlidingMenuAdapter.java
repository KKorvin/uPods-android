package com.chickenkiller.upods2.controllers;

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
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.ISlidingMenuManager;
import com.chickenkiller.upods2.models.SlidingMenuHeader;
import com.chickenkiller.upods2.models.SlidingMenuItem;
import com.chickenkiller.upods2.models.SlidingMenuRow;
import com.chickenkiller.upods2.view.controller.FragmentSettings;

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

    private class ViewHolderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image;
        public TextView text;
        public LinearLayout lnSlidingMenuItem;

        public ViewHolderItem(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.imgSMenutIcon);
            this.text = (TextView) itemView.findViewById(R.id.tvSMenuTitle);
            this.lnSlidingMenuItem = (LinearLayout) itemView.findViewById(R.id.lnSlidingMenuItem);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (items.get(getAdapterPosition()) instanceof SlidingMenuRow) {
                clearRowSelections();
                SlidingMenuRow clickedMenuItem = (SlidingMenuRow) items.get(getAdapterPosition());
                clickedMenuItem.isSelected = true;
                Context context = view.getContext();
                if (clickedMenuItem.getTitle().equals(context.getString(R.string.main_settings))) {
                    FragmentSettings settingsFragment = new FragmentSettings();
                    fragmentsManager.showFragment(R.id.fl_content, settingsFragment, FragmentSettings.TAG);
                } else {
                    Toast.makeText(context, "TEST" + clickedMenuItem.getTitle(), Toast.LENGTH_SHORT).show();
                }
                slidingMenuManager.toggle();
                notifyDataSetChanged();
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SlidingMenuItem item = items.get(position);
        if (holder instanceof ViewHolderItem) {
            Context mContext = ((ViewHolderItem) holder).text.getContext();
            ((ViewHolderItem) holder).text.setText(((SlidingMenuRow) item).getTitle());
            if (((SlidingMenuRow) item).isSelected) {
                ((ViewHolderItem) holder).text.setTextColor(mContext.getResources().getColor(R.color.pink_A400));
                ((ViewHolderItem) holder).lnSlidingMenuItem.setBackgroundColor(mContext.getResources().getColor(R.color.fragment_deatils_opacity_bckg));
                ((ViewHolderItem) holder).image.setImageResource(((SlidingMenuRow) item).getPressedIconId());
            } else {
                ((ViewHolderItem) holder).text.setTextColor(mContext.getResources().getColor(R.color.gray_202020));
                ((ViewHolderItem) holder).lnSlidingMenuItem.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                ((ViewHolderItem) holder).image.setImageResource(((SlidingMenuRow) item).getMainIconId());
            }
        }
        holder.itemView.setTag(item);
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
