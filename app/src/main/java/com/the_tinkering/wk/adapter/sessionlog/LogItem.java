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

package com.the_tinkering.wk.adapter.sessionlog;

import javax.annotation.Nullable;

/**
 * Abstract base class for items in the session log view.
 */
public abstract class LogItem {
    /**
     * Get the count of visible items at and under this item.
     *
     * @return the count
     */
    protected abstract int getCount();

    /**
     * Get the item at the given position, starting the count at this item.
     *
     * @param position the position to look for
     * @return the item or null if the position is out of bounds
     */
    protected abstract @Nullable LogItem getItem(final int position);

    /**
     * Get the view type for this item.
     *
     * @return the view type
     */
    public abstract int getViewType();

    /**
     * Get the size of this item in spans (columns).
     *
     * @param spans the total number of spans in the layout manager
     * @return the span size
     */
    public abstract int getSpanSize(final int spans);
}
