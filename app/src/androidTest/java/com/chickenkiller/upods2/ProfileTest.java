package com.chickenkiller.upods2;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.StreamUrl;
import com.chickenkiller.upods2.utils.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Created by alonzilberman on 13/03/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ProfileTest {

    private static final String TEST_FEED = "https://upods.io/static/podcasts/feed/test_podcast.xml";

    @Test
    public void checkSubscribedScenario() {
        //Testing podcast
        String podcastName = String.valueOf(System.currentTimeMillis()) + "_podcast";
        Podcast podcast = new Podcast(podcastName, TEST_FEED);

        ProfileManager.getInstance().addSubscribedMediaItem(podcast);
        boolean isPodcastInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(podcast);
        Logger.printInfo("testSubscribed", "Testing adding podcast to subscribed");
        assertTrue(isPodcastInFavorites);

        ProfileManager.getInstance().removeSubscribedMediaItem(podcast);
        isPodcastInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(podcast);
        Logger.printInfo("testSubscribed", "Testing removing podcast from subscribed");
        assertTrue(!isPodcastInFavorites);

        //Testing radio
        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addSubscribedMediaItem(radioItem);
        boolean isRadiotInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
        Logger.printInfo("testSubscribed", "Testing adding radio to subscribed");
        assertTrue(isRadiotInFavorites);

        ProfileManager.getInstance().removeSubscribedMediaItem(radioItem);
        isRadiotInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
        Logger.printInfo("testSubscribed", "Testing removing radio to subscribed");
        assertTrue(!isRadiotInFavorites);

    }

    @Test
    public void checkDownloadedScenario() {
        //Testing podcast
        String podcastName = String.valueOf(System.currentTimeMillis()) + "_podcast";
        String episodeName = String.valueOf(System.currentTimeMillis()) + "_episode";

        Podcast podcast = new Podcast(podcastName, TEST_FEED);
        Episode episode = new Episode();
        episode.setTitle(episodeName);

        ProfileManager.getInstance().addDownloadedTrack(podcast, episode);
        boolean isEpisodeDownloaded = ProfileManager.getInstance().isDownloaded(podcast, episode);
        Logger.printInfo("checkDownloadedScenario", "Testing adding downloaded episode");
        assertTrue(isEpisodeDownloaded);

        ProfileManager.getInstance().removeDownloadedTrack(podcast, episode);
        isEpisodeDownloaded = ProfileManager.getInstance().isDownloaded(podcast, episode);
        Logger.printInfo("checkDownloadedScenario", "Testing removing downloaded episode");
        assertTrue(!isEpisodeDownloaded);
    }

    @Test
    public void checkRecentScenario() {
        //Testing radio
        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addRecentMediaItem(radioItem);
        boolean isRecentStation = ProfileManager.getInstance().isRecentMediaItem(radioItem);
        Logger.printInfo("checkRecentScenario", "Testing adding radio to recent");
        assertTrue(isRecentStation);

        ProfileManager.getInstance().removeRecentMediaItem(radioItem);
        isRecentStation = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
        Logger.printInfo("checkRecentScenario", "Testing removing radio from recent");
        assertTrue(!isRecentStation);
    }
}
