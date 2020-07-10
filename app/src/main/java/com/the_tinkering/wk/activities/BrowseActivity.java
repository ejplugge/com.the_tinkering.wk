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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.the_tinkering.wk.Identification;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.fragments.BrowseOverviewFragment;
import com.the_tinkering.wk.fragments.SearchResultFragment;
import com.the_tinkering.wk.fragments.SubjectInfoFragment;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

/**
 * An activity for browsing/searching the subject database.
 */
public final class BrowseActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(BrowseActivity.class);

    /**
     * The constructor.
     */
    public BrowseActivity() {
        super(R.layout.activity_browse, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
                final @Nullable String query = getIntent().getStringExtra(SearchManager.QUERY);
                if (query != null) {
                    loadSearchResultFragment(null, 1, query);
                    return;
                }
            }

            final long id = getIntent().getLongExtra("id", -1);
            if (id > 0) {
                loadSubjectInfoFragment(id, getIntent().getLongArrayExtra("ids"), FragmentTransitionAnimation.NONE);
                return;
            }

            final @Nullable Uri uri = getIntent().getData();
            if (uri != null && Identification.APP_URI_SCHEME.equals(uri.getScheme()) && "subject-info".equals(uri.getAuthority())) {
                final @Nullable String s = uri.getPath();
                if (s != null) {
                    final int p = s.lastIndexOf('/');
                    if (p >= 0) {
                        try {
                            final long searchId = Long.parseLong(s.substring(p+1));
                            loadSubjectInfoFragment(searchId, new long[0], FragmentTransitionAnimation.NONE);
                            return;
                        } catch (final Exception e) {
                            LOGGER.uerr(e);
                        }
                    }
                }
            }

            loadOverviewFragment();
        }
    }

    @Override
    protected void onResumeLocal() {
        //
    }

    @Override
    protected void onPauseLocal() {
        //
    }

    @Override
    protected void enableInteractionLocal() {
        //
    }

    @Override
    protected void disableInteractionLocal() {
        //
    }

    private void loadOverviewFragment() {
        final Fragment fragment = new BrowseOverviewFragment();

        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        if (getCurrentFragment() != null) {
            transaction.addToBackStack(null);
            FragmentTransitionAnimation.RTL.apply(transaction);
        }
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
        hideSearchResultMenu();
    }

    /**
     * Show a search result fragment.
     *
     * @param presetName the name of the preset used, or null if not available
     * @param searchType the type of search 0=browse level, 1=keyword search, 2=advanced search
     * @param searchParameters the type-specific parameters for this search
     */
    public void loadSearchResultFragment(final @Nullable String presetName, final int searchType, final String searchParameters) {
        final Fragment fragment = new SearchResultFragment();
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

        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        if (getCurrentFragment() != null) {
            transaction.addToBackStack(null);
            FragmentTransitionAnimation.RTL.apply(transaction);
        }
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    /**
     * Show a subject info fragment.
     *
     * @param id the subject's ID
     * @param ids the list of context subject IDs
     * @param animation the transition animation to use for the transition
     */
    public void loadSubjectInfoFragment(final long id, final @Nullable long[] ids, final FragmentTransitionAnimation animation) {
        final Fragment fragment = new SubjectInfoFragment();
        final Bundle args = new Bundle();
        args.putLong("id", id);
        if (ids != null) {
            args.putLongArray("ids", ids);
        }
        fragment.setArguments(args);

        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        if (getCurrentFragment() != null) {
            transaction.addToBackStack(null);
            animation.apply(transaction);
        }
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    /**
     * Hide the search results submenu.
     */
    public void hideSearchResultMenu() {
        final @Nullable Menu menu = getMenu();
        if (menu != null) {
            final @Nullable MenuItem subMenu = menu.findItem(R.id.action_search_result);
            if (subMenu != null) {
                subMenu.setVisible(false);
            }
        }
    }

    /**
     * Show the search results submenu, with selected options available.
     *
     * @param listener listener for item click events
     * @param canRefine can this search result be refined with an advanced search form?
     * @param canStartSelfStudy can a self-study quiz be started from this result
     * @param canResurrect does the current result have resurrectable subjects
     * @param canBurn does the current result have burnable subjects
     */
    public void showSearchResultMenu(final MenuItem.OnMenuItemClickListener listener, final boolean canRefine,
                                     final boolean canStartSelfStudy, final boolean canResurrect, final boolean canBurn) {
        final @Nullable Menu menu = getMenu();
        if (menu != null) {
            final @Nullable MenuItem subMenu = menu.findItem(R.id.action_search_result);
            if (subMenu != null) {
                final @Nullable MenuItem refineItem = menu.findItem(R.id.action_search_result_refine);
                if (refineItem != null) {
                    refineItem.setOnMenuItemClickListener(listener);
                    refineItem.setVisible(canRefine);
                }
                final @Nullable MenuItem savePresetItem = menu.findItem(R.id.action_search_result_save_preset);
                if (savePresetItem != null) {
                    savePresetItem.setOnMenuItemClickListener(listener);
                    savePresetItem.setVisible(true);
                }
                final @Nullable MenuItem selfStudyItem = menu.findItem(R.id.action_search_result_self_study);
                if (selfStudyItem != null) {
                    selfStudyItem.setOnMenuItemClickListener(listener);
                    selfStudyItem.setVisible(canStartSelfStudy);
                }
                final @Nullable MenuItem resurrectItem = menu.findItem(R.id.action_search_result_resurrect);
                if (resurrectItem != null) {
                    resurrectItem.setOnMenuItemClickListener(listener);
                    resurrectItem.setVisible(canResurrect);
                }
                final @Nullable MenuItem burnItem = menu.findItem(R.id.action_search_result_burn);
                if (burnItem != null) {
                    burnItem.setOnMenuItemClickListener(listener);
                    burnItem.setVisible(canBurn);
                }
                subMenu.setVisible(true);
            }
        }
    }
}
