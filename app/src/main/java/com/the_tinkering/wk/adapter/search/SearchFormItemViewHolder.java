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

package com.the_tinkering.wk.adapter.search;

import android.app.Activity;
import android.view.View;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.activities.BrowseActivity;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.fragments.AbstractFragment;
import com.the_tinkering.wk.fragments.SearchResultFragment;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.WeakLcoRef;

import javax.annotation.Nullable;

/**
 * View holder class for the search form.
 */
public final class SearchFormItemViewHolder extends ResultItemViewHolder {
    private static final Logger LOGGER = Logger.get(SearchFormItemViewHolder.class);

    private final ViewProxy form = new ViewProxy();

    private final WeakLcoRef<SearchResultFragment> fragmentRef;

    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param parameters the search parameters for the form
     * @param fragment the fragment this view belongs to
     */
    public SearchFormItemViewHolder(final SearchResultAdapter adapter, final View view, final AdvancedSearchParameters parameters,
                                    final SearchResultFragment fragment) {
        super(adapter, view);
        fragmentRef = new WeakLcoRef<>(fragment);
        form.setDelegate(view, R.id.form);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    final @Nullable AbstractFragment theFragment = fragmentRef.getOrElse(null);
                    if (theFragment == null) {
                        return;
                    }
                    final @Nullable Activity activity = theFragment.getActivity();
                    if (activity instanceof BrowseActivity) {
                        ((BrowseActivity) activity).loadSearchResultFragment(fragmentRef.get().getPresetName(), 2,
                                Converters.getObjectMapper().writeValueAsString(form.extractParameters()));
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }
        };

        new ViewProxy(view, R.id.searchButton1).setOnClickListener(listener);
        new ViewProxy(view, R.id.searchButton2).setOnClickListener(listener);

        form.injectParameters(parameters);
    }

    @Override
    public void bind(final ResultItem newItem) {
        //
    }
}
