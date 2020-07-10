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

package com.the_tinkering.wk.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.activities.AbstractActivity;
import com.the_tinkering.wk.activities.BrowseActivity;
import com.the_tinkering.wk.activities.SessionActivity;
import com.the_tinkering.wk.adapter.search.SearchResultAdapter;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.SearchPreset;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SearchSortOrder;
import com.the_tinkering.wk.livedata.LiveSearchPresets;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.SearchUtil;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;

/**
 * Fragment to show a simple search result.
 */
public final class SearchResultFragment extends AbstractFragment implements MenuItem.OnMenuItemClickListener {
    private static final Logger LOGGER = Logger.get(SearchResultFragment.class);

    private int searchType = 0;
    private String searchParameters = "1";
    private String searchDescription = "";
    private @Nullable String presetName = null;
    private boolean canResurrect = false;
    private boolean canBurn = false;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    private final SearchResultAdapter adapter = new SearchResultAdapter(this);

    private final ViewProxy numHits = new ViewProxy();
    private final ViewProxy resultView = new ViewProxy();
    private final ViewProxy tutorialText = new ViewProxy();
    private final ViewProxy tutorialDismiss = new ViewProxy();

    /**
     * The constructor.
     */
    public SearchResultFragment() {
        super(R.layout.fragment_search_result);
    }

    /**
     * The name of the preset currently being shown.
     *
     * @return the name or null if this is a new search
     */
    public @Nullable String getPresetName() {
        return presetName;
    }

    @Override
    protected void onCreateLocal() {
        final @Nullable Bundle args = getArguments();
        if (args != null) {
            searchType = args.getInt("searchType", 0);
            searchParameters = args.getString("searchParameters", "1");
            searchDescription = args.getString("searchDescription", "");
            presetName = args.getString("presetName", null);
        }
    }

    @Override
    protected void onResumeLocal() {
        updateMenu();

        if (GlobalSettings.Tutorials.getSearchResultDismissed()) {
            tutorialText.setParentVisibility(false);
        }
        else {
            tutorialText.setText("This screen shows the results from the search you chose before."
                    + " You can click on the various headers in the list to collapse/expand groups"
                    + " of subjects.\n"
                    + "Other actions such as refining your search, saving a preset or "
                    + "starting a self-study session can be accessed from the menu.");
            tutorialText.setParentVisibility(true);
            tutorialDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GlobalSettings.Tutorials.setSearchResultDismissed(true);
                    tutorialText.setParentVisibility(false);
                }
            });
        }
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        try {
            resultView.setDelegate(view, R.id.resultView);
            numHits.setDelegate(view, R.id.numHits);
            tutorialText.setDelegate(view, R.id.tutorialText);
            tutorialDismiss.setDelegate(view, R.id.tutorialDismiss);

            if (savedInstanceState != null) {
                final @Nullable Collection<String> tags = savedInstanceState.getStringArrayList("collapsedTags");
                if (tags == null) {
                    adapter.setCollapsedTags(Collections.<String>emptyList());
                }
                else {
                    adapter.setCollapsedTags(tags);
                }
//                adapter.setShowingForm(savedInstanceState.getBoolean("showingForm", false));
            }

            final DisplayMetrics metrics = view.getContext().getResources().getDisplayMetrics();
            int spans = (int) ((metrics.widthPixels / metrics.density + 10) / 90);
            if (spans < 1) {
                spans = 1;
            }
            final int numSpans = spans;
            final GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(), spans);
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(final int position) {
                    return adapter.getItemSpanSize(position, numSpans);
                }
            };
            spanSizeLookup.setSpanGroupIndexCacheEnabled(true);
            spanSizeLookup.setSpanIndexCacheEnabled(true);
            layoutManager.setSpanSizeLookup(spanSizeLookup);
            resultView.setLayoutManager(layoutManager);
            resultView.setAdapter(adapter);
            resultView.setHasFixedSize(true);

            new SearchTask(this, searchType, searchParameters).execute();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public String getToolbarTitle() {
        if (presetName == null) {
            return "Search results";
        }
        return presetName;
    }

    @Override
    public int getToolbarBackgroundColor() {
        return ThemeUtil.getColor(R.attr.toolbarColorBackground);
    }

    @Override
    public void enableInteraction() {
        interactionEnabled = true;
    }

    @Override
    public void disableInteraction() {
        interactionEnabled = false;
    }

    @Override
    public @Nullable Subject getCurrentSubject() {
        return null;
    }

    @Override
    public void showOrHideSoftInput() {
        hideSoftInput();
    }

    @Override
    public void updateViews() {
        updateMenu();
        numHits.setTextFormat("%s: %d subject(s) found", searchDescription, adapter.getNumSubjects());
    }

    private void savePreset(final String name) {
        final SearchPreset preset = new SearchPreset();
        preset.name = name;
        preset.type = searchType;
        preset.data = searchParameters;
        new SavePresetTask(this, preset).execute();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_search_result_refine: {
                    if (searchType != 1) {
                        adapter.setShowingForm(!adapter.isShowingForm(), resultView.getLayoutManager());
                    }
                    return true;
                }
                case R.id.action_search_result_save_preset: {
                    final LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
                    final View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                    final ViewProxy textView = new ViewProxy(promptView, R.id.textView);
                    final ViewProxy editText = new ViewProxy(promptView, R.id.editText);
                    final ArrayAdapter<String> presetAdapter =
                            new ArrayAdapter<>(requireContext(), R.layout.spinner_item, LiveSearchPresets.getInstance().getNames());
                    editText.setArrayAdapter(presetAdapter);
                    textView.setText("Choose a name for the search preset");
                    editText.setHint("Name");
                    if (!isEmpty(presetName)) {
                        editText.setText(presetName);
                    }
                    final AlertDialog theDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Preset name")
                            .setView(promptView)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    try {
                                        hideSoftInput();
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                }
                            })
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    try {
                                        final String name = editText.getText();
                                        if (!isEmpty(name)) {
                                            savePreset(editText.getText());
                                        }
                                        hideSoftInput();
                                    } catch (final Exception e) {
                                        LOGGER.uerr(e);
                                    }
                                }
                            }).create();
                    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                            try {
                                boolean ok = false;
                                if (event == null && actionId != 0) {
                                    ok = true;
                                }
                                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                                    ok = true;
                                }
                                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                                    return true;
                                }
                                if (ok) {
                                    final String name = editText.getText();
                                    if (!isEmpty(name)) {
                                        savePreset(editText.getText());
                                    }
                                    hideSoftInput();
                                    theDialog.dismiss();
                                    return true;
                                }
                                return false;
                            } catch (final Exception e) {
                                LOGGER.uerr(e);
                                return false;
                            }
                        }
                    });
                    theDialog.show();
                    editText.requestFocus();
                    if (editText.getDelegate() != null) {
                        showSoftInput(editText.getDelegate());
                    }
                    return true;
                }
                case R.id.action_search_result_self_study: {
                    new StartSelfStudyTask(this, adapter.getSubjects()).execute();
                    return true;
                }
                case R.id.action_search_result_resurrect: {
                    goToResurrectActivity(adapter.getResurrectableSubjectIds());
                    return true;
                }
                case R.id.action_search_result_burn: {
                    goToBurnActivity(adapter.getBurnableSubjectIds());
                    return true;
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putStringArrayList("collapsedTags", new ArrayList<>(adapter.getCollapsedTags()));
//            outState.putBoolean("showingForm", adapter.isShowingForm());
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void updateMenu() {
        final @Nullable AbstractActivity activity = getAbstractActivity();
        if (activity instanceof BrowseActivity) {
            ((BrowseActivity) activity).showSearchResultMenu(this, searchType != 1, adapter.getNumSubjects() > 0, canResurrect, canBurn);
        }
    }

    private static final class SearchTask extends AsyncTask<Void, Void, List<Subject>> {
        private final WeakLcoRef<SearchResultFragment> fragmentRef;
        private final int searchType;
        private final String searchParameters;

        private SearchTask(final SearchResultFragment fragment, final int searchType, final String searchParameters) {
            fragmentRef = new WeakLcoRef<>(fragment);
            this.searchType = searchType;
            this.searchParameters = searchParameters;
        }

        @Override
        protected List<Subject> doInBackground(final Void... params) {
            try {
                if (searchType == 0) {
                    final int level = Integer.parseInt(searchParameters, 10);
                    fragmentRef.get().adapter.setSortOrder(SearchSortOrder.TYPE);
                    final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
                    parameters.minLevel = level;
                    parameters.maxLevel = level;
                    fragmentRef.get().adapter.setParameters(parameters);
                }
                if (searchType == 1) {
                    fragmentRef.get().adapter.setSortOrder(SearchSortOrder.TYPE);
                }
                if (searchType == 2) {
                    final AdvancedSearchParameters parameters = Converters.getObjectMapper().readValue(searchParameters, AdvancedSearchParameters.class);
                    fragmentRef.get().adapter.setSortOrder(parameters.sortOrder);
                    fragmentRef.get().adapter.setParameters(parameters);
                }
                return SearchUtil.searchSubjects(searchType, searchParameters);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return Collections.emptyList();
        }

        @Override
        protected void onPostExecute(final @Nullable List<Subject> result) {
            try {
                if (result != null) {
                    fragmentRef.get().adapter.setResult(result);
                    fragmentRef.get().canResurrect = false;
                    fragmentRef.get().canBurn = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isEmpty(GlobalSettings.Api.getWebPassword())) {
                        for (final Subject subject: result) {
                            if (subject.isResurrectable()) {
                                fragmentRef.get().canResurrect = true;
                            }
                            if (subject.isBurnable()) {
                                fragmentRef.get().canBurn = true;
                            }
                        }
                    }
                }
                fragmentRef.get().updateViews();
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }

    private static final class StartSelfStudyTask extends AsyncTask<Void, Void, Void> {
        private final WeakLcoRef<Actment> actmentRef;
        private final List<Subject> subjects;

        private StartSelfStudyTask(final Actment actment, final List<Subject> subjects) {
            actmentRef = new WeakLcoRef<>(actment);
            this.subjects = subjects;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                if (!subjects.isEmpty() && Session.getInstance().isInactive()) {
                    Session.getInstance().startNewSelfStudySession(subjects);
                }
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            try {
                actmentRef.get().goToActivity(SessionActivity.class);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }

    private static final class SavePresetTask extends AsyncTask<Void, Void, Void> {
        private final WeakLcoRef<Actment> actmentRef;
        private final SearchPreset preset;

        private SavePresetTask(final Actment actment, final SearchPreset preset) {
            actmentRef = new WeakLcoRef<>(actment);
            this.preset = preset;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            try {
                WkApplication.getDatabase().searchPresetDao().setPreset(preset.name, preset.type, preset.data);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            try {
                Toast.makeText(actmentRef.get().requireContext(), "Preset '" + preset.name + "' saved", Toast.LENGTH_SHORT).show();
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
