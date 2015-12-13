package com.chickenkiller.upods2.controllers.internet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityMain;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.Logger;
import com.squareup.okhttp.Request;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by alonzilberman on 12/13/15.
 */
public class NetworkTasksService extends IntentService {

    private final static String TAG = "NetworkTasksService";

    public static final String ACTION_CHECK_FOR_NEW_EPISODS = "com.chickenkiller.upods2.service.check_new_episods";
    private static final int NEW_EPISODS_NOTIFICATION_ID = 3829;

    public NetworkTasksService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.printInfo(TAG, "I am starting to run by intent: " + intent.getAction());
        if (intent.getAction().equals(ACTION_CHECK_FOR_NEW_EPISODS)) {
            checkForNewEpisods();
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
                    if (podcast.getEpisodsCount() > parsedEpisods.size() && parsedEpisods.size() > podcast.getEpisodsCount()) {
                        podcast.setNewEpisodsCount(parsedEpisods.size() - podcast.getEpisods().size());
                        podcast.setEpisodsCount(parsedEpisods.size());
                        ProfileManager.getInstance().saveChanges(ProfileManager.ProfileItem.SUBSCRIBDED_PODCASTS);
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

    private void sendNewEpisodsNotification(Podcast podcast) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext());
        nBuilder.setContentTitle(podcast.getName());
        nBuilder.setContentInfo(podcast.getName());
        nBuilder.setContentText(getString(R.string.there_are) + String.valueOf(podcast.getNewEpisodsCount()) + getString(R.string.new_episods_for));

        Intent intentOpen = new Intent(getApplicationContext(), ActivityMain.class);
        PendingIntent piOpen = PendingIntent.getActivity(getApplicationContext(), 0, intentOpen, 0);
        nBuilder.setContentIntent(piOpen);

        Notification notification = nBuilder.build();
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NEW_EPISODS_NOTIFICATION_ID, notification);
    }

}


