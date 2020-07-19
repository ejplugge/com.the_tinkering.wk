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

import android.view.View;

import static com.the_tinkering.wk.util.TextUtil.formatTimestamp;

/**
 * View holder class for available-at header items.
 */
public final class AvailableAtHeaderItemViewHolder extends HeaderItemViewHolder {
    private final long searchTime;

    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param searchTime the time the search was started
     */
    public AvailableAtHeaderItemViewHolder(final SearchResultAdapter adapter, final View view, final long searchTime) {
        super(adapter, view);
        this.searchTime = searchTime;
    }

    @Override
    public void bind(final ResultItem newItem) {
        if (!(newItem instanceof AvailableAtHeaderItem)) {
            return;
        }
        final AvailableAtHeaderItem item = (AvailableAtHeaderItem) newItem;

        bindCommon(item);

        final String titleStr;
        if (item.getTimestamp() == 0) {
            titleStr = "No review scheduled";
        }
        else if (item.getTimestamp() <= searchTime) {
            titleStr = "Review available now";
        }
        else {
            titleStr = formatTimestamp(item.getTimestamp());
        }
        title.setText(titleStr);
    }
}
