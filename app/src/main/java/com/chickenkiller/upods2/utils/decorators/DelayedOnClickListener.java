package com.chickenkiller.upods2.utils.decorators;

import android.os.Handler;
import android.view.View;

/**
 * Decorated to run OnClickListener with delay
 * Created by Alon Zilberman on 1/20/16.
 */
public class DelayedOnClickListener implements View.OnClickListener {

    private static final int DEFAULT_DELAY = 300;
    private View.OnClickListener onClickListener;
    private int delay;

    public DelayedOnClickListener(View.OnClickListener onClickListener) {
        this.delay = DEFAULT_DELAY;
        this.onClickListener = onClickListener;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public void onClick(final View v) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onClickListener.onClick(v);
            }
        }, delay);
    }
}
