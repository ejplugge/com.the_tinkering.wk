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

import javax.annotation.Nullable;

/**
 * The root of the result item tree, is not itself shown in the display or counted in the adapter view.
 */
public final class RootItem extends ContainerItem {
    @Override
    public int getCount() {
        int count = 0;
        for (final ResultItem item: items) {
            count += item.getCount();
        }
        return count;
    }

    @Override
    public @Nullable ResultItem getItem(final int position) {
        int pos = position;
        for (final ResultItem item: items) {
            final int count = item.getCount();
            if (pos < count) {
                return item.getItem(pos);
            }
            pos -= count;
        }
        return null;
    }

    @Override
    public int getViewType() {
        return -1;
    }

    @Override
    public int getSpanSize(final int spans) {
        return 1;
    }

    /**
     * Remove all contained items.
     */
    public void clear() {
        items.clear();
    }
}
