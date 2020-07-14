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

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.activities.AbstractActivity;
import com.the_tinkering.wk.activities.BrowseActivity;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.SearchPreset;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.livedata.LiveSearchPresets;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Fragment for the initial browse/search overview.
 */
public final class BrowseOverviewFragment extends AbstractFragment {
    private static final Logger LOGGER = Logger.get(BrowseOverviewFragment.class);

    private final ViewProxy levelTable = new ViewProxy();
    private final ViewProxy queryField = new ViewProxy();
    private final ViewProxy queryButton = new ViewProxy();
    private final ViewProxy searchButton1 = new ViewProxy();
    private final ViewProxy searchButton2 = new ViewProxy();
    private final ViewProxy searchForm = new ViewProxy();
    private final ViewProxy tutorialText = new ViewProxy();
    private final ViewProxy tutorialDismiss = new ViewProxy();
    private final ViewProxy presetSpinner = new ViewProxy();
    private final ViewProxy presetHeader = new ViewProxy();
    private final ViewProxy presetDivider = new ViewProxy();
    private final ViewProxy presetButton = new ViewProxy();
    private final ViewProxy presetDelete = new ViewProxy();

    /**
     * The constructor.
     */
    public BrowseOverviewFragment() {
        super(R.layout.fragment_browse_overview);
    }

    @Override
    protected void onCreateLocal() {
        //
    }

    @Override
    protected void onResumeLocal() {
        final @Nullable AbstractActivity activity = getAbstractActivity();
        if (activity instanceof BrowseActivity) {
            ((BrowseActivity) activity).hideSearchResultMenu();
        }

        if (GlobalSettings.Tutorials.getBrowseOverviewDismissed()) {
            tutorialText.setParentVisibility(false);
        }
        else {
            tutorialText.setText("From this screen you can search the subject database by level, by keyword, or by"
                    + " advanced filter criteria. From the search result screen you can then save search presets, start a self-study quiz,"
                    + " and more. Once you have defined search presets, they will be shown here as well.");
            tutorialText.setParentVisibility(true);
            tutorialDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GlobalSettings.Tutorials.setBrowseOverviewDismissed(true);
                    tutorialText.setParentVisibility(false);
                }
            });
        }
    }

    private void updatePresetAdapter() {
        final List<String> names = LiveSearchPresets.getInstance().getNames();
        final @Nullable Object selection = presetSpinner.getSelection();
        int position = -1;
        if (selection instanceof String) {
            position = LiveSearchPresets.getInstance().getNames().indexOf(selection);
        }
        final SpinnerAdapter adapter =
                new ArrayAdapter<>(requireContext(), R.layout.spinner_item, names);
        presetSpinner.setAdapter(adapter);
        if (position >= 0) {
            presetSpinner.setSelection(position);
        }
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        try {
            levelTable.setDelegate(view, R.id.levelTable);
            queryField.setDelegate(view, R.id.query);
            queryButton.setDelegate(view, R.id.queryButton);
            searchButton1.setDelegate(view, R.id.searchButton1);
            searchButton2.setDelegate(view, R.id.searchButton2);
            searchForm.setDelegate(view, R.id.searchForm);
            tutorialText.setDelegate(view, R.id.tutorialText);
            tutorialDismiss.setDelegate(view, R.id.tutorialDismiss);
            presetSpinner.setDelegate(view, R.id.presetSpinner);
            presetHeader.setDelegate(view, R.id.presetHeader);
            presetDivider.setDelegate(view, R.id.presetDivider);
            presetButton.setDelegate(view, R.id.presetButton);
            presetDelete.setDelegate(view, R.id.presetDelete);

            queryField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(final TextView v, final int actionId, final @Nullable KeyEvent event) {
                    try {
                        if (event == null && actionId != 0) {
                            submitQuery(queryField.getText());
                            return true;
                        }
                        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                            submitQuery(queryField.getText());
                            return true;
                        }
                        //noinspection RedundantIfStatement
                        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                            return true;
                        }
                        return false;
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                        return false;
                    }
                }
            });

            queryField.setImeOptions(EditorInfo.IME_ACTION_DONE);

            queryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        submitQuery(queryField.getText());
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            final View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        final @Nullable AdvancedSearchParameters searchParameters = searchForm.extractParameters();
                        if (searchParameters != null) {
                            final @Nullable AbstractActivity activity = getAbstractActivity();
                            if (activity instanceof BrowseActivity) {
                                ((BrowseActivity) activity).loadSearchResultFragment(null, 2,
                                        Converters.getObjectMapper().writeValueAsString(searchParameters));
                            }
                        }
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            };

            searchButton1.setOnClickListener(listener);
            searchButton2.setOnClickListener(listener);

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

            LiveSearchPresets.getInstance().observe(getViewLifecycleOwner(), new Observer<List<SearchPreset>>() {
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
                        final @Nullable Object selection = presetSpinner.getSelection();
                        if (selection instanceof String) {
                            final @Nullable SearchPreset preset = LiveSearchPresets.getInstance().getByName((String) selection);
                            final @Nullable AbstractActivity activity = getAbstractActivity();
                            if (activity instanceof BrowseActivity && preset != null) {
                                ((BrowseActivity) activity).loadSearchResultFragment(preset.name, preset.type, preset.data);
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

            new Task(this).execute();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
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
    public String getToolbarTitle() {
        return "Browse / search";
    }

    @Override
    public int getToolbarBackgroundColor() {
        return ThemeUtil.getColor(R.attr.toolbarColorBackground);
    }

    @Override
    public void enableInteraction() {
        //
    }

    @Override
    public void disableInteraction() {
        //
    }

    @Override
    public @Nullable Subject getCurrentSubject() {
        return null;
    }

    @Override
    public void showOrHideSoftInput() {
        //
    }

    @Override
    public void updateViews() {
        updateToolbar();
    }

    private void submitQuery(final String query) {
        if (query.isEmpty()) {
            return;
        }
        final @Nullable AbstractActivity activity = getAbstractActivity();
        if (activity instanceof BrowseActivity) {
            hideSoftInput();
            ((BrowseActivity) activity).loadSearchResultFragment(null, 1, query);
        }
    }

    private void render(final int maxLevel) {
        final @Nullable Context context = getContext();
        if (context == null) {
            return;
        }

        levelTable.removeAllViews();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int numColumns = Math.round(dpWidth / Constants.BROWSE_OVERVIEW_COLUMN_WIDTH);
        if (numColumns < 1) {
            numColumns = 1;
        }
        if (numColumns > 10) {
            numColumns = 10;
        }
        if (numColumns < 10 && numColumns > 5) {
            numColumns = 5;
        }

        @Nullable TableRow currentRow = null;

        for (int i=0; i<maxLevel; i++) {
            final int column = i % numColumns;
            if (column == 0) {
                final TableLayout.LayoutParams rowLayoutParams;
                currentRow = new TableRow(context);
                currentRow.setId(ViewCompat.generateViewId());
                rowLayoutParams = new TableLayout.LayoutParams(0, 0);
                rowLayoutParams.setMargins(0, dp2px(6), 0, 0);
                rowLayoutParams.width = WRAP_CONTENT;
                rowLayoutParams.height = WRAP_CONTENT;
                levelTable.addView(currentRow, rowLayoutParams);
            }

            final TableRow.LayoutParams cellLayoutParams;

            final int level = i + 1;
            final View.OnClickListener onClick = new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        final @Nullable AbstractActivity activity = getAbstractActivity();
                        if (activity instanceof BrowseActivity) {
                            ((BrowseActivity) activity).loadSearchResultFragment(null, 0, Integer.toString(level));
                        }
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            };

            final TextView button = new TextView(context, null, R.attr.WK_TextView_BrowseLevelButton);
            button.setText(Integer.toString(level));
            button.setPadding(dp2px(6), dp2px(6), dp2px(6), dp2px(6));
            button.setClickable(true);
            button.setFocusable(false);
            button.setOnClickListener(onClick);
            button.setGravity(Gravity.CENTER_HORIZONTAL);
            button.setShadowLayer(3, 1, 1, 0xFF000000);
            if (level == LiveLevelDuration.getInstance().get().getLevel()) {
                button.setBackgroundColor(ThemeUtil.getColor(R.attr.buttonHighlightColor));
            }
            cellLayoutParams = new TableRow.LayoutParams(0, 0);
            cellLayoutParams.setMargins(dp2px(2), dp2px(2), 0, 0);
            cellLayoutParams.width = MATCH_PARENT;
            cellLayoutParams.height = WRAP_CONTENT;
            currentRow.addView(button, cellLayoutParams);
        }
    }

    private static final class Task extends AsyncTask<Void, Void, Integer> {
        private final WeakLcoRef<BrowseOverviewFragment> fragmentRef;

        private Task(final BrowseOverviewFragment fragment) {
            fragmentRef = new WeakLcoRef<>(fragment);
        }

        @Override
        protected Integer doInBackground(final Void... params) {
            try {
                return WkApplication.getDatabase().subjectAggregatesDao().getMaxLevel();
            } catch (final Exception e) {
                LOGGER.uerr(e);
                return 0;
            }
        }

        @Override
        protected void onPostExecute(final @Nullable Integer result) {
            try {
                if (result != null) {
                    fragmentRef.get().render(result);
                }
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
