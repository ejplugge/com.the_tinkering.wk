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

import com.the_tinkering.wk.R;

/**
 * Item for available-at headers.
 */
public final class AvailableAtHeaderItem extends HeaderItem {
    private final long timestamp;

    /**
     * The constructor.
     *
     * @param timestamp the timestamp for this header
     */
    public AvailableAtHeaderItem(final long timestamp) {
        super(Long.toString(timestamp));
        this.timestamp = timestamp;
    }

    @Override
    public int getViewType() {
        return R.id.viewTypeResultAvailableAtHeader;
    }

    @Override
    public int getSpanSize(final int spans) {
        return spans;
    }

    /**
     * The timestamp for the next available review.
     *
     * @return the timestamp or 0 if no review scheduled
     */
    public long getTimestamp() {
        return timestamp;
    }
}
