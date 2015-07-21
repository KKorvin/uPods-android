package com.chickenkiller.upods2.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by alonzilberman on 7/21/15.
 */
public class ControllableScrollView extends ScrollView {

    private boolean enabled;

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
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ControllableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (enabled) {
            super.onTouchEvent(ev);
            return true;
        }
        return false;
    }
}
