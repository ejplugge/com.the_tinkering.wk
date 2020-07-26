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

package com.the_tinkering.wk.adapter.sessionlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Abstract base class for items that can contain other items.
 */
public abstract class ContainerItem extends LogItem {
    /**
     * The sub-items.
     */
    protected final Collection<LogItem> items = new ArrayList<>();

    /**
     * The contained items. This is filled even if this item is collapsed.
     *
     * @return the items
     */
    public final Collection<LogItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    /**
     * Add an item to the list of direct sub-items of this one.
     *
     * @param item the item to add
     */
    public final void addItem(final LogItem item) {
        items.add(item);
    }

    /**
     * Does this header currently contain no items?.
     *
     * @return true if it does
     */
    public final boolean isEmpty() {
        return items.isEmpty();
    }
}
