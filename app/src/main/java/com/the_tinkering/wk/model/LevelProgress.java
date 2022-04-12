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

import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SubjectType;

import java.util.ArrayList;
import java.util.List;

/**
 * A model for the subject progress bar chart.
 *
 * <p>
 *     Each bar has an array of ints representing the size of the various parts of the bar.
 *     Each array has 10 elements:
 * </p>
 *
 * <ul>
 *     <li>0 - Passed</li>
 *     <li>1 - Apprentice IV</li>
 *     <li>2 - For future expansion</li>
 *     <li>3 - Apprentice III</li>
 *     <li>4 - For future expansion</li>
 *     <li>5 - Apprentice II</li>
 *     <li>6 - For future expansion</li>
 *     <li>7 - Apprentice I</li>
 *     <li>8 - Initiate (not started yet)</li>
 *     <li>9 - Locked</li>
 * </ul>
 */
public final class LevelProgress {
    private final List<BarEntry> entries = new ArrayList<>();

    /**
     * The constructor.
     *
     * @param userLevel the user's level
     */
    public LevelProgress(final int userLevel) {
        for (int i=1; i<=userLevel; i++) {
            for (final SubjectType type: SubjectType.values()) {
                entries.add(new BarEntry(i, type));
            }
        }
    }

    /**
     * All entries in the chart.
     * @return the value
     */
    public List<BarEntry> getEntries() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return entries;
    }

    /**
     * Set the total subject count for each bar in the overview from the aggregate database data supplied.
     *
     * @param item the count of subjects for a specific level/type combination
     */
    public void setTotalCount(final LevelProgressItem item) {
        for (final BarEntry entry: entries) {
            if (entry.level == item.getLevel() && entry.type == item.getType()) {
                entry.totalCount = item.getCount();
            }
        }
    }

    /**
     * Set the passed subject count for each bar in the overview from the aggregate database data supplied.
     *
     * @param item the count of subjects for a specific level/type combination
     */
    public void setNumPassed(final LevelProgressItem item) {
        for (final BarEntry entry: entries) {
            if (entry.level == item.getLevel() && entry.type == item.getType()) {
                entry.numPassed = item.getCount();
            }
        }
    }

    /**
     * Clean up the chart by removing bars where everything has already been passed.
     */
    public void removePassedBars() {
        int i = 0;
        while (i < entries.size()) {
            final BarEntry entry = entries.get(i);
            if (entry.totalCount == 0 || entry.numPassed >= entry.totalCount) {
                entries.remove(i);
            }
            else {
                i++;
            }
        }
    }

    /**
     * An entry in the chart for one bar.
     */
    public static final class BarEntry {
        private final int level;
        private final SubjectType type;
        private final int[] buckets = new int[10];
        private int numPassed = 0;
        private int totalCount = 0;

        /**
         * The constructor.
         *
         * @param level the level for this bar
         * @param type the subject type for this bar
         */
        BarEntry(final int level, final SubjectType type) {
            this.level = level;
            this.type = type;
        }

        /**
         * The level for this bar.
         * @return the value
         */
        public int getLevel() {
            return level;
        }

        /**
         * The subject type for this bar.
         * @return the value
         */
        public SubjectType getType() {
            return type;
        }

        /**
         * The bucket counts for this bar.
         * 0 = passed, 1-7 = Apprentice IV-I, 8 = initiate, 9 = locked.
         * @return the value
         */
        public int[] getBuckets() {
            //noinspection AssignmentOrReturnOfFieldWithMutableType
            return buckets;
        }

        /**
         * Add a subject to the relevant bucket.
         *
         * @param subject the subject to add
         */
        public void addSubject(final Subject subject) {
            if (subject.isLocked()) {
                buckets[9]++;
                return;
            }
            if (subject.isPassed()) {
                buckets[0]++;
                return;
            }
            final SrsSystem.Stage stage = subject.getSrsStage();
            buckets[stage.getLevelProgressBucket()]++;
        }
    }
}
