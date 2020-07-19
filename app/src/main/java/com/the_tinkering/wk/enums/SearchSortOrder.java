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

import android.annotation.SuppressLint;

import com.the_tinkering.wk.adapter.search.AvailableAtHeaderItem;
import com.the_tinkering.wk.adapter.search.HeaderItem;
import com.the_tinkering.wk.adapter.search.ItemTypeHeaderItem;
import com.the_tinkering.wk.adapter.search.LevelHeaderItem;
import com.the_tinkering.wk.adapter.search.SrsStageHeaderItem;
import com.the_tinkering.wk.db.model.Subject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Sort order for advanced search.
 */
@SuppressWarnings("JavaDoc")
@SuppressLint("NewApi")
public enum SearchSortOrder {
    TYPE("Type", true) {
        @Override
        public Comparator<Subject> getComparator(final long searchTime) {
            return Comparator.comparingInt(Subject::getTypeOrder);
        }

        @Override
        public String getTopLevelTag(final Subject subject, final long searchTime) {
            return subject.getType().name();
        }

        @Override
        public HeaderItem createTopLevelHeaderItem(final Subject subject, final long searchTime) {
            return createSubLevelHeaderItem(null, subject);
        }
    },

    LEVEL_TYPE("Level, Type", false) {
        @Override
        public Comparator<Subject> getComparator(final long searchTime) {
            return Comparator.comparingInt(Subject::getLevel).thenComparingInt(Subject::getTypeOrder);
        }

        @Override
        public String getTopLevelTag(final Subject subject, final long searchTime) {
            return Integer.toString(subject.getLevel());
        }

        @Override
        public HeaderItem createTopLevelHeaderItem(final Subject subject, final long searchTime) {
            return new LevelHeaderItem(subject.getLevel());
        }
    },

    AVAILABLE_AT_TYPE("Next review, Type", false) {
        @Override
        public Comparator<Subject> getComparator(final long searchTime) {
            return Comparator.<Subject>comparingLong(subject -> {
                final long date = subject.getAvailableAt();
                return date == 0 ? 0 : Math.max(date, searchTime);
            }).thenComparingInt(Subject::getTypeOrder);
        }

        @Override
        public String getTopLevelTag(final Subject subject, final long searchTime) {
            final long date = subject.getAvailableAt();
            if (date == 0) {
                return "0";
            }
            else if (date <= searchTime) {
                return Long.toString(searchTime);
            }
            return Long.toString(date);
        }

        @Override
        public HeaderItem createTopLevelHeaderItem(final Subject subject, final long searchTime) {
            final long date = subject.getAvailableAt();
            if (date == 0) {
                return new AvailableAtHeaderItem(0);
            }
            else if (date <= searchTime) {
                return new AvailableAtHeaderItem(searchTime);
            }
            return new AvailableAtHeaderItem(date);
        }
    },

    STAGE_TYPE("SRS Stage, Type", false) {
        @Override
        public Comparator<Subject> getComparator(final long searchTime) {
            return Comparator.comparing(Subject::getSrsStage).thenComparingInt(Subject::getTypeOrder);
        }

        @Override
        public String getTopLevelTag(final Subject subject, final long searchTime) {
            return subject.getSrsStage().getAdvancedSearchTag();
        }

        @Override
        public HeaderItem createTopLevelHeaderItem(final Subject subject, final long searchTime) {
            return new SrsStageHeaderItem(subject.getSrsStage());
        }
    };

    private final String description;
    private final boolean singleLevel;

    SearchSortOrder(final String description, final boolean singleLevel) {
        this.description = description;
        this.singleLevel = singleLevel;
    }

    /**
     * Find an instance by its description.
     *
     * @param description the description to look for
     * @param defaultValue the default value if not found
     * @return the order or defaultValue
     */
    public static SearchSortOrder forDescription(final String description, final SearchSortOrder defaultValue) {
        for (final SearchSortOrder value: values()) {
            if (value.description.equals(description)) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * Get the list of descriptions for the values of this enum.
     *
     * @return the list of descriptions
     */
    public static List<String> descriptionValues() {
        final List<String> result = new ArrayList<>();
        for (final SearchSortOrder value: values()) {
            result.add(value.description);
        }
        return result;
    }

    /**
     * Does this order contain only a single header level?.
     *
     * @return true if it does
     */
    public boolean isSingleLevel() {
        return singleLevel;
    }

    /**
     * Create a sub-level header item for this order.
     *
     * @param parentTag tag of the parent header, or null if this is a single level order
     * @param subject the subject
     * @return the item
     */
    public static HeaderItem createSubLevelHeaderItem(final @Nullable String parentTag, final Subject subject) {
        return new ItemTypeHeaderItem(parentTag, subject.getType());
    }

    /**
     * The comparator that implements this sort order.
     *
     * @param searchTime the timestamp the search was started
     * @return the comparator
     */
    public abstract Comparator<Subject> getComparator(long searchTime);

    /**
     * Get the header tag for a top level item corresponding to the given subject.
     * @param subject the subject
     * @param searchTime the timestamp the search was started
     * @return the tag
     */
    public abstract String getTopLevelTag(Subject subject, long searchTime);

    /**
     * Create a top-level header item for this order.
     *
     * @param subject the subject
     * @param searchTime the timestamp the search was started
     * @return the item
     */
    public abstract HeaderItem createTopLevelHeaderItem(Subject subject, long searchTime);
}
