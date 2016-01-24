package com.chickenkiller.upods2.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.dialogs.DialogFragmentAddMediaItem;
import com.chickenkiller.upods2.dialogs.DialogFragmentConfarmation;
import com.chickenkiller.upods2.dialogs.DialogFragmentMessage;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import java.io.File;

/**
 * Created by alonzilberman on 10/22/15.
 * Put all logic for context menu clicks here
 */
public class ContextMenuHelper {

    public static void showAboutPodcastDialog(Podcast podcast, IFragmentsManager fragmentsManager) {
        DialogFragmentMessage dialogFragmentMessage = new DialogFragmentMessage();
        dialogFragmentMessage.setTitle(podcast.getName());
        dialogFragmentMessage.setMessage(podcast.getDescription());
        fragmentsManager.showDialogFragment(dialogFragmentMessage);
    }

    public static void showPodcastInFolder(IPlayableMediaItem mediaItemData, Activity activity) {
        String path = ProfileManager.getInstance().getDownloadedMediaItemPath(mediaItemData);
        Uri selectedUri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "*/*");
        activity.startActivity(intent);
    }

    public static void removeAllDonwloadedEpisods(Activity activity, final Podcast podcast, final IOperationFinishCallback contextItemSelected) {
        DialogFragmentConfarmation dialogFragmentConfarmation = new DialogFragmentConfarmation();
        dialogFragmentConfarmation.setTitle(podcast.getName());
        dialogFragmentConfarmation.setMessage(activity.getString(R.string.remove_podcast_conformation));
        dialogFragmentConfarmation.setPositiveClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = ProfileManager.getInstance().getDownloadedMediaItemPath(podcast);
                GlobalUtils.deleteDirectory(new File(path));
                ProfileManager.getInstance().removeDownloadedMediaItem(podcast);
                contextItemSelected.operationFinished();
            }
        });
        ((IFragmentsManager) activity).showDialogFragment(dialogFragmentConfarmation);
    }

    public static void removeDonwloadedTrack(Activity activity, Track track, IPlayableMediaItem mediaItem,
                                             final IOperationFinishCallback contextItemSelected) {
        ProfileManager.getInstance().removeDownloadedTrack(mediaItem, track);
        contextItemSelected.operationFinished();
        Toast.makeText(activity, R.string.episod_removed, Toast.LENGTH_SHORT).show();
    }

    public static void showAddMediaDialog(Activity activity, MediaItemType mediaItemType) {
        DialogFragmentAddMediaItem dialogFragmentAddMediaItem = new DialogFragmentAddMediaItem();
        dialogFragmentAddMediaItem.setMediaItemType(mediaItemType);
        ((IFragmentsManager) activity).showDialogFragment(dialogFragmentAddMediaItem);
    }

    public static void selectRadioStreamQuality(final Activity activity, final FragmentPlayer fragmentPlayer, final RadioItem currentMediaItem) {
        final RadioItem playableMediaItem = (RadioItem) UniversalPlayer.getInstance().getPlayingMediaItem();
        String[] availableStreams = (playableMediaItem).getAvailableStreams();
        if (availableStreams.length == 0) {
            Toast.makeText(activity, activity.getString(R.string.not_available_for_stream), Toast.LENGTH_SHORT).show();
        } else {
            new MaterialDialog.Builder(activity).title(R.string.select_stream_quality);
            new MaterialDialog.Builder(activity).items(availableStreams);
            new MaterialDialog.Builder(activity).itemsCallbackSingleChoice(((RadioItem) playableMediaItem).getSelectedStreamAsNumber(availableStreams),
                    new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            playableMediaItem.selectStreamUrl(text.toString());
                            currentMediaItem.selectStreamUrl(text.toString());
                            UniversalPlayer.getInstance().softRestart();
                            fragmentPlayer.initPlayerStateUI();
                            return true;
                        }
                    });
            new MaterialDialog.Builder(activity).positiveText(R.string.select);
            new MaterialDialog.Builder(activity).show();
        }
    }

}
