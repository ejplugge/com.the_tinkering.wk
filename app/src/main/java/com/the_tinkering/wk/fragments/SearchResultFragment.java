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

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;

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
import com.the_tinkering.wk.util.SearchUtil;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Fragment to show a simple search result.
 */
public final class SearchResultFragment extends AbstractFragment implements MenuItem.OnMenuItemClickListener {
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
     * Create a new instance with arguments set.
     *
     * @param searchType the type for this search
     * @param searchParameters the type-specific parameters for this search
     * @param presetName the preset used to get here, or null if no preset
     * @return the fragment
     */
    public static SearchResultFragment newInstance(final int searchType, final String searchParameters, final @Nullable String presetName) {
        final SearchResultFragment fragment = new SearchResultFragment();

        final Bundle args = new Bundle();
        args.putInt("searchType", searchType);
        args.putString("searchParameters", searchParameters);
        if (searchType == 0) {
            args.putString("searchDescription", "Level " + searchParameters);
        }
        else if (searchType == 1) {
            args.putString("searchDescription", "'" + searchParameters + "'");
        }
        else if (searchType == 2) {
            args.putString("searchDescription", "Advanced search");
        }
        if (presetName != null) {
            args.putString("presetName", presetName);
        }

        fragment.setArguments(args);
        return fragment;
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
            tutorialDismiss.setOnClickListener(v -> {
                GlobalSettings.Tutorials.setSearchResultDismissed(true);
                tutorialText.setParentVisibility(false);
            });
        }
    }

    private void onViewCreatedBase(final View view, final @Nullable Bundle savedInstanceState) {
        resultView.setDelegate(view, R.id.resultView);
        numHits.setDelegate(view, R.id.numHits);
        tutorialText.setDelegate(view, R.id.tutorialText);
        tutorialDismiss.setDelegate(view, R.id.tutorialDismiss);

        if (savedInstanceState != null) {
            final @Nullable Collection<String> tags = savedInstanceState.getStringArrayList("collapsedTags");
            if (tags == null) {
                adapter.setCollapsedTags(Collections.emptyList());
            }
            else {
                adapter.setCollapsedTags(tags);
            }
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

        runAsync(this, publisher -> {
            if (searchType == 0) {
                final int level = Integer.parseInt(searchParameters, 10);
                adapter.setSortOrder(SearchSortOrder.TYPE);
                final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
                parameters.minLevel = level;
                parameters.maxLevel = level;
                adapter.setParameters(parameters);
            }
            if (searchType == 1) {
                adapter.setSortOrder(SearchSortOrder.TYPE);
            }
            if (searchType == 2) {
                final AdvancedSearchParameters parameters = Converters.getObjectMapper().readValue(searchParameters, AdvancedSearchParameters.class);
                adapter.setSortOrder(parameters.sortOrder);
                adapter.setParameters(parameters);
            }
            return SearchUtil.searchSubjects(searchType, searchParameters);
        }, null, result -> {
            if (result != null) {
                adapter.setResult(result);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isEmpty(GlobalSettings.Api.getWebPassword())) {
                    canResurrect = result.stream().anyMatch(Subject::isResurrectable);
                    canBurn = result.stream().anyMatch(Subject::isBurnable);
                }
                else {
                    canResurrect = false;
                    canBurn = false;
                }
            }
            updateViews();
        });
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        safe(() -> onViewCreatedBase(view, savedInstanceState));
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

        runAsync(this, publisher -> {
            WkApplication.getDatabase().searchPresetDao().setPreset(preset.name, preset.type, preset.data);
            return null;
        }, null, result -> Toast.makeText(requireContext(), "Preset '" + preset.name + "' saved", Toast.LENGTH_SHORT).show());
    }

    private boolean onMenuItemClickBase(final MenuItem item) {
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
                        .setNegativeButton("Cancel", (dialog, which) -> safe(this::hideSoftInput))
                        .setPositiveButton("Save", (dialog, which) -> safe(() -> {
                            final String name = editText.getText();
                            if (!isEmpty(name)) {
                                savePreset(editText.getText());
                            }
                            hideSoftInput();
                        })).create();
                editText.setOnEditorActionListener((v, actionId, event) -> safe(false, () -> {
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
                }));
                theDialog.show();
                editText.requestFocus();
                if (editText.getDelegate() != null) {
                    showSoftInput(editText.getDelegate());
                }
                return true;
            }
            case R.id.action_search_result_self_study: {
                final List<Subject> subjects = adapter.getSubjects();
                runAsync(this, publisher -> {
                    if (!subjects.isEmpty() && Session.getInstance().isInactive()) {
                        Session.getInstance().startNewSelfStudySession(subjects);
                    }
                    return null;
                }, null, result -> goToActivity(SessionActivity.class));
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
        return false;
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return safe(false, () -> onMenuItemClickBase(item));
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        safe(() -> {
            super.onSaveInstanceState(outState);
            outState.putStringArrayList("collapsedTags", new ArrayList<>(adapter.getCollapsedTags()));
        });
    }

    private void updateMenu() {
        final @Nullable AbstractActivity activity = getAbstractActivity();
        if (activity instanceof BrowseActivity) {
            ((BrowseActivity) activity).showSearchResultMenu(this, searchType != 1, adapter.getNumSubjects() > 0, canResurrect, canBurn);
        }
    }
}
