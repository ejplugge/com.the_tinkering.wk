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

import com.the_tinkering.wk.db.model.Subject;

import java.util.Comparator;

import static com.the_tinkering.wk.util.ObjectSupport.compareBooleans;
import static com.the_tinkering.wk.util.ObjectSupport.compareIntegers;

/**
 * The order to present lessons in. Each value will produce a comparator on demand.
 */
@SuppressWarnings("unused")
public enum LessonOrder {
    /**
     * Completely random order. I.e., the comparator always returns 0 and doesn't impose an order.
     */
    SHUFFLE(true) {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return 0;
                }
            };
        }
    },

    /**
     * First by level, then by type.
     */
    LEVEL_THEN_TYPE(false) {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return compareIntegers(o1.getLevel(), o2.getLevel(), o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    },

    /**
     * First radicals, then for the rest first by level, then by type.
     */
    RADICALS_THEN_LEVEL_THEN_TYPE(false) {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = compareBooleans(!o1.getType().isRadical(), !o2.getType().isRadical());
                    if (n != 0) {
                        return n;
                    }
                    return compareIntegers(o1.getLevel(), o2.getLevel(), o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    },

    /**
     * First by type, then by level.
     */
    TYPE_THEN_LEVEL(false) {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return compareIntegers(o1.getType().getOrder(), o2.getType().getOrder(), o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * First by level, then shuffle.
     */
    LEVEL_THEN_SHUFFLE(true) {
        @Override
        public Comparator<Subject> getComparator () {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return compareIntegers(o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * First by type, then shuffle.
     */
    TYPE_THEN_SHUFFLE(true) {
        @Override
        public Comparator<Subject> getComparator () {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return compareIntegers(o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    };

    private final boolean shuffle;

    LessonOrder(final boolean shuffle) {
        this.shuffle = shuffle;
    }

    /**
     * Create a comparator to compare two subjects according to this order.
     *
     * @return the comparator
     */
    public abstract Comparator<Subject> getComparator();

    /**
     * Does this order require shuffling before applying the sorting order?.
     *
     * @return true if it does
     */
    public boolean isShuffle() {
        return shuffle;
    }
}
