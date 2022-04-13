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

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.SessionItem;

import javax.annotation.Nullable;

/**
 * Item for subject type headers.
 */
public final class SessionItemItem extends LogItem {
    private final SessionItem sessionItem;
    private final HeaderItem parent;
    private final boolean showButtonInRestrictedMode;
    private final boolean clickableInRestrictedMode;

    /**
     * The constructor.
     *
     * @param sessionItem the session item for this item
     * @param parent the parent this item is contained in
     * @param showButtonInRestrictedMode If the full session log is not shown, should this item show the subject title button?
     * @param clickableInRestrictedMode If the full session log is not shown, should this item be clickable?
     */
    public SessionItemItem(final SessionItem sessionItem, final HeaderItem parent,
                           final boolean showButtonInRestrictedMode, final boolean clickableInRestrictedMode) {
        this.sessionItem = sessionItem;
        this.parent = parent;
        this.showButtonInRestrictedMode = showButtonInRestrictedMode;
        this.clickableInRestrictedMode = clickableInRestrictedMode;
    }

    /**
     * The session item for this item.
     *
     * @return the session item
     */
    public SessionItem getSessionItem() {
        return sessionItem;
    }

    /**
     * The parent this item is contained in.
     *
     * @return the parent
     */
    public HeaderItem getParent() {
        return parent;
    }

    /**
     * If the full session log is not shown, should this item show the subject title button?.
     *
     * @return true if it should
     */
    public boolean isShowButtonInRestrictedMode() {
        return showButtonInRestrictedMode;
    }

    /**
     * If the full session log is not shown, should this item be clickable?.
     *
     * @return true if it should
     */
    public boolean isClickableInRestrictedMode() {
        return clickableInRestrictedMode;
    }

    @Override
    public int getViewType() {
        return R.id.viewTypeLogItem;
    }

    @Override
    public int getSpanSize(final int spans) {
        return spans >= 6 ? 3 : spans;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public @Nullable LogItem getItem(final int position) {
        return position == 0 ? this : null;
    }
}
