package com.chickenkiller.upods2.controllers.player;

import android.os.AsyncTask;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Logger;

/**
 * Created by Alon Zilberman on 11/24/15.
 */
public class PlayerPositionUpdater extends AsyncTask<Void, Integer, Void> {

    protected static final long POSITION_UPDATE_RATE = 800;
    protected static final int SAVE_POSITION_RATE = 5;

    protected IOnPositionUpdatedCallback positionUpdatedCallback;
    protected UniversalPlayer universalPlayer;
    private long radioOfset;

    public PlayerPositionUpdater(IOnPositionUpdatedCallback positionUpdatedCallback) {
        this.positionUpdatedCallback = positionUpdatedCallback;
        this.universalPlayer = UniversalPlayer.getInstance();
        this.radioOfset = 0;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (!isCancelled() && positionUpdatedCallback != null) {
                if (universalPlayer.isPrepaired) {
                    int position = universalPlayer.getCurrentPosition();
                    if (position >= FragmentPlayer.DEFAULT_RADIO_DURATIO + radioOfset && universalPlayer.getPlayingMediaItem() instanceof RadioItem) {
                        radioOfset += FragmentPlayer.DEFAULT_RADIO_DURATIO;
                        position -= radioOfset;
                    }

                    publishProgress(position);

                    if (universalPlayer.getPlayingMediaItem() instanceof Podcast && position % SAVE_POSITION_RATE == 0) {
                        Track track = ((Podcast) universalPlayer.getPlayingMediaItem()).getSelectedTrack();
                        ProfileManager.getInstance().saveTrackPosition(track, position);
                    }
                }
                Thread.sleep(POSITION_UPDATE_RATE);
            }
        } catch (Exception e) {
            Logger.printError("PlayerPositionUpdater", "Failed to update progress:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        positionUpdatedCallback.poistionUpdated(progress[0]);
        super.onProgressUpdate(progress[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        positionUpdatedCallback.poistionUpdaterStoped();
    }
}
