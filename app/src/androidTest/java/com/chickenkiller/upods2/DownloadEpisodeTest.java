package com.chickenkiller.upods2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.HelpFunctions;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by alonzilberman on 29/03/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DownloadEpisodeTest {

    private static final String TEST_FEED1 = "http://mediaweb.musicradio.com/RSSFeed.xml?Channel=5966";

    @Test
    public void checkBasicDownloadRemoveScenario() {
        //EMULATE downloading episode -> remove episode
        try {
            UpodsApplication.initAllResources();

            String podcastName = String.valueOf(System.currentTimeMillis()) + "_podcast";
            Podcast podcast = new Podcast(podcastName, TEST_FEED1);
            ArrayList<Episode> episodes = HelpFunctions.parseEpisodes(podcast.getFeedUrl());

            ProfileManager.getInstance().addDownloadedTrack(podcast, episodes.get(0));
            assertTrue(podcast.isDownloaded);
            assertTrue(episodes.get(0).isDownloaded);

            assertTrue(podcast.id >= 0);
            assertTrue(episodes.get(0).id >= 0);

            long cachedId = episodes.get(0).id;
            ProfileManager.getInstance().removeDownloadedTrack(podcast, episodes.get(0));

            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
            String[] args = {String.valueOf(cachedId)};
            Cursor cursor = database.rawQuery("SELECT * FROM podcasts_episodes_rel WHERE episode_id = ?", args);
            assertTrue(cursor.getCount() == 0);

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
