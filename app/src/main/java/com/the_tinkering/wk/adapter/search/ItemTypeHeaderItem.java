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
import com.the_tinkering.wk.enums.SubjectType;

import javax.annotation.Nullable;

/**
 * Item for subject type headers.
 */
public final class ItemTypeHeaderItem extends HeaderItem {
    private final SubjectType type;

    /**
     * The constructor.
     *
     * @param parentTag tag for the parent header, or null if this is a top-level header.
     * @param type the subject type
     */
    public ItemTypeHeaderItem(final @Nullable String parentTag, final SubjectType type) {
        super(parentTag == null ? type.name() : parentTag + " " + type.name());
        this.type = type;
    }

    /**
     * The subject type for this header.
     *
     * @return the type
     */
    public SubjectType getType() {
        return type;
    }

    @Override
    public int getViewType() {
        return R.id.viewTypeResultItemTypeHeader;
    }

    @Override
    public int getSpanSize(final int spans) {
        return spans;
    }
}
