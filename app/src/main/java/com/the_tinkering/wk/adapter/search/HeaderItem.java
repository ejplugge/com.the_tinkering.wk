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

import javax.annotation.Nullable;

/**
 * Item for header entries in the view.
 */
public abstract class HeaderItem extends ContainerItem {
    private final String tag;
    private boolean collapsed = false;

    /**
     * The constructor.
     *
     * @param tag the tag for this item, unique among its siblings.
     */
    protected HeaderItem(final String tag) {
        this.tag = tag;
    }

    /**
     * The tag for this item, unique among its siblings.
     *
     * @return the tag
     */
    public final String getTag() {
        return tag;
    }

    /**
     * Is this header currently collapsed?.
     *
     * @return true if it is
     */
    public final boolean isCollapsed() {
        return collapsed;
    }

    /**
     * Is this header currently collapsed?.
     *
     * @param collapsed true if it is
     */
    public final void setCollapsed(final boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public final int getCount() {
        int count = 1;
        if (!collapsed) {
            for (final ResultItem item: items) {
                count += item.getCount();
            }
        }
        return count;
    }

    @Override
    public final @Nullable ResultItem getItem(final int position) {
        if (position == 0) {
            return this;
        }
        if (!collapsed) {
            int pos = position - 1;
            for (final ResultItem item: items) {
                final int count = item.getCount();
                if (pos < count) {
                    return item.getItem(pos);
                }
                pos -= count;
            }
        }
        return null;
    }
}
