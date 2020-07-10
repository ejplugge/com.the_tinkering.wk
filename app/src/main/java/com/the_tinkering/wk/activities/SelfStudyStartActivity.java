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
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.SearchPreset;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.LiveSearchPresets;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.SearchUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Activity for starting a self-study session.
 *
 * <p>
 *     This activity show the self-study configurations, allows it to modified,
 *     and launches te session when the user taps Start.
 * </p>
 */
public final class SelfStudyStartActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(SelfStudyStartActivity.class);
    private static final Session session = Session.getInstance();

    private final ViewProxy tutorialText = new ViewProxy();
    private final ViewProxy tutorialDismiss = new ViewProxy();
    private final ViewProxy presetSpinner = new ViewProxy();
    private final ViewProxy presetHeader = new ViewProxy();
    private final ViewProxy presetDivider = new ViewProxy();
    private final ViewProxy presetButton = new ViewProxy();
    private final ViewProxy presetDelete = new ViewProxy();
    private final ViewProxy searchForm = new ViewProxy();
    private final ViewProxy searchButton1 = new ViewProxy();
    private final ViewProxy searchButton2 = new ViewProxy();

    /**
     * The constructor.
     */
    public SelfStudyStartActivity() {
        super(R.layout.activity_self_study_start, R.menu.generic_options_menu);
    }

    private void updatePresetAdapter() {
        final List<String> names = LiveSearchPresets.getInstance().getNames();
        final @Nullable Object selection = presetSpinner.getSelection();
        int position = -1;
        if (selection instanceof String) {
            position = LiveSearchPresets.getInstance().getNames().indexOf(selection);
        }
        final SpinnerAdapter adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, names);
        presetSpinner.setAdapter(adapter);
        if (position >= 0) {
            presetSpinner.setSelection(position);
        }
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        if (!session.isInactive()) {
            finish();
            return;
        }

        tutorialText.setDelegate(this, R.id.tutorialText);
        tutorialDismiss.setDelegate(this, R.id.tutorialDismiss);
        presetSpinner.setDelegate(this, R.id.presetSpinner);
        presetHeader.setDelegate(this, R.id.presetHeader);
        presetDivider.setDelegate(this, R.id.presetDivider);
        presetButton.setDelegate(this, R.id.presetButton);
        presetDelete.setDelegate(this, R.id.presetDelete);
        searchForm.setDelegate(this, R.id.searchForm);
        searchButton1.setDelegate(this, R.id.searchButton1);
        searchButton2.setDelegate(this, R.id.searchButton2);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    if (!interactionEnabled) {
                        return;
                    }
                    disableInteraction();

                    final @Nullable AdvancedSearchParameters searchParameters = searchForm.extractParameters();
                    if (searchParameters != null) {
                        new StartSessionTask(SelfStudyStartActivity.this,
                                2, Converters.getObjectMapper().writeValueAsString(searchParameters), true).execute();
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        };

        searchButton1.setOnClickListener(listener);
        searchButton2.setOnClickListener(listener);

        final @Nullable SearchPreset selfStudyPreset = LiveSearchPresets.getInstance().getByName("\u0000SELF_STUDY_DEFAULT");
        if (selfStudyPreset != null) {
            try {
                final AdvancedSearchParameters parameters = Converters.getObjectMapper().readValue(selfStudyPreset.data, AdvancedSearchParameters.class);
                searchForm.injectParameters(parameters);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        searchForm.setSearchButtonLabel("Start");
        searchForm.setSortOrderVisibility(false);

        updatePresetAdapter();

        if (savedInstanceState != null) {
            final @Nullable String selection = savedInstanceState.getString("presetSelection");
            if (selection != null) {
                final int position = LiveSearchPresets.getInstance().getNames().indexOf(selection);
                if (position >= 0) {
                    presetSpinner.setSelection(position);
                }
            }
        }

        LiveSearchPresets.getInstance().observe(this, new Observer<List<SearchPreset>>() {
            @Override
            public void onChanged(final @Nullable List<SearchPreset> t) {
                try {
                    if (LiveSearchPresets.getInstance().getNames().isEmpty()) {
                        presetHeader.setVisibility(false);
                        presetDivider.setVisibility(false);
                        presetSpinner.setParentVisibility(false);
                    }
                    else {
                        updatePresetAdapter();
                        presetHeader.setVisibility(true);
                        presetDivider.setVisibility(true);
                        presetSpinner.setParentVisibility(true);
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });

        presetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    if (!interactionEnabled) {
                        return;
                    }
                    disableInteraction();

                    final @Nullable Object selection = presetSpinner.getSelection();
                    if (selection instanceof String) {
                        final @Nullable SearchPreset preset = LiveSearchPresets.getInstance().getByName((String) selection);
                        if (preset != null) {
                            new StartSessionTask(SelfStudyStartActivity.this, preset.type, preset.data, false).execute();
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });

        presetDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    final @Nullable Object selection = presetSpinner.getSelection();
                    if (selection instanceof String) {
                        final String name = (String) selection;
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Delete preset?")
                                .setMessage(String.format(Locale.ROOT, "Are you sure you want to delete the preset named '%s'?", name))
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
                                            new DeletePresetTask(name).execute();
                                            Toast.makeText(v.getContext(), "Preset deleted", Toast.LENGTH_SHORT).show();
                                        } catch (final Exception e) {
                                            LOGGER.uerr(e);
                                        }
                                    }
                                }).create().show();
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        });
    }

    @Override
    protected void onResumeLocal() {
        if (!session.isInactive()) {
            finish();
        }

        if (GlobalSettings.Tutorials.getStartSelfStudyDismissed()) {
            tutorialText.setParentVisibility(false);
        }
        else {
            tutorialText.setText("From this screen you can start a self-study quiz. The subjects for this quiz can be selected"
                    + " by filling out the search form below. If you have any search presets defined, you can also start a self-study"
                    + " based on those directly. Go to 'Browse/search...' from the menu to define presets.\n\n"
                    + "Results from a self-study quiz are not reported to WaniKani. They don't affect your progress"
                    + " and are only for your own self-study purposes.");
            tutorialText.setParentVisibility(true);
            tutorialDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GlobalSettings.Tutorials.setStartSelfStudyDismissed(true);
                    tutorialText.setParentVisibility(false);
                }
            });
        }
    }

    @Override
    protected void onPauseLocal() {
        //
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final @Nullable Object selection = presetSpinner.getSelection();
        if (selection instanceof String) {
            outState.putString("presetSelection", (String) selection);
        }
    }

    @Override
    protected void enableInteractionLocal() {
        searchButton1.enableInteraction();
        searchButton2.enableInteraction();
        presetButton.enableInteraction();
    }

    @Override
    protected void disableInteractionLocal() {
        searchButton1.disableInteraction();
        searchButton2.disableInteraction();
        presetButton.disableInteraction();
    }

    private static final class StartSessionTask extends AsyncTask<Void, Void, List<Subject>> {
        private final WeakLcoRef<SelfStudyStartActivity> activityRef;
        private final int searchType;
        private final String searchParameters;
        private final boolean saveSelfStudyPreset;

        private StartSessionTask(final SelfStudyStartActivity activity, final int searchType, final String searchParameters,
                                 final boolean saveSelfStudyPreset) {
            activityRef = new WeakLcoRef<>(activity);
            this.searchType = searchType;
            this.searchParameters = searchParameters;
            this.saveSelfStudyPreset = saveSelfStudyPreset;
        }

        @Override
        protected List<Subject> doInBackground(final Void... params) {
            try {
                if (saveSelfStudyPreset && searchType == 2) {
                    WkApplication.getDatabase().searchPresetDao().setPreset("\u0000SELF_STUDY_DEFAULT", searchType, searchParameters);
                }
                final List<Subject> subjects = SearchUtil.searchSubjects(searchType, searchParameters);
                if (!subjects.isEmpty() && session.isInactive()) {
                    Session.getInstance().startNewSelfStudySession(subjects);
                }
                return subjects;
            } catch (final Exception e) {
                LOGGER.uerr(e);
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(final List<Subject> result) {
            try {
                if (result.isEmpty() || session.isInactive()) {
                    Toast.makeText(activityRef.get(), "No subjects found for self-study!", Toast.LENGTH_SHORT).show();
                    activityRef.get().goToMainActivity();
                }
                activityRef.get().goToActivity(SessionActivity.class);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }

    private static final class DeletePresetTask extends AsyncTask<Void, Void, Void> {
        private final String name;

        private DeletePresetTask(final String name) {
            this.name = name;
        }

        @Override
        protected @Nullable Void doInBackground(final Void... params) {
            try {
                WkApplication.getDatabase().searchPresetDao().deletePreset(name);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return null;
        }
    }
}
