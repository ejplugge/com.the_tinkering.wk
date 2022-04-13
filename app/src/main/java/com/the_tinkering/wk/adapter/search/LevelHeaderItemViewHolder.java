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

import android.view.View;

/**
 * View holder class for level header items.
 */
public final class LevelHeaderItemViewHolder extends HeaderItemViewHolder {
    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     */
    public LevelHeaderItemViewHolder(final SearchResultAdapter adapter, final View view) {
        super(adapter, view);
    }

    @Override
    public void bind(final ResultItem newItem) {
        if (!(newItem instanceof LevelHeaderItem)) {
            return;
        }
        final LevelHeaderItem item = (LevelHeaderItem) newItem;

        bindCommon(item);

        final String titleStr = "Level " + item.getLevel();
        title.setText(titleStr);
    }
}
