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

import com.the_tinkering.wk.R;

import javax.annotation.Nullable;

/**
 * Item for header entries in the view.
 */
public abstract class HeaderItem extends ContainerItem {
    private final String tag;
    private boolean collapsed;

    /**
     * The constructor.
     *
     * @param tag the tag for this item, unique among its siblings.
     * @param collapsed true if this header is initially collapsed
     */
    protected HeaderItem(final String tag, final boolean collapsed) {
        this.tag = tag;
        this.collapsed = collapsed;
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

    @Override
    public final int getViewType() {
        return R.id.viewTypeLogHeader;
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
            for (final LogItem item: items) {
                count += item.getCount();
            }
        }
        return count;
    }

    @Override
    public final @Nullable LogItem getItem(final int position) {
        if (position == 0) {
            return this;
        }
        if (!collapsed) {
            int pos = position - 1;
            for (final LogItem item: items) {
                final int count = item.getCount();
                if (pos < count) {
                    return item.getItem(pos);
                }
                pos -= count;
            }
        }
        return null;
    }

    @Override
    public final int getSpanSize(final int spans) {
        return spans;
    }

    /**
     * The main title text for this header.
     *
     * @return the text
     */
    public abstract String getTitleText();

    /**
     * The small details text for this header.
     *
     * @return the text
     */
    public abstract String getDetailsText();
}
