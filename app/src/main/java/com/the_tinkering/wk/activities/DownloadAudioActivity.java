/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.the_tinkering.wk.activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.util.Function;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.AudioDownloadStatus;
import com.the_tinkering.wk.jobs.AbortAudioDownloadJob;
import com.the_tinkering.wk.jobs.DeleteAllAudioJob;
import com.the_tinkering.wk.jobs.ScanAudioDownloadStatusJob;
import com.the_tinkering.wk.livedata.LiveAudioDownloadStatus;
import com.the_tinkering.wk.livedata.LiveAudioMoveStatus;
import com.the_tinkering.wk.livedata.LiveTaskCounts;
import com.the_tinkering.wk.model.TaskCounts;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.services.JobRunnerService;
import com.the_tinkering.wk.util.AudioUtil;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.WeakLcoRef;
import com.the_tinkering.wk.views.DownloadAudioBracketView;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.the_tinkering.wk.Constants.DELETE_AUDIO_WARNING;
import static com.the_tinkering.wk.util.TextUtil.renderHtml;

/**
 * An activity for starting background downloads of pronunciation audio files.
 */
public final class DownloadAudioActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(DownloadAudioActivity.class);

    private final ViewProxy moveProgressBar = new ViewProxy();
    private final ViewProxy cancelButton = new ViewProxy();
    private final ViewProxy deleteButton = new ViewProxy();
    private final ViewProxy moveButton = new ViewProxy();
    private final ViewProxy abortMoveButton = new ViewProxy();
    private final ViewProxy header = new ViewProxy();
    private final ViewProxy activeDownloads = new ViewProxy();
    private final ViewProxy downloadAudioView = new ViewProxy();

    /**
     * The constructor.
     */
    public DownloadAudioActivity() {
        super(R.layout.activity_download_audio, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        moveProgressBar.setDelegate(this, R.id.moveProgressBar);
        cancelButton.setDelegate(this, R.id.cancelButton);
        deleteButton.setDelegate(this, R.id.deleteButton);
        moveButton.setDelegate(this, R.id.moveButton);
        abortMoveButton.setDelegate(this, R.id.abortMoveButton);
        header.setDelegate(this, R.id.header);
        activeDownloads.setDelegate(this, R.id.activeDownloads);
        downloadAudioView.setDelegate(this, R.id.downloadAudioView);

        JobRunnerService.schedule(ScanAudioDownloadStatusJob.class, "");

        LiveAudioDownloadStatus.getInstance().observe(this, new Observer<List<AudioDownloadStatus>>() {
            @Override
            public void onChanged(final @Nullable List<AudioDownloadStatus> t) {
                try {
                    if (t != null) {
                        updateOverviewDisplay(t);
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });

        LiveTaskCounts.getInstance().observe(this, new Observer<TaskCounts>() {
            @Override
            public void onChanged(final @Nullable TaskCounts t) {
                try {
                    LiveAudioDownloadStatus.getInstance().ping();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });

        LiveAudioMoveStatus.getInstance().observe(this, new Observer<Object>() {
            @Override
            public void onChanged(final Object t) {
                try {
                    if (LiveAudioMoveStatus.getInstance().isActive()) {
                        moveButton.disableInteraction();
                        abortMoveButton.enableInteraction();
                        moveProgressBar.setVisibility(true);
                    }
                    else {
                        moveButton.enableInteraction();
                        abortMoveButton.disableInteraction();
                        moveProgressBar.setVisibility(false);
                    }
                    final int total = LiveAudioMoveStatus.getInstance().getNumTotal();
                    final int done = LiveAudioMoveStatus.getInstance().getNumDone();
                    if (total != moveProgressBar.getMax() && total > 0) {
                        moveProgressBar.setMax(total);
                    }
                    moveProgressBar.setProgress(Math.min(done, total));
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });
    }

    @Override
    protected void onResumeLocal() {
        updateOverviewDisplay(LiveAudioDownloadStatus.getInstance().get());
    }

    @Override
    protected void onPauseLocal() {
        LiveAudioMoveStatus.getInstance().setActive(false);
        LiveAudioMoveStatus.getInstance().forceUpdate();
    }

    @Override
    protected void enableInteractionLocal() {
        //
    }

    @Override
    protected void disableInteractionLocal() {
        //
    }

    private void updateOverviewDisplay(final Collection<AudioDownloadStatus> overview) {
        final int audioCount = LiveTaskCounts.getInstance().get().getAudioCount();

        header.setText("You can download vocabulary audio here, which will be played during lessons and reviews."
                + " Once started, downloads will continue in the background.");

        activeDownloads.setTextFormat("Active downloads: %d", audioCount);

        cancelButton.setVisibility(audioCount > 0);
        deleteButton.setVisibility(audioCount == 0);

        int i = 0;
        final int maxLevel = Math.max(overview.size(), 60);

        for (int j=1; j<=maxLevel; j+=10) {
            if (i >= downloadAudioView.getChildCount()) {
                final DownloadAudioBracketView view = new DownloadAudioBracketView(this);
                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
                layoutParams.setMargins(0, 0, 0, 0);
                layoutParams.width = WRAP_CONTENT;
                layoutParams.height = WRAP_CONTENT;
                downloadAudioView.addView(view, layoutParams);
            }
            final @Nullable DownloadAudioBracketView view = (DownloadAudioBracketView) downloadAudioView.getChildAt(i);
            if (view != null) {
                view.setBracket(overview, j, j+9);
            }
            i++;
        }

        while (i < downloadAudioView.getChildCount()) {
            downloadAudioView.removeViewAt(i);
        }

        if (AudioUtil.hasAnyMisplacedAudioFiles()) {
            moveButton.enableInteraction();
        }
        else {
            moveButton.disableInteraction();
        }
    }

    /**
     * Handler for the abort button.
     *
     * @param view the button
     */
    @SuppressWarnings("MethodMayBeStatic")
    public void onCancel(@SuppressWarnings("unused") final View view) {
        try {
            JobRunnerService.schedule(AbortAudioDownloadJob.class, "");
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the delete button.
     *
     * @param view the button
     */
    public void onDelete(@SuppressWarnings("unused") final View view) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete all audio?")
                    .setMessage(renderHtml(DELETE_AUDIO_WARNING))
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            //
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            try {
                                JobRunnerService.schedule(DeleteAllAudioJob.class, "");
                            } catch (final Exception e) {
                                LOGGER.uerr(e);
                            }
                        }
                    }).create().show();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the move button.
     *
     * @param view the button
     */
    public void onMove(@SuppressWarnings("unused") final View view) {
        try {
            LiveAudioMoveStatus.getInstance().setActive(true);
            LiveAudioMoveStatus.getInstance().setNumDone(0);
            LiveAudioMoveStatus.getInstance().setNumTotal(100);
            LiveAudioMoveStatus.getInstance().forceUpdate();
            new Task(this).execute();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the abort move button.
     *
     * @param view the button
     */
    @SuppressWarnings("MethodMayBeStatic")
    public void onAbortMove(@SuppressWarnings("unused") final View view) {
        try {
            LiveAudioMoveStatus.getInstance().setActive(false);
            LiveAudioMoveStatus.getInstance().forceUpdate();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private static final class Task extends AsyncTask<Void, Void, Void> {
        private final WeakLcoRef<DownloadAudioActivity> activityRef;

        private Task(final DownloadAudioActivity activity) {
            activityRef = new WeakLcoRef<>(activity);
        }

        @Override
        protected @Nullable Void doInBackground(final Void... params) {
            try {
                final int numTotal = AudioUtil.getNumMisplacedAudioFiles();
                if (numTotal <= 0) {
                    return null;
                }
                LiveAudioMoveStatus.getInstance().setNumTotal(numTotal);
                LiveAudioMoveStatus.getInstance().forceUpdate();

                AudioUtil.iterateMisplacedAudioFiles(new Function<File, Boolean>() {
                    @Override
                    public Boolean apply(final File input) {
                        AudioUtil.moveToPreferredLocation(input);
                        LiveAudioMoveStatus.getInstance().setNumDone(LiveAudioMoveStatus.getInstance().getNumDone() + 1);
                        LiveAudioMoveStatus.getInstance().forceUpdate();
                        return !LiveAudioMoveStatus.getInstance().isActive();
                    }
                });
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final @Nullable Void result) {
            try {
                LiveAudioMoveStatus.getInstance().setActive(false);
                LiveAudioMoveStatus.getInstance().forceUpdate();
                activityRef.get().moveProgressBar.setVisibility(false);
                activityRef.get().moveButton.enableInteraction();
                activityRef.get().abortMoveButton.disableInteraction();
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
