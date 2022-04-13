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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceManager;

import com.the_tinkering.wk.R;

import java.util.Locale;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.orElse;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom preference that combines two non-negative number fields, each of which may be empty.
 * -1 is used as the special value for an empty field.
 */
public final class NumberRangePreference extends DialogPreference {
    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     * @param defStyleAttr default style
     * @param defStyleRes default style resource
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public NumberRangePreference(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     * @param defStyleAttr default style
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public NumberRangePreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public NumberRangePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public NumberRangePreference(final Context context) {
        super(context);
        init();
    }

    private void init() {
        safe(() -> {
            setPersistent(false);
            setDialogLayoutResource(R.layout.pref_number_range);
            setSummaryProvider((SummaryProvider<NumberRangePreference>) preference -> {
                if (preference.getMin() == -1) {
                    return preference.getMax() == -1
                            ? "Unlimited"
                            : "Max " + preference.getMax();
                }
                else {
                    return preference.getMax() == -1
                            ? "Min " + preference.getMin()
                            : String.format(Locale.ROOT, "%d - %d", preference.getMin(), preference.getMax());
                }
            });
        });
    }

    @Override
    protected Object onGetDefaultValue(final TypedArray a, final int index) {
        return orElse(a.getString(index), "");
    }

    @Override
    protected void onSetInitialValue(final @Nullable Object defaultValue) {
        //
    }

    /**
     * The minimum value for this preference.
     * @return the value
     */
    public int getMin() {
        return safe(-1, () -> {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            return prefs.getInt(getKey() + "_min", -1);
        });
    }

    /**
     * The minimum value for this preference.
     * @param value the value
     */
    public void setMin(final int value) {
        safe(() -> {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getKey() + "_min", value);
            editor.apply();
            notifyChanged();
        });
    }

    /**
     * The maximum value for this preference.
     * @return the value
     */
    public int getMax() {
        return safe(-1, () -> {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            return prefs.getInt(getKey() + "_max", -1);
        });
    }

    /**
     * The maximum value for this preference.
     * @param value the value
     */
    public void setMax(final int value) {
        safe(() -> {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getKey() + "_max", value);
            editor.apply();
            notifyChanged();
        });
    }
}
