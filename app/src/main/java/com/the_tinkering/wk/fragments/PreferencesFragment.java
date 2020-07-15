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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.activities.AboutActivity;
import com.the_tinkering.wk.activities.AbstractActivity;
import com.the_tinkering.wk.activities.FontImportActivity;
import com.the_tinkering.wk.activities.FontSelectionActivity;
import com.the_tinkering.wk.activities.KeyboardHelpActivity;
import com.the_tinkering.wk.activities.SupportActivity;
import com.the_tinkering.wk.activities.ThemeCustomizationActivity;
import com.the_tinkering.wk.api.ApiState;
import com.the_tinkering.wk.components.TaggedUrlPreference;
import com.the_tinkering.wk.components.TaggedUrlPreferenceDialogFragment;
import com.the_tinkering.wk.jobs.ResetDatabaseJob;
import com.the_tinkering.wk.livedata.LiveApiState;
import com.the_tinkering.wk.services.JobRunnerService;
import com.the_tinkering.wk.util.AudioUtil;
import com.the_tinkering.wk.util.DbLogger;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.ENABLE_ADVANCED_WARNING;
import static com.the_tinkering.wk.Constants.RESET_DATABASE_WARNING;
import static com.the_tinkering.wk.Constants.RESET_TUTORIALS_WARNING;
import static com.the_tinkering.wk.Constants.UPLOAD_DEBUG_LOG_WARNING;
import static com.the_tinkering.wk.util.ObjectSupport.isTrue;
import static com.the_tinkering.wk.util.TextUtil.renderHtml;

/**
 * Fragment for preferences.
 */
public final class PreferencesFragment extends PreferenceFragmentCompat {
    private static final Logger LOGGER = Logger.get(PreferencesFragment.class);

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            view.setBackgroundColor(ThemeUtil.getColor(R.attr.colorBackground));

            final @Nullable Preference enableAdvanced = findPreference("enable_advanced");
            if (enableAdvanced != null) {
                enableAdvanced.setOnPreferenceChangeListener((preference, newValue) -> {
                    try {
                        final boolean enabled = isTrue(newValue);
                        if (enabled && !GlobalSettings.getAdvancedEnabled()) {
                            new AlertDialog.Builder(view.getContext())
                                    .setTitle("Enable advanced settings?")
                                    .setMessage(renderHtml(ENABLE_ADVANCED_WARNING))
                                    .setIcon(R.drawable.ic_baseline_warning_24px)
                                    .setNegativeButton("No", (dialog, which) -> {
                                        try {
                                            GlobalSettings.setAdvancedEnabled(false);
                                            setVisibleIf("advanced_lesson_settings", false);
                                            setVisibleIf("advanced_review_settings", false);
                                            setVisibleIf("advanced_self_study_settings", false);
                                            setVisibleIf("advanced_other_settings", false);
                                            ((TwoStatePreference) preference).setChecked(false);
                                        } catch (final Exception e) {
                                            LOGGER.uerr(e);
                                        }
                                    })
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        try {
                                            GlobalSettings.setAdvancedEnabled(true);
                                            setVisibleIf("advanced_lesson_settings", true);
                                            setVisibleIf("advanced_review_settings", true);
                                            setVisibleIf("advanced_self_study_settings", true);
                                            setVisibleIf("advanced_other_settings", true);
                                            ((TwoStatePreference) preference).setChecked(true);
                                        } catch (final Exception e) {
                                            LOGGER.uerr(e);
                                        }
                                    }).create().show();
                            return false;
                        }
                        else {
                            setVisibleIf("advanced_lesson_settings", enabled);
                            setVisibleIf("advanced_review_settings", enabled);
                            setVisibleIf("advanced_self_study_settings", enabled);
                            setVisibleIf("advanced_other_settings", enabled);
                        }
                        return true;
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                        return false;
                    }
                });
            }

            setOnPreferenceClick("reset_database", preference -> {
                try {
                    new AlertDialog.Builder(preference.getContext())
                            .setTitle("Reset database?")
                            .setMessage(renderHtml(RESET_DATABASE_WARNING))
                            .setIcon(R.drawable.ic_baseline_warning_24px)
                            .setNegativeButton("No", (dialog, which) -> {
                                //
                            })
                            .setPositiveButton("Yes", (dialog, which) -> {
                                try {
                                    JobRunnerService.schedule(ResetDatabaseJob.class, "");
                                    goToMainActivity();
                                } catch (final Exception e) {
                                    LOGGER.uerr(e);
                                }
                            }).create().show();
                    return true;
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                    return false;
                }
            });

            setOnPreferenceClick("reset_tutorials", preference -> {
                try {
                    new AlertDialog.Builder(preference.getContext())
                            .setTitle("Reset confirmations and tutorials?")
                            .setMessage(renderHtml(RESET_TUTORIALS_WARNING))
                            .setIcon(R.drawable.ic_baseline_warning_24px)
                            .setNegativeButton("No", (dialog, which) -> {
                                //
                            })
                            .setPositiveButton("Yes", (dialog, which) -> {
                                try {
                                    GlobalSettings.resetConfirmationsAndTutorials();
                                    Toast.makeText(preference.getContext(), "Confirmations and tutorials reset", Toast.LENGTH_LONG).show();
                                } catch (final Exception e) {
                                    LOGGER.uerr(e);
                                }
                            }).create().show();
                    return true;
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                    return false;
                }
            });

            setOnPreferenceClick("upload_debug_log", preference -> {
                try {
                    new AlertDialog.Builder(preference.getContext())
                            .setTitle("Upload debug log?")
                            .setMessage(renderHtml(UPLOAD_DEBUG_LOG_WARNING))
                            .setIcon(R.drawable.ic_baseline_warning_24px)
                            .setNegativeButton("No", (dialog, which) -> {
                                //
                            })
                            .setPositiveButton("Yes", (dialog, which) -> {
                                try {
                                    new Task(preference.getContext()).execute();
                                } catch (final Exception e) {
                                    LOGGER.uerr(e);
                                }
                            }).create().show();
                    return true;
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                    return false;
                }
            });

            final @Nullable ListPreference audioLocation = findPreference("audio_location");
            if (audioLocation != null) {
                final List<String> locationValues = AudioUtil.getLocationValues();
                final List<String> locations = AudioUtil.getLocations(locationValues);
                audioLocation.setEntries(locations.toArray(new String[] {}));
                audioLocation.setEntryValues(locationValues.toArray(new String[] {}));
                audioLocation.setVisible(true);
            }

            setVisibleIf("api_key_help", LiveApiState.getInstance().get() != ApiState.OK);
            setVisibleIf("advanced_lesson_settings", GlobalSettings.getAdvancedEnabled());
            setVisibleIf("advanced_review_settings", GlobalSettings.getAdvancedEnabled());
            setVisibleIf("advanced_self_study_settings", GlobalSettings.getAdvancedEnabled());
            setVisibleIf("advanced_other_settings", GlobalSettings.getAdvancedEnabled());
            setVisibleIf("ime_hint_reading", Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
            setVisibleIf("ime_hint_meaning", Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
            setVisibleIf("web_password", Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

            setNumberInputType("overdue_threshold");
            setNumberInputType("max_lesson_session_size");
            setNumberInputType("max_review_session_size");
            setNumberInputType("max_self_study_session_size");
            setDecimalNumberInputType("next_button_delay");

            setOnClickGoToActivity("about_this_app", AboutActivity.class);
            setOnClickGoToActivity("support_and_feedback", SupportActivity.class);
            setOnClickGoToActivity("theme_customization", ThemeCustomizationActivity.class);
            setOnClickGoToActivity("font_selection", FontSelectionActivity.class);
            setOnClickGoToActivity("font_import", FontImportActivity.class);
            setOnClickGoToActivity("keyboard_help", KeyboardHelpActivity.class);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            final @Nullable Bundle args = getArguments();
            @Nullable CharSequence title = null;
            if (args != null) {
                final @Nullable String key = args.getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);
                if (key != null) {
                    final @Nullable Preference preference = findPreference(key);
                    if (preference != null) {
                        title = preference.getTitle();
                    }
                }
            }
            if (title == null) {
                title = "Settings";
            }
            final @Nullable Activity activity = getActivity();
            if (activity instanceof Actment) {
                final @Nullable Toolbar toolbar = ((Actment) activity).getToolbar();
                if (toolbar != null) {
                    toolbar.setTitle(title);
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(final Preference preference) {
        try {
            if (preference instanceof TaggedUrlPreference) {
                if (getParentFragmentManager().findFragmentByTag("TaggedUrlPreference") != null) {
                    return;
                }
                final DialogFragment f = TaggedUrlPreferenceDialogFragment.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(getParentFragmentManager(), "TaggedUrlPreference");
                return;
            }

            super.onDisplayPreferenceDialog(preference);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void goToActivity(final Class<? extends AbstractActivity> clas) {
        final @Nullable Activity a = getActivity();
        if (a instanceof Actment) {
            ((Actment) a).goToActivity(clas);
        }
    }

    private void goToMainActivity() {
        final @Nullable Activity a = getActivity();
        if (a instanceof Actment) {
            ((Actment) a).goToMainActivity();
        }
    }

    private void setOnPreferenceClick(final CharSequence key, final @Nullable Preference.OnPreferenceClickListener listener) {
        try {
            final @Nullable Preference pref = findPreference(key);
            if (pref != null) {
                pref.setOnPreferenceClickListener(listener);
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void setOnClickGoToActivity(final CharSequence key, final Class<? extends AbstractActivity> clas) {
        try {
            setOnPreferenceClick(key, preference -> {
                try {
                    goToActivity(clas);
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
                return true;
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void setNumberInputType(final CharSequence key) {
        try {
            final @Nullable EditTextPreference pref = findPreference(key);
            if (pref != null) {
                pref.setOnBindEditTextListener(editText -> {
                    try {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setSelection(editText.getText().length());
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                });
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void setDecimalNumberInputType(@SuppressWarnings("SameParameterValue") final CharSequence key) {
        try {
            final @Nullable EditTextPreference pref = findPreference(key);
            if (pref != null) {
                pref.setOnBindEditTextListener(editText -> {
                    try {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        editText.setSelection(editText.getText().length());
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                });
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void setVisibleIf(final CharSequence key, final boolean visible) {
        try {
            final @Nullable Preference pref = findPreference(key);
            if (pref != null) {
                pref.setVisible(visible);
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private static final class Task extends AsyncTask<Void, Void, Boolean> {
        private final Context context;

        private Task(final Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                return DbLogger.uploadLog();
            } catch (final Exception e) {
                LOGGER.uerr(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            try {
                if (result) {
                    Toast.makeText(context, "Upload successful, thanks!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show();
                }
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
