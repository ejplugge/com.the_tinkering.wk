/*
 * Copyright 2019-2022 Ernst Jan Plugge <rmc@dds.nl>
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

import static com.the_tinkering.wk.util.ObjectSupport.runAsyncWithProgress;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.lifecycle.Lifecycle;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * An activity for burning one or more subjects.
 */
public final class BurnActivity extends AbstractActivity {
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

        final long[] ids = getIntent().getLongArrayExtra("ids");
        if (ids == null || ids.length == 0) {
            finish();
            return;
        }
        subjectIds = new ArrayList<>();
        for (final long id: ids) {
            subjectIds.add(id);
        }
        subjects.setSubjectIds(this, subjectIds, true, true);
        todoCount.setText("To do: " + subjectIds.size());
        successCount.setText("Burned: 0");
        failCount.setText("Failed to burn: 0");

        startButton.setOnClickListener(v -> start());
        startButton.enableInteraction();
        stopButton.setOnClickListener(v -> stop());
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

    @Override
    protected boolean showWithoutApiKey() {
        return false;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("SameReturnValue")
    private @Nullable Void doInBackground(final Consumer<Object[]> publisher) {
        if (WebClient.getInstance().getLastLoginState() != 1) {
            publisher.accept(new Object[] {"Status: Logging in...", false, false, -1L});
            WebClient.getInstance().doLogin();
            publisher.accept(new Object[] {"Status: " + WebClient.getInstance().getLastLoginMessage(), false, false, -1L});
        }

        final AppDatabase db = WkApplication.getDatabase();
        int i = 0;
        while (i < subjectIds.size()) {
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) || stopped) {
                break;
            }
            final long id = subjectIds.get(0);
            final @Nullable Subject subject = db.subjectDao().getById(id);
            if (WebClient.getInstance().getLastLoginState() != 1) {
                publisher.accept(new Object[] {null, false, true, -1L});
                i++;
            }
            else if (subject == null || !subject.isBurnable()) {
                publisher.accept(new Object[] {null, true, false, id});
                subjectIds.remove(i);
            }
            else {
                final CharSequence title = subject.getInfoTitle("Status: Burning: ", "");
                publisher.accept(new Object[] {title, false, false, -1L});
                if (WebClient.getInstance().burn(subject)) {
                    publisher.accept(new Object[] {null, true, false, id});
                    subjectIds.remove(i);
                }
                else {
                    publisher.accept(new Object[] {null, false, true, -1L});
                    i++;
                }
            }
            if (subject != null) {
                db.subjectSyncDao().patchAssignment(id, subject.getSrsSystem().getCompletedStage().getId(),
                        subject.getUnlockedAt(), subject.getStartedAt(), 0,
                        subject.getPassedAt(), System.currentTimeMillis(), 0);
            }
        }
        publisher.accept(new Object[] {"Status: Finished", false, false, -1L});
        return null;
    }

    private void onPublishProgress(final Object[] values) {
        final @Nullable CharSequence newStatus = (CharSequence) values[0];
        if (newStatus != null) {
            status.setText(newStatus);
        }
        if ((boolean) values[1]) {
            todo--;
            success++;
            todoCount.setText("To do: " + todo);
            successCount.setText("Burned: " + success);
            failCount.setText("Failed to burn: " + fail);
        }
        if ((boolean) values[2]) {
            todo--;
            fail++;
            todoCount.setText("To do: " + todo);
            successCount.setText("Burned: " + success);
            failCount.setText("Failed to burn: " + fail);
        }
        final long id = (long) values[3];
        if (id != -1) {
            subjects.removeSubject(id);
        }
    }

    private void onPostExecute() {
        startButton.enableInteraction();
        stopButton.disableInteraction();
        if (subjectIds.isEmpty()) {
            finish();
        }
    }

    /**
     * Handler for the start button.
     */
    private void start() {
        safe(() -> {
            startButton.disableInteraction();
            stopButton.enableInteraction();
            todo = subjectIds.size();
            success = 0;
            fail = 0;
            stopped = false;
            runAsyncWithProgress(
                    this,
                    this::doInBackground,
                    this::onPublishProgress,
                    result -> onPostExecute());
        });
    }

    /**
     * Handler for the start button.
     */
    private void stop() {
        safe(() -> {
            stopButton.disableInteraction();
            stopped = true;
        });
    }
}
