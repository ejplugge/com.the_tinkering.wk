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

import java.util.Collection;
import java.util.Comparator;

/**
 * The items in a session that should be given priority over the rest.
 */
@SuppressWarnings({"BoundedWildcard", "unused"})
public enum SessionPriority {
    /**
     * None, all are treated equally.
     */
    NONE() {
        @Override
        public Comparator<Subject> getComparator(final Comparator<Subject> base,
                                                 final Collection<Long> levelUpIds,
                                                 final int userLevel, final int maxLevel) {
            return base;
        }
    },

    /**
     * Radicals before others.
     */
    RADICALS_FIRST() {
        @Override
        public Comparator<Subject> getComparator(final Comparator<Subject> base,
                                                 final Collection<Long> levelUpIds,
                                                 final int userLevel, final int maxLevel) {
            return (o1, o2) -> {
                if (o1.getType().isRadical() && !o2.getType().isRadical()) {
                    return -1;
                }
                if (o2.getType().isRadical() && !o1.getType().isRadical()) {
                    return 1;
                }
                return base.compare(o1, o2);
            };
        }
    },

    /**
     * Level-up progression path items first, i.e. current-level kanji and radicals that
     * keep those kanji locked, but only ones not passed yet.
     */
    LEVEL_UP_FIRST() {
        @Override
        public Comparator<Subject> getComparator(final Comparator<Subject> base,
                                                 final Collection<Long> levelUpIds,
                                                 final int userLevel, final int maxLevel) {
            return (o1, o2) -> {
                if (userLevel < maxLevel) {
                    final boolean b1 = !o1.isPassed() && levelUpIds.contains(o1.getId());
                    final boolean b2 = !o2.isPassed() && levelUpIds.contains(o2.getId());
                    if (b1 && !b2) {
                        return -1;
                    }
                    if (b2 && !b1) {
                        return 1;
                    }
                }
                return base.compare(o1, o2);
            };
        }
    },

    /**
     * Current-level radicals and kanji first.
     */
    CURRENT_LEVEL_RADICAL_KANJI_FIRST() {
        @Override
        public Comparator<Subject> getComparator(final Comparator<Subject> base,
                                                 final Collection<Long> levelUpIds,
                                                 final int userLevel, final int maxLevel) {
            return (o1, o2) -> {
                final boolean b1 = o1.getLevel() == userLevel && !o1.getType().isVocabulary();
                final boolean b2 = o2.getLevel() == userLevel && !o2.getType().isVocabulary();
                if (b1 && !b2) {
                    return -1;
                }
                if (b2 && !b1) {
                    return 1;
                }
                return base.compare(o1, o2);
            };
        }
    },

    /**
     * Current-level items first.
     */
    CURRENT_LEVEL_FIRST() {
        @Override
        public Comparator<Subject> getComparator(final Comparator<Subject> base,
                                                 final Collection<Long> levelUpIds,
                                                 final int userLevel, final int maxLevel) {
            return (o1, o2) -> {
                if (o1.getLevel() == userLevel && o2.getLevel() != userLevel) {
                    return -1;
                }
                if (o2.getLevel() == userLevel && o1.getLevel() != userLevel) {
                    return 1;
                }
                return base.compare(o1, o2);
            };
        }
    };

    /**
     * Create a comparator that implements the rules for this enum value.
     *
     * @param base the base comparator to delegate to if both argument subjects are equal
     * @param levelUpIds the IDs of subjects that are on the level-up progression path
     * @param userLevel the user's level
     * @param maxLevel the max level granted by the user's subscription
     * @return the comparator
     */
    public abstract Comparator<Subject> getComparator(Comparator<Subject> base,
                                                      Collection<Long> levelUpIds,
                                                      int userLevel, int maxLevel);
}
