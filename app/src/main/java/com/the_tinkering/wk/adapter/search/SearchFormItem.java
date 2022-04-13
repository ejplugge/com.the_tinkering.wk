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

package com.the_tinkering.wk.adapter.search;

import androidx.core.util.Consumer;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;

import javax.annotation.Nullable;

/**
 * Item for the search form in the view.
 */
public final class SearchFormItem extends ResultItem {
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public @Nullable ResultItem getItem(final int position) {
        if (position == 0) {
            return this;
        }
        return null;
    }

    @Override
    public int getViewType() {
        return R.id.viewTypeResultSearchForm;
    }

    @Override
    public int getSpanSize(final int spans) {
        return spans;
    }

    @Override
    protected void iterateSubjects(final Consumer<? super Subject> consumer) {
        //
    }
}
