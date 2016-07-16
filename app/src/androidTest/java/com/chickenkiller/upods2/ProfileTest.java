package com.chickenkiller.upods2;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.StreamUrl;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Created by alonzilberman on 13/03/2016.
 * This tests check only basic functionality of profile manager.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ProfileTest {

    private static final String TEST_FEED = ServerApi.DOMAIN + "/static/podcasts/feed/test_podcast.xml";

    @Test
    public void checkSubscribedScenario() {
        UpodsApplication.initAllResources();

        //Testing podcast
        String podcastName = String.valueOf(System.currentTimeMillis()) + "_podcast";
        Podcast podcast = new Podcast(podcastName, TEST_FEED);

        ProfileManager.getInstance().addSubscribedMediaItem(podcast);
        boolean isPodcastInFavorites = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedPodcasts(), podcast);
        Logger.printInfo("testSubscribed", "Testing adding podcast to subscribed");
        assertTrue(isPodcastInFavorites);

        ProfileManager.getInstance().removeSubscribedMediaItem(podcast);
        isPodcastInFavorites = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedPodcasts(), podcast);
        Logger.printInfo("testSubscribed", "Testing removing podcast from subscribed");
        assertTrue(!isPodcastInFavorites);

        //Testing radio
        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addSubscribedMediaItem(radioItem);
        boolean isRadiotInFavorites = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedRadioItems(), radioItem);
        Logger.printInfo("testSubscribed", "Testing adding radio to subscribed");
        assertTrue(isRadiotInFavorites);

        ProfileManager.getInstance().removeSubscribedMediaItem(radioItem);
        isRadiotInFavorites = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedRadioItems(), radioItem);
        Logger.printInfo("testSubscribed", "Testing removing radio to subscribed");
        assertTrue(!isRadiotInFavorites);

    }

    @Test
    public void checkDownloadedScenario() {
        UpodsApplication.initAllResources();

        //Testing podcast
        String podcastName = String.valueOf(System.currentTimeMillis()) + "_podcast";
        String episodeName = String.valueOf(System.currentTimeMillis()) + "_episode";

        Podcast podcast = new Podcast(podcastName, TEST_FEED);
        Episode episode = new Episode();
        episode.setTitle(episodeName);

        ProfileManager.getInstance().addDownloadedTrack(podcast, episode);
        boolean isEpisodeDownloaded = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getDownloadedPodcasts(), podcast);
        Logger.printInfo("checkDownloadedScenario", "Testing adding downloaded episode");
        assertTrue(isEpisodeDownloaded);

        ProfileManager.getInstance().removeDownloadedTrack(podcast, episode);
        isEpisodeDownloaded = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getDownloadedPodcasts(), podcast);
        Logger.printInfo("checkDownloadedScenario", "Testing removing downloaded episode");
        assertTrue(!isEpisodeDownloaded);
    }

    @Test
    public void checkRecentScenario() {
        UpodsApplication.initAllResources();

        //Testing radio
        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addRecentMediaItem(radioItem);
        boolean isRecentStation = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getRecentRadioItems(), radioItem);
        Logger.printInfo("checkRecentScenario", "Testing adding radio to recent");
        assertTrue(isRecentStation);

        ProfileManager.getInstance().removeRecentMediaItem(radioItem);
        isRecentStation = MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getRecentRadioItems(), radioItem);
        Logger.printInfo("checkRecentScenario", "Testing removing radio from recent");
        assertTrue(!isRecentStation);
    }
}
