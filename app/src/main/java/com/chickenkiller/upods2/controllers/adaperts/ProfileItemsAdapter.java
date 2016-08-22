package com.chickenkiller.upods2.controllers.adaperts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.ProfileItem;

/**
 * Created by Alon Zilberman on 2/26/16.
 */
public class ProfileItemsAdapter extends ArrayAdapter<ProfileItem> {

    private int layaoutId;

    private static class ViewHolder {
        public TextView tvLeftText;
        public TextView tvRightText;
    }

    public ProfileItemsAdapter(Context context, int layaoutId) {
        super(context, layaoutId);
        this.layaoutId = layaoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProfileItem profileItem = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layaoutId, parent, false);
            viewHolder.tvLeftText = (TextView) convertView.findViewById(R.id.tvLeftText);
            viewHolder.tvRightText = (TextView) convertView.findViewById(R.id.tvRightText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvLeftText.setText(profileItem.leftText);
        viewHolder.tvRightText.setText(profileItem.rightText);
        return convertView;
    }
}
