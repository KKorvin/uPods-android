package com.chickenkiller.upods2;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.models.RadioItem;

public class ActivityMain extends Activity {

    private GridView gvMain;
    private MediaItemsAdapter adapterGvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gvMain = (GridView)findViewById(R.id.gvMain);
        adapterGvMain = new MediaItemsAdapter(this,R.layout.item_main_greed,RadioItem.generateDebugList(20));
        gvMain.setAdapter(adapterGvMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
