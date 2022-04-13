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

package com.the_tinkering.wk.components;

import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

/**
 * A custom preference that combines two non-negative number fields, each of which may be empty.
 * -1 is used as the special value for an empty field.
 */
public final class NumberRangePreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_MIN = "NumberRangePreferenceDialogFragment.min";
    private static final String SAVE_STATE_MAX = "NumberRangePreferenceDialogFragment.max";

    private final ViewProxy minInput = new ViewProxy();
    private final ViewProxy maxInput = new ViewProxy();
    private final ViewProxy message = new ViewProxy();
    private int min = -1;
    private int max = -1;

    /**
     * Create a new instance for the given key.
     *
     * @param key the key
     * @return the instance
     */
    public static NumberRangePreferenceDialogFragment newInstance(final String key) {
        final NumberRangePreferenceDialogFragment fragment = new NumberRangePreferenceDialogFragment();
        final Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private NumberRangePreference getNumberRangePreference() {
        return (NumberRangePreference) requireNonNull(getPreference());
    }

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        safe(() -> {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                min = getNumberRangePreference().getMin();
                max = getNumberRangePreference().getMax();
            }
            else {
                min = savedInstanceState.getInt(SAVE_STATE_MIN, -1);
                max = savedInstanceState.getInt(SAVE_STATE_MAX, -1);
            }
        });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        safe(() -> {
            super.onSaveInstanceState(outState);
            outState.putInt(SAVE_STATE_MIN, min);
            outState.putInt(SAVE_STATE_MAX, max);
        });
    }

    @Override
    protected void onBindDialogView(final View view) {
        safe(() -> {
            super.onBindDialogView(view);

            minInput.setDelegate(view, R.id.minInput);
            maxInput.setDelegate(view, R.id.maxInput);
            message.setDelegate(view, R.id.message);

            message.setText("Choose a minimum and maximum value for this category. Leave a field empty to not constrain that value.");

            minInput.setText(min == -1 ? "" : Integer.toString(min));
            maxInput.setText(max == -1 ? "" : Integer.toString(max));
        });
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        if (positiveResult) {
            safe(() -> {
                final String minText = minInput.getText();
                if (isEmpty(minText)) {
                    getNumberRangePreference().setMin(-1);
                }
                else {
                    getNumberRangePreference().setMin(Integer.parseInt(minText, 10));
                }
            });
            safe(() -> {
                final String maxText = maxInput.getText();
                if (isEmpty(maxText)) {
                    getNumberRangePreference().setMax(-1);
                }
                else {
                    getNumberRangePreference().setMax(Integer.parseInt(maxText, 10));
                }
            });
        }
    }
}
