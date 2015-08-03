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
public class DetailsScrollView extends ScrollView {

    private int isScrollable; //0 - unknown  1-yes 2-no
    private boolean enabled;
    private boolean isInTheTop;
    private boolean isScrollDown;

    public DetailsScrollView(Context context) {
        super(context);
        this.init();
    }

    public DetailsScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public DetailsScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.isScrollDown = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetailsScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        this.enabled = true;
        this.isInTheTop = true;
        this.isScrollable = 0;
    }

    private float touchY;


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (enabled) {
            if (isScrollable != 2) {
                isInTheTop = getScrollY() == 0;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        isScrollDown = touchY < motionEvent.getY();
                    }
                }
            }

            if (isScrollable == 0) {
                View child = getChildAt(getChildCount() - 1);
                int childHeight = child.getHeight();
                isScrollable = getHeight() < childHeight + getPaddingTop() + getPaddingBottom() ? 1 : 2;
            }
            if ((isScrollDown && isInTheTop) || isScrollable == 2) {
                return false;
            }
            super.onTouchEvent(motionEvent);
            return true;
        }

        return false;
    }
}
