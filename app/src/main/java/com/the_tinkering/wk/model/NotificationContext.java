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

package com.the_tinkering.wk.model;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * The context of information needed for notifications.
 */
public final class NotificationContext {
    private int numLessons = 0;
    private int numReviews = 0;
    private int numNewReviews = 0;
    private @Nullable Date newestAvailableAt;

    /**
     * Number of lessons currently available.
     * @return the value
     */
    public int getNumLessons() {
        return numLessons;
    }

    /**
     * Number of lessons currently available.
     * @param numLessons the value
     */
    public void setNumLessons(final int numLessons) {
        this.numLessons = numLessons;
    }

    /**
     * Number of reviews currently available.
     * @return the value
     */
    public int getNumReviews() {
        return numReviews;
    }

    /**
     * Number of reviews currently available.
     * @param numReviews the value
     */
    public void setNumReviews(final int numReviews) {
        this.numReviews = numReviews;
    }

    /**
     * Number of reviews that have come up since the last notification.
     * @return the value
     */
    public int getNumNewReviews() {
        return numNewReviews;
    }

    /**
     * Number of reviews that have come up since the last notification.
     * @param numNewReviews the value
     */
    public void setNumNewReviews(final int numNewReviews) {
        this.numNewReviews = numNewReviews;
    }

    /**
     * The date when the newest available review became available.
     * @return the value
     */
    public @Nullable Date getNewestAvailableAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return newestAvailableAt;
    }

    /**
     * The date when the newest available review became available.
     * @param newestAvailableAt the value
     */
    public void setNewestAvailableAt(final @Nullable Date newestAvailableAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.newestAvailableAt = newestAvailableAt;
    }
}
