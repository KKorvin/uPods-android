package com.chickenkiller.upods2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by alonzilberman on 7/6/15.
 */
public class ImageViewBanner extends ImageView {
    public ImageViewBanner(Context context) {
        super(context);
    }

    public ImageViewBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*@Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = 2000;
        int ratio = getMeasuredWidth() / width;
        int height = ratio > 0 ? getMeasuredHeight() / ratio : heightMeasureSpec;
        super.onMeasure(width, height);
    }*/
}
