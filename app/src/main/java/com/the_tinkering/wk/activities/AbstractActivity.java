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

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.Identification;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.enums.OnlineStatus;
import com.the_tinkering.wk.fragments.AbstractFragment;
import com.the_tinkering.wk.jobs.ActivityResumedJob;
import com.the_tinkering.wk.jobs.AutoSyncNowJob;
import com.the_tinkering.wk.jobs.FlushTasksJob;
import com.the_tinkering.wk.jobs.SettingChangedJob;
import com.the_tinkering.wk.jobs.SyncNowJob;
import com.the_tinkering.wk.jobs.TickJob;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.livedata.LiveSessionProgress;
import com.the_tinkering.wk.livedata.LiveSessionState;
import com.the_tinkering.wk.livedata.LiveTaskCounts;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.services.JobRunnerService;
import com.the_tinkering.wk.services.NetworkStateBroadcastReceiver;
import com.the_tinkering.wk.tasks.ApiTask;
import com.the_tinkering.wk.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.FLUSH_TASKS_WARNING;
import static com.the_tinkering.wk.Constants.MINUTE;
import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.join;
import static com.the_tinkering.wk.util.TextUtil.renderHtml;

/**
 * Abstract superclass for all activities that takes care of a bunch of common functionality.
 */
public abstract class AbstractActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Actment {
    private static final Logger LOGGER = Logger.get(AbstractActivity.class);
    private static long lastResume = 0;
    private static long lastPause = 0;
    private final int layoutId;
    private final int optionsMenuId;
    private final boolean mainActivity;
    private @Nullable NetworkStateBroadcastReceiver networkStateBroadcastReceiver = null;
    private @Nullable Timer tickTimer = null;
    private @Nullable ActiveTheme creationTheme = null;
    private @Nullable Resources.Theme createdTheme = null;

    /**
     * True if the activity is 'active', i.e. the user can interact with the buttons on it.
     */
    protected boolean interactionEnabled = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * The constructor.
     *
     * @param layoutId id for this activity's layout
     * @param optionsMenuId id for this activity's menu
     */
    protected AbstractActivity(final int layoutId, final int optionsMenuId) {
        this.layoutId = layoutId;
        this.optionsMenuId = optionsMenuId;
        mainActivity = this instanceof MainActivity;
    }

    @Override
    public final Context requireContext() {
        return this;
    }

    @Override
    protected final void onCreate(final @Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            creationTheme = ActiveTheme.getCurrentTheme();
            setContentView(layoutId);

            final @Nullable Toolbar toolbar = getToolbar();
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                final @Nullable ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    if (!mainActivity) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setDisplayShowHomeEnabled(true);
                    }

                    LiveTaskCounts.getInstance().observe(this, t -> {
                        try {
                            final OnlineStatus onlineStatus = ApiTask.getOnlineStatus();
                            final boolean hasApi = t != null && t.getApiCount() > 0 && onlineStatus.canCallApi();
                            final boolean hasAudio = t != null && t.getAudioCount() > 0 && onlineStatus.canDownloadAudio();
                            final boolean hasPitchInfo = t != null && t.getPitchInfoCount() > 0 && onlineStatus.canDownloadAudio();

                            final Collection<String> parts = new ArrayList<>();
                            if (hasApi) {
                                parts.add(String.format(Locale.ROOT, "%d background tasks", t.getApiCount()));
                            }
                            if (hasAudio) {
                                parts.add(String.format(Locale.ROOT, "%d audio download tasks", t.getAudioCount()));
                            }
                            if (hasPitchInfo) {
                                parts.add(String.format(Locale.ROOT, "%d pitch info download tasks", t.getPitchInfoCount()));
                            }

                            if (parts.isEmpty()) {
                                actionBar.setSubtitle(null);
                            }
                            else {
                                actionBar.setSubtitle(join(", ", "", "", parts) + "... ⌛");
                            }

                            final @Nullable Menu menu = getMenu();
                            if (menu != null) {
                                final @Nullable MenuItem flushTasksItem = menu.findItem(R.id.action_flush_tasks);
                                if (t != null && flushTasksItem != null) {
                                    flushTasksItem.setVisible(!t.isEmpty());
                                }
                            }
                        } catch (final Exception e) {
                            LOGGER.uerr(e);
                        }
                    });
                }
            }

            LiveSessionState.getInstance().observe(this, t -> {
                try {
                    final @Nullable Menu menu = getMenu();
                    final Session session = Session.getInstance();

                    if (menu != null) {
                        final @Nullable MenuItem abandonSessionItem = menu.findItem(R.id.action_abandon_session);
                        if (abandonSessionItem != null) {
                            abandonSessionItem.setVisible(session.canBeAbandoned());
                        }

                        final @Nullable MenuItem wrapupSessionItem = menu.findItem(R.id.action_wrapup_session);
                        if (wrapupSessionItem != null) {
                            wrapupSessionItem.setVisible(session.canBeWrappedUp());
                        }

                        final @Nullable MenuItem selfStudyItem = menu.findItem(R.id.action_self_study);
                        if (selfStudyItem != null) {
                            selfStudyItem.setVisible(session.isInactive());
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            LiveSessionProgress.getInstance().observe(this, t -> {
                try {
                    final @Nullable Menu menu = getMenu();
                    final Session session = Session.getInstance();

                    if (menu != null) {
                        final @Nullable MenuItem wrapupSessionItem = menu.findItem(R.id.action_wrapup_session);
                        if (wrapupSessionItem != null) {
                            wrapupSessionItem.setVisible(session.canBeWrappedUp());
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            LiveLevelDuration.getInstance().observe(this, t -> {
                try {
                    final @Nullable Menu menu = getMenu();
                    if (menu != null) {
                        final @Nullable MenuItem testItem = menu.findItem(R.id.action_test);
                        if (testItem != null) {
                            final @Nullable String username = t.getUsername();
                            testItem.setVisible(!isEmpty(username) && username.equals(Identification.AUTHOR_USERNAME));
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            onCreateLocal(savedInstanceState);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    protected final void onResume() {
        try {
            super.onResume();

            if (creationTheme != null && creationTheme != ActiveTheme.getCurrentTheme()) {
                recreate();
                return;
            }

            if (GlobalSettings.Api.getSyncOnOpen() && (lastPause == 0 && lastResume == 0
                    || lastResume < lastPause && System.currentTimeMillis() - lastPause > 3 * MINUTE)) {
                JobRunnerService.schedule(AutoSyncNowJob.class, "");
            }
            lastResume = System.currentTimeMillis();

            PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

            if (!(this instanceof NoApiKeyHelpActivity)) {
                final @Nullable String apiKey = GlobalSettings.Api.getApiKey();
                if (isEmpty(apiKey)) {
                    goToActivity(NoApiKeyHelpActivity.class);
                }
            }

            networkStateBroadcastReceiver = new NetworkStateBroadcastReceiver();
            final IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateBroadcastReceiver, filter);

            JobRunnerService.schedule(ActivityResumedJob.class, getClass().getSimpleName());

            tickTimer = new Timer();
            tickTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        JobRunnerService.schedule(TickJob.class, "");
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            }, MINUTE, MINUTE);

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            onResumeLocal();
            enableInteraction();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    protected final void onPause() {
        try {
            super.onPause();

            lastPause = System.currentTimeMillis();

            if (tickTimer != null) {
                tickTimer.cancel();
                tickTimer = null;
            }

            if (networkStateBroadcastReceiver != null) {
                unregisterReceiver(networkStateBroadcastReceiver);
                networkStateBroadcastReceiver = null;
            }

            PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

            onPauseLocal();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        try {
            getMenuInflater().inflate(optionsMenuId, menu);

            final @Nullable MenuItem abandonSessionItem = menu.findItem(R.id.action_abandon_session);
            if (abandonSessionItem != null) {
                final Session session = Session.getInstance();
                abandonSessionItem.setVisible(session.canBeAbandoned());
            }

            final @Nullable MenuItem wrapupSessionItem = menu.findItem(R.id.action_wrapup_session);
            if (wrapupSessionItem != null) {
                final Session session = Session.getInstance();
                wrapupSessionItem.setVisible(session.canBeWrappedUp());
            }

            final @Nullable MenuItem viewLastFinishedItem = menu.findItem(R.id.action_view_last_finished);
            if (viewLastFinishedItem != null) {
                final Session session = Session.getInstance();
                viewLastFinishedItem.setVisible(session.getLastFinishedSubjectId() != -1);
            }

            final @Nullable MenuItem studyMaterialsItem = menu.findItem(R.id.action_study_materials);
            if (studyMaterialsItem != null) {
                studyMaterialsItem.setVisible(getCurrentSubject() != null && getCurrentSubject().getType().canHaveStudyMaterials());
            }

            final @Nullable MenuItem selfStudyItem = menu.findItem(R.id.action_self_study);
            if (selfStudyItem != null) {
                final Session session = Session.getInstance();
                selfStudyItem.setVisible(session.isInactive());
            }

            final @Nullable MenuItem flushTasksItem = menu.findItem(R.id.action_flush_tasks);
            if (flushTasksItem != null) {
                flushTasksItem.setVisible(!LiveTaskCounts.getInstance().get().isEmpty());
            }

            final @Nullable SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final @Nullable MenuItem searchItem = menu.findItem(R.id.action_search);
            if (searchItem != null && searchManager != null) {
                final SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, BrowseActivity.class)));
            }

            final @Nullable MenuItem testItem = menu.findItem(R.id.action_test);
            if (testItem != null) {
                final @Nullable String username = LiveLevelDuration.getInstance().get().getUsername();
                testItem.setVisible(!isEmpty(username) && username.equals(Identification.AUTHOR_USERNAME));
            }

            return true;
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return true;
        }
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_settings: {
                    goToPreferencesActivity(null);
                    return true;
                }
                case R.id.action_search: {
                    startSearch(null, false, null, false);
                    return true;
                }
                case R.id.action_dashboard: {
                    goToMainActivity();
                    return true;
                }
                case R.id.action_download_audio: {
                    goToActivity(DownloadAudioActivity.class);
                    return true;
                }
                case R.id.action_browse: {
                    goToActivity(BrowseActivity.class);
                    return true;
                }
                case R.id.action_view_last_finished: {
                    final long subjectId = Session.getInstance().getLastFinishedSubjectId();
                    if (subjectId != -1) {
                        goToSubjectInfo(subjectId, Collections.emptyList(), FragmentTransitionAnimation.RTL);
                    }
                    return true;
                }
                case R.id.action_abandon_session: {
                    final Session session = Session.getInstance();
                    if (GlobalSettings.UiConfirmations.getUiConfirmAbandonSession()) {
                        final int numActive = session.getNumActiveItems();
                        final int numPending = session.getNumPendingItems();
                        final int numReported = session.getNumReportedItems();
                        final int numStarted = session.getNumStartedItems();
                        final int numNotStarted = numActive - numStarted;
                        String message = "Are you sure you want to abandon this session? If you do:";
                        if (numPending > 0) {
                            message += String.format(Locale.ROOT, "\n- %d finished items will not be reported", numPending);
                        }
                        if (numReported > 0) {
                            message += String.format(Locale.ROOT, "\n- %d already finished items will still be reported", numReported);
                        }
                        if (numStarted > 0) {
                            message += String.format(Locale.ROOT, "\n- %d partially quizzed items will not be reported", numStarted);
                        }
                        if (numNotStarted > 0) {
                            message += String.format(Locale.ROOT, "\n- %d unquizzed items will not be reported", numNotStarted);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Abandon session?")
                                .setMessage(message)
                                .setIcon(R.drawable.ic_baseline_warning_24px)
                                .setNegativeButton("No", (dialog, which) -> {
                                    //
                                })
                                .setNeutralButton("Yes and don't ask again", (dialog, which) -> {
                                    try {
                                        session.finish();
                                        Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show();
                                        GlobalSettings.UiConfirmations.setUiConfirmAbandonSession(false);
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                })
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        session.finish();
                                        Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show();
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                }).create().show();
                    }
                    else {
                        session.finish();
                        Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                case R.id.action_wrapup_session: {
                    final Session session = Session.getInstance();
                    if (GlobalSettings.UiConfirmations.getUiConfirmWrapupSession()) {
                        final int numActive = session.getNumActiveItems();
                        final int numStarted = session.getNumStartedItems();
                        final int numNotStarted = numActive - numStarted;
                        String message = "Are you sure you want to wrap up this session? If you do:";
                        if (numStarted > 0) {
                            message += String.format(Locale.ROOT, "\n- %d partially quizzed items will remain in the session", numStarted);
                        }
                        if (numNotStarted > 0) {
                            message += String.format(Locale.ROOT, "\n- %d unquizzed items will be removed from the session", numNotStarted);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Wrap up session?")
                                .setMessage(message)
                                .setIcon(R.drawable.ic_baseline_warning_24px)
                                .setNegativeButton("No", (dialog, which) -> {
                                    //
                                })
                                .setNeutralButton("Yes and don't ask again", (dialog, which) -> {
                                    try {
                                        session.wrapup();
                                        Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT).show();
                                        GlobalSettings.UiConfirmations.setUiConfirmWrapupSession(false);
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                })
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        session.wrapup();
                                        Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT).show();
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                }).create().show();
                    }
                    else {
                        session.wrapup();
                        Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                case R.id.action_study_materials: {
                    final @Nullable Subject subject = getCurrentSubject();
                    if (subject != null && subject.getType().canHaveStudyMaterials()) {
                        goToStudyMaterialsActivity(subject.getId());
                    }
                    return true;
                }
                case R.id.action_self_study: {
                    goToActivity(SelfStudyStartActivity.class);
                    return true;
                }
                case R.id.action_sync_now: {
                    JobRunnerService.schedule(SyncNowJob.class, "");
                    return true;
                }
                case R.id.action_flush_tasks: {
                    new AlertDialog.Builder(this)
                            .setTitle("Flush background tasks?")
                            .setMessage(renderHtml(FLUSH_TASKS_WARNING))
                            .setIcon(R.drawable.ic_baseline_warning_24px)
                            .setNegativeButton("No", (dialog, which) -> {
                                //
                            })
                            .setPositiveButton("Yes", (dialog, which) -> {
                                try {
                                    JobRunnerService.schedule(FlushTasksJob.class, "");
                                    Toast.makeText(this, "Background tasks flushed!", Toast.LENGTH_SHORT).show();
                                } catch (final Exception e) {
                                    LOGGER.uerr(e);
                                }
                            }).create().show();
                    return true;
                }
                case R.id.action_about: {
                    goToActivity(AboutActivity.class);
                    return true;
                }
                case R.id.action_support: {
                    goToActivity(SupportActivity.class);
                    return true;
                }
                case R.id.action_test: {
                    Toast.makeText(this, "Test!", Toast.LENGTH_SHORT).show();
                    goToActivity(TestActivity.class);
                    return true;
                }
                case android.R.id.home: {
                    onBackPressed();
                    return true;
                }
                default:
                    break;
            }

            return super.onOptionsItemSelected(item);
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return true;
        }
    }

    @Override
    public final void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        try {
            JobRunnerService.schedule(SettingChangedJob.class, key);
            if ("theme".equals(key)) {
                recreate();
                WkApplication.getInstance().resetTheme();
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * When changing content within an activity, collapse the search box.
     */
    protected final void collapseSearchBox() {
        try {
            final @Nullable Menu menu = getMenu();
            if (menu != null) {
                final @Nullable MenuItem searchItem = menu.findItem(R.id.action_search);
                if (searchItem != null) {
                    searchItem.collapseActionView();
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Get the current theme based on user settings.
     *
     * @return the theme
     */
    @Override
    public final @Nullable Resources.Theme getTheme() {
        if (createdTheme == null) {
            try {
                createdTheme = super.getTheme();
                if (createdTheme != null) {
                    createdTheme.applyStyle(ActiveTheme.getCurrentTheme().getStyleId(), true);
                }
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
        return createdTheme;
    }

    @Override
    public final @Nullable Toolbar getToolbar() {
        try {
            return (Toolbar) findViewById(R.id.toolbar);
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return null;
        }
    }

    /**
     * Get the menu for this activity.
     *
     * @return the menu or null if it doesn't exist (yet).
     */
    protected final @Nullable Menu getMenu() {
        try {
            final @Nullable Toolbar toolbar = getToolbar();
            if (toolbar == null) {
                return null;
            }
            return toolbar.getMenu();
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return null;
        }
    }

    @Override
    public final void goToActivity(final Class<? extends AbstractActivity> clas) {
        try {
            final Intent intent = new Intent(this, clas);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Jump to the preferences activity immediately.
     *
     * @param rootKey the key of the screen to jump to, or null for the root screen
     */
    protected final void goToPreferencesActivity(final @Nullable String rootKey) {
        try {
            final Intent intent = new Intent(this, PreferencesActivity.class);
            if (rootKey != null) {
                intent.putExtra("rootKey", rootKey);
            }
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    public final void goToMainActivity() {
        try {
            final Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public final void goToSubjectInfo(final long id, final List<Long> ids, final FragmentTransitionAnimation animation) {
        try {
            final long[] a = new long[ids.size()];
            for (int i=0; i<a.length; i++) {
                a[i] = ids.get(i);
            }
            goToSubjectInfo(id, a, animation);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public final void goToSubjectInfo(final long id, final long[] ids, final FragmentTransitionAnimation animation) {
        try {
            if (this instanceof BrowseActivity) {
                ((BrowseActivity) this).loadSubjectInfoFragment(id, ids, animation);
                return;
            }
            final Intent intent = new Intent(this, BrowseActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("ids", ids);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Go to the resurrect activity with the supplied list of subject IDs to resurrect.
     *
     * @param ids the subject IDs
     */
    public final void goToResurrectActivity(final long[] ids) {
        try {
            final Intent intent = new Intent(this, ResurrectActivity.class);
            intent.putExtra("ids", ids);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Go to the burn activity with the supplied list of subject IDs to burn.
     *
     * @param ids the subject IDs
     */
    public final void goToBurnActivity(final long[] ids) {
        try {
            final Intent intent = new Intent(this, BurnActivity.class);
            intent.putExtra("ids", ids);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void goToStudyMaterialsActivity(final long id) {
        try {
            final Uri uri = new Uri.Builder()
                    .scheme(Identification.APP_URI_SCHEME)
                    .authority("study-materials")
                    .appendPath(Long.toString(id))
                    .build();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Get the subject that this activity is currently dealing with,
     * or null if there is no specific subject.
     *
     * @return the subject
     */
    private @Nullable Subject getCurrentSubject() {
        final @Nullable AbstractFragment fragment = getCurrentFragment();
        if (fragment != null) {
            return fragment.getCurrentSubject();
        }
        return null;
    }

    /**
     * Update the current subject ID for this activity. This provides the
     * context to enable the menu option for study materials.
     */
    public final void updateCurrentSubject() {
        try {
            final @Nullable Menu menu = getMenu();
            if (menu != null) {
                final @Nullable MenuItem studyMaterialsItem = menu.findItem(R.id.action_study_materials);
                if (studyMaterialsItem != null) {
                    studyMaterialsItem.setVisible(getCurrentSubject() != null && getCurrentSubject().getType().canHaveStudyMaterials());
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * The current fragment attached to the default fragment container view.
     *
     * @return the fragment or null if not found
     */
    protected final @Nullable AbstractFragment getCurrentFragment() {
        final FragmentManager manager = getSupportFragmentManager();
        final @Nullable Fragment fragment = manager.findFragmentById(R.id.fragment);
        if (fragment instanceof AbstractFragment) {
            return (AbstractFragment) fragment;
        }
        return null;
    }

    /**
     * Enable interactivity on the current activity. This makes buttons clickable, etc.
     *
     * <p>
     *     The idea is that if an interaction can take time to resolve, further
     *     interaction is disabled until it has been resolved. Allowing parallel actions
     *     sometimes causes problems.
     * </p>
     */
    protected final void enableInteraction() {
        try {
            enableInteractionLocal();
            interactionEnabled = true;
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Disable interactivity on the current activity. This makes buttons non-clickable, etc.
     *
     * <p>
     *     The idea is that if an interaction can take time to resolve, further
     *     interaction is disabled until it has been resolved. Allowing parallel actions
     *     sometimes causes problems.
     * </p>
     */
    protected final void disableInteraction() {
        try {
            interactionEnabled = false;
            disableInteractionLocal();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Translate DIPs to pixels.
     *
     * @param dp the dimension in DIPs
     * @return the corresponding number of pixels
     */
    protected final int dp2px(final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Handle the onCreate event. This is called after the generic handling has been done.
     *
     * @param savedInstanceState the saved instance state from onCreate()
     */
    protected abstract void onCreateLocal(@Nullable Bundle savedInstanceState);

    /**
     * Handle the onResume event. This is called after the generic handling has been done.
     */
    protected abstract void onResumeLocal();

    /**
     * Handle the onPause event. This is called after the generic handling has been done.
     */
    protected abstract void onPauseLocal();

    /**
     * See enableInteraction(), this is the part that must be implemented by each subclass.
     */
    protected abstract void enableInteractionLocal();

    /**
     * See disableInteraction(), this is the part that must be implemented by each subclass.
     */
    protected abstract void disableInteractionLocal();
}
