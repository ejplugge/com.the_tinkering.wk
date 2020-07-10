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

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.fragments.PreferencesFragment;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;

/**
 * Activity for the preferences.
 */
public final class PreferencesActivity extends AbstractActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    private static final Logger LOGGER = Logger.get(PreferencesActivity.class);

    /**
     * The constructor.
     */
    public PreferencesActivity() {
        super(R.layout.activity_preferences, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final FragmentManager manager = getSupportFragmentManager();
            @Nullable PreferencesFragment fragment = (PreferencesFragment) manager.findFragmentByTag("preferencesFragment");
            if (fragment == null) {
                fragment = new PreferencesFragment();
            }

            final @Nullable String rootKey = getIntent().getStringExtra("rootKey");
            if (!isEmpty(rootKey)) {
                final Bundle args = new Bundle();
                args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, rootKey);
                fragment.setArguments(args);
            }

            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment, fragment, "preferencesFragment");
            transaction.commitNow();
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

    @Override
    public boolean onPreferenceStartScreen(final PreferenceFragmentCompat caller, final PreferenceScreen pref) {
        try {
            final PreferencesFragment fragment = new PreferencesFragment();
            final Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey());
            fragment.setArguments(args);

            final FragmentManager manager = getSupportFragmentManager();
            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment, fragment, pref.getKey());
            transaction.setReorderingAllowed(true);
            transaction.addToBackStack(pref.getKey());
            transaction.commit();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }

        return true;
    }
}
