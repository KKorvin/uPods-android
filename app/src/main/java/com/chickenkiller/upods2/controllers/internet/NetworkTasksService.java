package com.chickenkiller.upods2.controllers.internet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityMain;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SimpleCacheManager;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Request;

/**
 * Created by alonzilberman on 12/13/15.
 */
public class NetworkTasksService extends IntentService {

    private final static String TAG = "NetworkTasksService";
    private final static String WAKE_LOCK_NAME = "com.chickenkiller.upods2.WAKE_LOCK";
    private static final int LARGE_ICON_SIZE = UIHelper.dpToPixels(80);

    public static final String ACTION_CHECK_FOR_NEW_EPISODS = "com.chickenkiller.upods2.service.check_new_episods";
    public static final int NOTIFICATIONS_SHOW_PODCASTS_SUBSCRIBED = 3501;

    private static final int NEW_EPISODS_NOTIFICATION_ID = 3829;

    private PowerManager.WakeLock wakeLock = null;

    public NetworkTasksService() {
        super(TAG);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.printInfo(TAG, "I am created! ");
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        wakeLock.setReferenceCounted(true);
    }

    @Override
    public void onStart(Intent intent, final int startId) {
        wakeLock.acquire();
        super.onStart(intent, startId);
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.printInfo(TAG, "I am starting to run by intent: " + intent.getAction());
        if (intent.getAction().equals(ACTION_CHECK_FOR_NEW_EPISODS)) {
            checkForNewEpisods();
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }


    /**
     * Will check for new episods by fetching full episods list for each subscribed podcast
     */
    private void checkForNewEpisods() {
        Logger.printInfo(TAG, "Checking for new episods...");
        ArrayList<Podcast> subscribedPodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
        if (subscribedPodcasts.size() > 0) {
            for (Podcast podcast : subscribedPodcasts) {
                Request request = new Request.Builder().url(podcast.getFeedUrl()).build();
                try {
                    String response = BackendManager.getInstance().sendSimpleSynchronicRequest(request);
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    EpisodsXMLHandler episodsXMLHandler = new EpisodsXMLHandler();
                    xr.setContentHandler(episodsXMLHandler);

                    //TODO could be encoding problem
                    InputSource inputSource = new InputSource(new StringReader(response));
                    xr.parse(inputSource);
                    ArrayList<Episod> parsedEpisods = episodsXMLHandler.getParsedEpisods();
                    int episodesCount = Integer.valueOf(podcast.getTrackCount());
                    if (episodesCount != 0 && parsedEpisods.size() > episodesCount) {//
                        int newEpisodesCount = parsedEpisods.size() - episodesCount + podcast.getNewEpisodsCount();
                        for (int i = parsedEpisods.size() - newEpisodesCount; i < parsedEpisods.size(); i++) {
                            podcast.addNewEpisodsTitle(parsedEpisods.get(i).getTitle());
                        }
                        ProfileManager.getInstance().saveChanges(ProfileManager.ProfileItem.SUBSCRIBDED_PODCASTS);
                        SimpleCacheManager.getInstance().removeFromCache(podcast.getFeedUrl());
                        sendNewEpisodsNotification(podcast);
                        //TODO automaticly download new episods here
                    }
                } catch (Exception e) {
                    Logger.printError(TAG, "Can't check for updates for podcast with url: " + podcast.getFeedUrl());
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNewEpisodsNotification(final Podcast podcast) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle(podcast.getName());
        nBuilder.setContentInfo(podcast.getName());
        nBuilder.setContentText(String.valueOf(podcast.getNewEpisodsCount()) + " " + getString(R.string.new_episods));
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);

        try {
            Bitmap icon = null;
            if (podcast.getCoverImageUrl() != null) {
                URL url = new URL(podcast.getCoverImageUrl());
                icon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } else {
                LetterBitmap letterBitmap = new LetterBitmap(this);
                icon = letterBitmap.getLetterTile(podcast.getName(), podcast.getName(), LARGE_ICON_SIZE, LARGE_ICON_SIZE);
            }
            nBuilder.setLargeIcon(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intentOpen = new Intent(getApplicationContext(), ActivityMain.class);
        intentOpen.putExtra(ActivityPlayer.ACTIVITY_STARTED_FROM, NOTIFICATIONS_SHOW_PODCASTS_SUBSCRIBED);

        PendingIntent piOpen = PendingIntent.getActivity(this, 0, intentOpen, PendingIntent.FLAG_CANCEL_CURRENT);
        nBuilder.setContentIntent(piOpen);

        final Notification notification = nBuilder.build();
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NEW_EPISODS_NOTIFICATION_ID, notification);
    }

}


