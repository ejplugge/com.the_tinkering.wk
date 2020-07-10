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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.WeakLcoRef;
import com.the_tinkering.wk.util.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * An activity for burning one or more subjects.
 */
public final class BurnActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(BurnActivity.class);

    private List<Long> subjectIds = Collections.emptyList();
    private int todo = 0;
    private int success = 0;
    private int fail = 0;
    private boolean stopped = true;

    private final ViewProxy todoCount = new ViewProxy();
    private final ViewProxy successCount = new ViewProxy();
    private final ViewProxy failCount = new ViewProxy();
    private final ViewProxy status = new ViewProxy();
    private final ViewProxy startButton = new ViewProxy();
    private final ViewProxy stopButton = new ViewProxy();
    private final ViewProxy subjects = new ViewProxy();

    /**
     * The constructor.
     */
    public BurnActivity() {
        super(R.layout.activity_burn, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        todoCount.setDelegate(this, R.id.todoCount);
        successCount.setDelegate(this, R.id.successCount);
        failCount.setDelegate(this, R.id.failCount);
        status.setDelegate(this, R.id.status);
        startButton.setDelegate(this, R.id.startButton);
        stopButton.setDelegate(this, R.id.stopButton);
        subjects.setDelegate(this, R.id.subjects);

        final @Nullable long[] ids = getIntent().getLongArrayExtra("ids");
        if (ids == null || ids.length == 0) {
            finish();
            return;
        }
        subjectIds = new ArrayList<>();
        for (final long id: ids) {
            subjectIds.add(id);
        }
        subjects.setSubjectIds(this, subjectIds);
        todoCount.setText("To do: " + subjectIds.size());
        successCount.setText("Burned: 0");
        failCount.setText("Failed to burn: 0");

        startButton.enableInteraction();
        stopButton.disableInteraction();
    }

    @Override
    protected void onResumeLocal() {
        //
    }

    @Override
    protected void onPauseLocal() {
        stopped = true;
    }

    @Override
    protected void enableInteractionLocal() {
        //
    }

    @Override
    protected void disableInteractionLocal() {
        //
    }

    /**
     * Handler for the start button.
     *
     * @param view the button
     */
    public void start(@SuppressWarnings("unused") final View view) {
        try {
            startButton.disableInteraction();
            stopButton.enableInteraction();
            todo = subjectIds.size();
            success = 0;
            fail = 0;
            stopped = false;
            new Task(this, subjectIds).execute();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the start button.
     *
     * @param view the button
     */
    public void stop(@SuppressWarnings("unused") final View view) {
        try {
            stopButton.disableInteraction();
            stopped = true;
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private static final class Task extends AsyncTask<Void, Object, Void> {
        private final WeakLcoRef<BurnActivity> activityRef;
        private final List<Long> subjectIds;

        private Task(final BurnActivity activity, final List<Long> subjectIds) {
            activityRef = new WeakLcoRef<>(activity);
            this.subjectIds = subjectIds;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                if (WebClient.getInstance().getLastLoginState() != 1) {
                    publishProgress("Status: Logging in...", false, false, -1L);
                    WebClient.getInstance().doLogin();
                    publishProgress("Status: " + WebClient.getInstance().getLastLoginMessage(), false, false, -1L);
                }

                final AppDatabase db = WkApplication.getDatabase();
                int i = 0;
                while (i < subjectIds.size()) {
                    final @Nullable BurnActivity activity = activityRef.getOrElse(null);
                    if (activity == null || activity.stopped) {
                        break;
                    }
                    final long id = subjectIds.get(0);
                    final @Nullable Subject subject = db.subjectDao().getById(id);
                    if (WebClient.getInstance().getLastLoginState() != 1) {
                        publishProgress(null, false, true, -1L);
                        i++;
                    }
                    else if (subject == null || !subject.isBurnable()) {
                        publishProgress(null, true, false, id);
                        subjectIds.remove(i);
                    }
                    else {
                        final CharSequence title = subject.getInfoTitle("Status: Burning: ", "");
                        publishProgress(title, false, false, -1L);
                        if (WebClient.getInstance().burn(subject)) {
                            publishProgress(null, true, false, id);
                            subjectIds.remove(i);
                        }
                        else {
                            publishProgress(null, false, true, -1L);
                            i++;
                        }
                    }
                    if (subject != null) {
                        db.subjectSyncDao().patchAssignment(id, subject.getSrsSystem().getFirstStartedStage().getId(),
                                subject.getUnlockedAt(), subject.getStartedAt(), new Date(),
                                subject.getPassedAt(), subject.getBurnedAt(), new Date());
                    }
                }
                publishProgress("Status: Finished", false, false, -1L);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Object... values) {
            try {
                final @Nullable CharSequence status = (CharSequence) values[0];
                if (status != null) {
                    activityRef.get().status.setText(status);
                }
                if ((boolean) values[1]) {
                    activityRef.get().todo--;
                    activityRef.get().success++;
                    activityRef.get().todoCount.setText("To do: " + activityRef.get().todo);
                    activityRef.get().successCount.setText("Burned: " + activityRef.get().success);
                    activityRef.get().failCount.setText("Failed to burn: " + activityRef.get().fail);
                }
                if ((boolean) values[2]) {
                    activityRef.get().todo--;
                    activityRef.get().fail++;
                    activityRef.get().todoCount.setText("To do: " + activityRef.get().todo);
                    activityRef.get().successCount.setText("Burned: " + activityRef.get().success);
                    activityRef.get().failCount.setText("Failed to burn: " + activityRef.get().fail);
                }
                final long id = (long) values[3];
                if (id != -1) {
                    activityRef.get().subjects.removeSubject(id);
                }
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        @Override
        protected void onPostExecute(final Void result) {
            try {
                activityRef.get().startButton.enableInteraction();
                activityRef.get().stopButton.disableInteraction();
                if (subjectIds.isEmpty()) {
                    activityRef.get().finish();
                }
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
