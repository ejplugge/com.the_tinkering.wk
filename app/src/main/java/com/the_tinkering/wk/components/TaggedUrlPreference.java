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

package com.the_tinkering.wk.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.orElse;

/**
 * A custom preference that combines two related edittext preferences: a URL and a name/tag describing it.
 */
public final class TaggedUrlPreference extends DialogPreference {
    private static final Logger LOGGER = Logger.get(TaggedUrlPreference.class);

    private String defaultTag = "";
    private String defaultUrl = "";

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     * @param defStyleAttr default style
     * @param defStyleRes default style resource
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public TaggedUrlPreference(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     * @param defStyleAttr default style
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public TaggedUrlPreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attributes from XML
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public TaggedUrlPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     */
    @SuppressWarnings({"WeakerAccess", "RedundantSuppression", "unused"})
    public TaggedUrlPreference(final Context context) {
        super(context);
        init(null);
    }

    private void init(final @Nullable AttributeSet attrs) {
        try {
            setPersistent(false);
            setDialogLayoutResource(R.layout.pref_tagged_url);
            setSummaryProvider(new Preference.SummaryProvider<TaggedUrlPreference>() {
                @Override
                public CharSequence provideSummary(final TaggedUrlPreference preference) {
                    return getTag();
                }
            });

            final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TaggedUrlPreference, 0, 0);
            try {
                defaultTag = orElse(a.getString(R.styleable.TaggedUrlPreference_defaultTag), "");
            }
            finally {
                a.recycle();
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    protected Object onGetDefaultValue(final TypedArray a, final int index) {
        return orElse(a.getString(index), "");
    }

    @Override
    protected void onSetInitialValue(final @Nullable Object defaultValue) {
        defaultUrl = orElse((String) defaultValue, "");
    }

    /**
     * The tag value for this preference.
     * @return the value
     */
    public String getTag() {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            return prefs.getString(getKey() + "_tag", defaultTag);
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return "";
        }
    }

    /**
     * The tag value for this preference.
     * @param value the value
     */
    public void setTag(final String value) {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getKey() + "_tag", value);
            editor.apply();
            notifyChanged();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * The URL value for this preference.
     * @return the value
     */
    public String getUrl() {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            return prefs.getString(getKey(), defaultUrl);
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return "";
        }
    }

    /**
     * The URL value for this preference.
     * @param value the value
     */
    public void setUrl(final String value) {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getKey(), value);
            editor.apply();
            notifyChanged();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }
}
