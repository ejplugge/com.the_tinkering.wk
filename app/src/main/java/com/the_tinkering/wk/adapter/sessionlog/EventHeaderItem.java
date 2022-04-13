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

import android.annotation.SuppressLint;

/**
 * Item for SRS stage headers.
 */
public final class EventHeaderItem extends HeaderItem {
    private final String title;

    /**
     * The constructor.
     *
     * @param tag the tag for this header
     * @param collapsed true if this header is initially collapsed
     * @param title the title for this header
     * @param events the events for this header
     */
    @SuppressLint("NewApi")
    public EventHeaderItem(final String tag, final boolean collapsed, final String title, final Iterable<EventItem> events) {
        super(tag, collapsed);
        this.title = title;
        events.forEach(this::addItem);
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public String getTitleText() {
        return title;
    }

    @Override
    public String getDetailsText() {
        if (items.size() == 1) {
            return "1 event";
        }
        return items.size() + " events";
    }
}
