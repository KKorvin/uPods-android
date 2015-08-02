package com.chickenkiller.upods2.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by alonzilberman on 7/21/15.
 */
public class ControllableScrollView extends ScrollView {

    private int isScrollable; //0 - unknown  1-yes 2-no
    private boolean enabled;
    private boolean isInTheTop;
    private boolean isTouchDown;

    public ControllableScrollView(Context context) {
        super(context);
        this.init();
    }

    public ControllableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ControllableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isTouchDown = false;
        this.enabled = enabled;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ControllableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        this.enabled = true;
        this.isInTheTop = true;
        this.isScrollable = 0;
    }


    @Override
    protected void onScrollChanged(int w, int h, int ow, int oh) {
        View view = getChildAt(getChildCount() - 1);
        isInTheTop = view.getTop() == h ? true : false;
        if (h <= oh) {
            isTouchDown = true;
        } else {
            isTouchDown = false;
        }
        super.onScrollChanged(w, h, ow, oh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (enabled) {
            if (isScrollable == 0) {
                View child = getChildAt(getChildCount() - 1);
                int childHeight = child.getHeight();
                isScrollable = getHeight() < childHeight + getPaddingTop() + getPaddingBottom() ? 1 : 2;
            }
            if ((isTouchDown && isInTheTop) || isScrollable == 2) {
                return false;
            }
            super.onTouchEvent(motionEvent);
            return true;
        }
        return false;
    }


}
