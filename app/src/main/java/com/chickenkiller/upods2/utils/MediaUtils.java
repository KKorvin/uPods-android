package com.chickenkiller.upods2.utils;

import android.app.Activity;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.dialogs.DialogFragmentMessage;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.enums.Direction;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by Alon Zilberman on 10/30/15.
 */
public class MediaUtils {

    private static final String LOG_TAG = "MediaUtils";

    public static String extractMp3FromFile(String m3uUrl) throws IOException, JSONException {
        Request request = new Request.Builder().url(m3uUrl).build();
        String response = BackendManager.getInstance().sendSimpleSynchronicRequest(request);
        List<String> allURls = GlobalUtils.extractUrls(response);
        String mp3Url = allURls.size() > 0 ? allURls.get(0) : "";
        Logger.printInfo(LOG_TAG, "Extracted from file urls: " + allURls.toString());
        return mp3Url;
    }

    public static String formatMsToTimeString(int millis) {
        return String.format("%02d:%02d",
                MILLISECONDS.toMinutes(millis),
                MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(MILLISECONDS.toMinutes(millis))
        );
    }

    public static long timeStringToLong(String timeString) {
        if (timeString.matches("^[0-9]{3,5}$")) {//Time was given in seconds
            return Long.valueOf(timeString) * 1000;
        }
        if (timeString.matches("[0-9]{2}:[0-9]{2}")) {
            timeString = "00:" + timeString;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        long ms;
        try {
            date = sdf.parse("1970-01-01 " + timeString);
            ms = date.getTime();
        } catch (ParseException e) {
            ms = FragmentPlayer.DEFAULT_RADIO_DURATIO;
            e.printStackTrace();
        }
        return ms;
    }

    public static int calculateNextTrackNumber(Direction direction, int currentTrackNumber, int tracksSize) {
        int changeTo = 0;
        if (direction == Direction.LEFT) {
            changeTo = currentTrackNumber - 1;
            if (changeTo < 0) {
                changeTo = tracksSize - 1;
            }
        } else {
            changeTo = currentTrackNumber + 1;
            if (changeTo >= tracksSize) {
                changeTo = 0;
            }
        }
        return changeTo;
    }

    public static boolean isVideoUrl(String link) {
        return link.matches(".*\\.(mpeg|avi|mp4|3gp|webm|mkv|ogg|wma)$");
    }

    public static IOperationFinishCallback getPlayerFailCallback(final Activity activity, final MediaItem playableMediaItem) {
        return new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                UniversalPlayer.getInstance().releasePlayer();
                StringBuilder mesStringBuilder = new StringBuilder();
                mesStringBuilder.append(activity.getString(R.string.cant_play));
                mesStringBuilder.append(" ");
                mesStringBuilder.append(playableMediaItem instanceof Podcast
                        ? activity.getString(R.string.podcast_small) : activity.getString(R.string.radio_station));
                mesStringBuilder.append(":( ");
                mesStringBuilder.append(activity.getString(R.string.please_try_later));
                DialogFragmentMessage dialogFragmentMessage = new DialogFragmentMessage();
                dialogFragmentMessage.setMessage(mesStringBuilder.toString());
                dialogFragmentMessage.setTitle(activity.getString(R.string.oops));
                dialogFragmentMessage.setOnOkClicked(new IOperationFinishCallback() {
                    @Override
                    public void operationFinished() {
                        activity.onBackPressed();
                        UniversalPlayer.getInstance().releasePlayer();
                    }
                });
                ((IFragmentsManager) activity).showDialogFragment(dialogFragmentMessage);
            }
        };
    }

}
