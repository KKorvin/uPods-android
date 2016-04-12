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
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Feed;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;

import java.net.URL;
import java.util.ArrayList;

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
     * Will check for new episodes by fetching full episodes list for each subscribed podcast
     */
    private void checkForNewEpisods() {
        Logger.printInfo(TAG, "Checking for new episodes...");
        ArrayList<Podcast> subscribedPodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
        if (subscribedPodcasts.size() > 0) {
            for (Podcast podcast : subscribedPodcasts) {
                try {
                    ArrayList<Episode> parsedEpisodes = BackendManager.getInstance().fetchEpisodes(podcast.getFeedUrl());
                    if (Feed.handleUpdates(parsedEpisodes, podcast)) {
                        sendNewEpisodsNotification(podcast);
                    }
                    Logger.printError(TAG, "Finished for podcast with url: " + podcast.getFeedUrl());
                } catch (Exception e) {
                    Logger.printError(TAG, "Can't check for updates for podcast with url: " + podcast.getFeedUrl());
                    e.printStackTrace();
                }
            }
        }
        Logger.printInfo(TAG, "Finished!");
    }

    private void sendNewEpisodsNotification(final Podcast podcast) {
        StringBuilder titleStringBuilder = new StringBuilder();
        titleStringBuilder.append(podcast.getNewEpisodsCount());
        titleStringBuilder.append(" ");
        titleStringBuilder.append(podcast.getNewEpisodsCount() > 0 ? getString(R.string.new_episodes) : getString(R.string.new_episode));

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setContentTitle(podcast.getName());
        nBuilder.setContentInfo(podcast.getName());
        nBuilder.setContentText(titleStringBuilder.toString());
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


