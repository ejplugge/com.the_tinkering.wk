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

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.Identification;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.fragments.BrowseOverviewFragment;
import com.the_tinkering.wk.fragments.SearchResultFragment;
import com.the_tinkering.wk.fragments.SubjectInfoFragment;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * An activity for browsing/searching the subject database.
 */
public final class BrowseActivity extends AbstractActivity {
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
                @Nullable long[] ids = getIntent().getLongArrayExtra("ids");
                if (ids == null) {
                    ids = new long[0];
                }
                loadSubjectInfoFragment(id, ids, FragmentTransitionAnimation.NONE);
                return;
            }

            final @Nullable Uri uri = getIntent().getData();
            if (uri != null && Identification.APP_URI_SCHEME.equals(uri.getScheme()) && "subject-info".equals(uri.getAuthority())) {
                final @Nullable String s = uri.getPath();
                if (s != null) {
                    final int p = s.lastIndexOf('/');
                    if (p >= 0) {
                        safe(() -> {
                            final long searchId = Long.parseLong(s.substring(p+1));
                            loadSubjectInfoFragment(searchId, new long[0], FragmentTransitionAnimation.NONE);
                        });
                        return;
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
        final Fragment fragment = SearchResultFragment.newInstance(searchType, searchParameters, presetName);
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
    public void loadSubjectInfoFragment(final long id, final long[] ids, final FragmentTransitionAnimation animation) {
        final Fragment fragment = SubjectInfoFragment.newInstance(id, ids);
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
                final @Nullable MenuItem star0Item = menu.findItem(R.id.action_search_result_star0);
                if (star0Item != null) {
                    star0Item.setOnMenuItemClickListener(listener);
                    star0Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                final @Nullable MenuItem star1Item = menu.findItem(R.id.action_search_result_star1);
                if (star1Item != null) {
                    star1Item.setOnMenuItemClickListener(listener);
                    star1Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                final @Nullable MenuItem star2Item = menu.findItem(R.id.action_search_result_star2);
                if (star2Item != null) {
                    star2Item.setOnMenuItemClickListener(listener);
                    star2Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                final @Nullable MenuItem star3Item = menu.findItem(R.id.action_search_result_star3);
                if (star3Item != null) {
                    star3Item.setOnMenuItemClickListener(listener);
                    star3Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                final @Nullable MenuItem star4Item = menu.findItem(R.id.action_search_result_star4);
                if (star4Item != null) {
                    star4Item.setOnMenuItemClickListener(listener);
                    star4Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                final @Nullable MenuItem star5Item = menu.findItem(R.id.action_search_result_star5);
                if (star5Item != null) {
                    star5Item.setOnMenuItemClickListener(listener);
                    star5Item.setVisible(GlobalSettings.Other.getEnableStarsRatings());
                }
                subMenu.setVisible(true);
            }
        }
    }
}
