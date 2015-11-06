package com.chickenkiller.upods2.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 11/6/15.
 */
public class Playlist {

    private LinearLayout lnPlaylist;
    private ImageButton imgPlaybutton;
    private Context mContext;


    public Playlist(Context mContext, View rootView) {
        this.mContext = mContext;
        this.lnPlaylist = (LinearLayout) rootView.findViewById(R.id.lnPlaylist);
    }

    public View.OnClickListener getPlaylistOpenClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lnPlaylist.getLayoutParams();
                ValueAnimator animator = ValueAnimator.ofInt(params.bottomMargin, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                        lnPlaylist.requestLayout();
                    }
                });
                animator.setDuration(300);
                animator.start();
            }
        };
    }

}
