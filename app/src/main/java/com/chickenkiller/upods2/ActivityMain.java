package com.chickenkiller.upods2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.SlidingMenu;
import com.chickenkiller.upods2.views.RecyclerViewCards;

public class ActivityMain extends Activity {

    private RecyclerViewCards rvMain;
    private MediaItemsAdapter adapterRvMain;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        slidingMenu = new SlidingMenu(this, toolbar);

        adapterRvMain = new MediaItemsAdapter(this,R.layout.card_media_item,RadioItem.generateDebugList(200));

        rvMain = (RecyclerViewCards)findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        rvMain.setAdapter(adapterRvMain);
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
