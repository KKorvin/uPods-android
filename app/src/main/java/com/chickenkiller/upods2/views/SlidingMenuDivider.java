package com.chickenkiller.upods2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chickenkiller.upods2.controllers.adaperts.SlidingMenuAdapter;

/**
 * Created by alonzilberman on 7/27/15.
 */
public class SlidingMenuDivider extends RecyclerView.ItemDecoration {

    private final int DEVIDER_SIZE = 2;
    private Drawable mDivider;
    public SlidingMenuDivider(Context mContext, int drawable) {
        mDivider = mContext.getResources().getDrawable(drawable);
    }

    /**
     * Custom divider will be used
     */

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        SlidingMenuAdapter adapter = (SlidingMenuAdapter) parent.getAdapter();
        if (adapter.shouldHideDivider(position)) {
            outRect.set(0, 0, 0, 0);
        } else {
            outRect.bottom = DEVIDER_SIZE;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
