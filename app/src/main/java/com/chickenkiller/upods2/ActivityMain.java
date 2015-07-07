package com.chickenkiller.upods2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chickenkiller.upods2.controllers.BanerItemsAdapter;
import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.models.BanerItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.SlidingMenu;
import com.chickenkiller.upods2.views.AutofitRecyclerView;

public class ActivityMain extends Activity {

    private AutofitRecyclerView rvMain;
    private RecyclerView rvBanners;
    private MediaItemsAdapter mediaItemsAdapter;
    private BanerItemsAdapter banerItemsAdapter;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        slidingMenu = new SlidingMenu(this, toolbar);

        mediaItemsAdapter = new MediaItemsAdapter(this, R.layout.card_media_item,
                R.layout.media_item_title, RadioItem.generateDebugList(40, this));
        banerItemsAdapter = new BanerItemsAdapter(this, R.layout.baner_item, BanerItem.generateDebugList(1));

        rvMain = (AutofitRecyclerView) findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(mediaItemsAdapter);
        rvMain.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mediaItemsAdapter.getItemViewType(position);
                return viewType != MediaItemsAdapter.HEADER ? 1 : rvMain.getSpanCount();
            }
        });

        rvBanners = (RecyclerView) findViewById(R.id.rvBanners);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvBanners.setLayoutManager(layoutManager);
        rvBanners.setHasFixedSize(true);
        rvBanners.setAdapter(banerItemsAdapter);

        layoutManager.scrollToPosition(banerItemsAdapter.MIDDLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
