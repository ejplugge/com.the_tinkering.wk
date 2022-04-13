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

import com.the_tinkering.wk.db.model.Subject;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Abstract base class for items that can contain other items.
 */
public abstract class ContainerItem extends ResultItem {
    /**
     * The sub-items.
     */
    protected final Collection<ResultItem> items = new ArrayList<>();

    /**
     * Add an item to the list of direct sub-items of this one.
     *
     * @param item the item to add
     */
    public final void addItem(final ResultItem item) {
        items.add(item);
    }

    /**
     * Find a direct child header item with the given tag.
     *
     * @param tag the tag to look for
     * @return the item or null if not found
     */
    public final @Nullable HeaderItem findHeaderItem(final String tag) {
        for (final ResultItem item: items) {
            if (item instanceof HeaderItem) {
                final HeaderItem headerItem = (HeaderItem) item;
                if (headerItem.getTag().equals(tag)) {
                    return headerItem;
                }
            }
        }
        return null;
    }

    @Override
    public final void iterateSubjects(final Consumer<? super Subject> consumer) {
        for (final ResultItem item: items) {
            item.iterateSubjects(consumer);
        }
    }
}
