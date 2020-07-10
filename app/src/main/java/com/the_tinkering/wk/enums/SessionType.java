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

package com.the_tinkering.wk.enums;

/**
 * The type of session.
 */
public enum SessionType {
    /**
     * None, the session isn't active.
     */
    NONE(false, false),

    /**
     * A lesson session.
     */
    LESSON(false, true),

    /**
     * A review session.
     */
    REVIEW(true, true),

    /**
     * A self-study session which is not reported back to the mothership.
     */
    SELF_STUDY(false, false);

    private final boolean srsRelevant;
    private final boolean reportingTaskNeeded;

    /**
     * The constructor.
     *
     * @param srsRelevant Are SRS stage changes relevant for this session type?.
     * @param reportingTaskNeeded When a session item's result is reported, does this session type also require a reporting task?.
     */
    SessionType(final boolean srsRelevant, final boolean reportingTaskNeeded) {
        this.srsRelevant = srsRelevant;
        this.reportingTaskNeeded = reportingTaskNeeded;
    }

    /**
     * Are SRS stage changes relevant for this session type?.
     * @return the value
     */
    public boolean isSrsRelevant() {
        return srsRelevant;
    }

    /**
     * When a session item's result is reported, does this session type also require a reporting task?.
     * @return the value
     */
    public boolean isReportingTaskNeeded() {
        return reportingTaskNeeded;
    }
}
