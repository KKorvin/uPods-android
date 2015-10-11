package com.chickenkiller.upods2.models;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.BackendManager;
import com.chickenkiller.upods2.controllers.BannerItemsAdapter;
import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.utils.ServerApi;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 9/1/15.
 */
public class ViewHolderBannersLayout extends RecyclerView.ViewHolder {
    public static final int BANNER_SCROLL_TIME = 5000;//ms
    private RecyclerViewPager rvBanners;
    private BannerItemsAdapter bannerItemsAdapter;
    private LinearLayoutManager layoutManager;
    private IFragmentsManager fragmentsManager;
    private IContentLoadListener contentLoadListener;
    private boolean needDestroy = false;


    public ViewHolderBannersLayout(View view, IFragmentsManager fragmentsManager, IContentLoadListener contentLoadListener) {
        super(view);
        this.rvBanners = (RecyclerViewPager) view.findViewById(R.id.rvBanners);
        this.layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        this.rvBanners.setLayoutManager(layoutManager);
        this.rvBanners.setHasFixedSize(true);
        this.fragmentsManager = fragmentsManager;
        this.needDestroy = false;
        this.contentLoadListener = contentLoadListener;
        loadBanners(view.getContext());
    }

    public void setNeedDestroy(boolean needDestroy) {
        this.needDestroy = needDestroy;
    }

    private void loadBanners(final Context mContext) {
        BackendManager.getInstance().loadTops(BackendManager.TopType.MAIN_BANNER, ServerApi.RADIO_TOP, new IRequestCallback() {
                    @Override
                    public void onRequestSuccessed(final JSONObject jResponse) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<RadioItem> topRadioStations = RadioItem.withJsonArray(jResponse.getJSONArray("result"), mContext);
                                    bannerItemsAdapter = new BannerItemsAdapter(mContext, R.layout.baner_item, topRadioStations);
                                    if (fragmentsManager != null) {
                                        bannerItemsAdapter.setFragmentsManager(fragmentsManager);
                                    }
                                    rvBanners.setAdapter(bannerItemsAdapter);
                                    layoutManager.scrollToPosition(bannerItemsAdapter.MIDDLE);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            if (needDestroy) {
                                                return;
                                            }
                                            rvBanners.smoothScrollToPosition(rvBanners.getCurrentPosition() + 1);
                                            handler.postDelayed(this, BANNER_SCROLL_TIME);
                                        }
                                    }, BANNER_SCROLL_TIME);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                contentLoadListener.onContentLoaded();
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed() {

                    }
                }
        );
    }
}
