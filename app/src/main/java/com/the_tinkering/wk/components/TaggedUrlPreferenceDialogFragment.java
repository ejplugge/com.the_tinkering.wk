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

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

/**
 * A custom preference that combines two related edittext preferences: a URL and a name/tag describing it.
 */
public final class TaggedUrlPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_TAG = "TaggedUrlPreferenceDialogFragment.tag";
    private static final String SAVE_STATE_URL = "TaggedUrlPreferenceDialogFragment.url";

    private final ViewProxy tagInput = new ViewProxy();
    private final ViewProxy urlInput = new ViewProxy();
    private final ViewProxy message = new ViewProxy();
    private String tag = "";
    private String url = "";

    /**
     * Create a new instance for the given key.
     *
     * @param key the key
     * @return the instance
     */
    public static TaggedUrlPreferenceDialogFragment newInstance(final String key) {
        final TaggedUrlPreferenceDialogFragment fragment = new TaggedUrlPreferenceDialogFragment();
        final Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private TaggedUrlPreference getTaggedUrlPreference() {
        return (TaggedUrlPreference) requireNonNull(getPreference());
    }

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        safe(() -> {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                tag = getTaggedUrlPreference().getTag();
                url = getTaggedUrlPreference().getUrl();
            }
            else {
                tag = savedInstanceState.getString(SAVE_STATE_TAG, "");
                url = savedInstanceState.getString(SAVE_STATE_URL, "");
            }
        });
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        safe(() -> {
            super.onSaveInstanceState(outState);
            outState.putString(SAVE_STATE_TAG, tag);
            outState.putString(SAVE_STATE_URL, url);
        });
    }

    @Override
    protected void onBindDialogView(final View view) {
        safe(() -> {
            super.onBindDialogView(view);

            tagInput.setDelegate(view, R.id.tagInput);
            urlInput.setDelegate(view, R.id.urlInput);
            message.setDelegate(view, R.id.message);

            message.setText("Search engines are shown when long-pressing on a subject title. In the URL, \"%s\" will be replaced"
                    + " with the search query.");

            tagInput.setText(tag);
            urlInput.setText(url);

            urlInput.setSingleLine();
            urlInput.setMaxLines(5);
            urlInput.setHorizontallyScrolling(false);
            urlInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        });
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        safe(() -> {
            if (positiveResult) {
                getTaggedUrlPreference().setTag(tagInput.getText());
                getTaggedUrlPreference().setUrl(urlInput.getText());
            }
        });
    }
}
