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

import static com.the_tinkering.wk.util.ObjectSupport.compareIntegers;

/**
 * The order to present reviews/self-study in. Each value will produce a comparator on demand.
 */
@SuppressWarnings("unused")
public enum ReviewOrder {
    /**
     * Completely random order. I.e., the comparator always returns 0 and doesn't impose an order.
     */
    SHUFFLE() {
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
     * By level.
     */
    LEVEL() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return Integer.compare(o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * By type.
     */
    TYPE() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    },

    /**
     * By SRS stage.
     */
    SRS() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    return o1.getSrsStage().compareTo(o2.getSrsStage());
                }
            };
        }
    },

    /**
     * By level, then by type.
     */
    LEVEL_THEN_TYPE() {
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
     * By level, then by SRS stage.
     */
    LEVEL_THEN_SRS() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = Integer.compare(o1.getLevel(), o2.getLevel());
                    if (n != 0) {
                        return n;
                    }
                    return o1.getSrsStage().compareTo(o2.getSrsStage());
                }
            };
        }
    },

    /**
     * By type, then by level.
     */
    TYPE_THEN_LEVEL() {
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
     * By type, then by SRS stage.
     */
    TYPE_THEN_SRS() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
                    if (n != 0) {
                        return n;
                    }
                    return o1.getSrsStage().compareTo(o2.getSrsStage());
                }
            };
        }
    },

    /**
     * By SRS stage, then by type.
     */
    SRS_THEN_TYPE() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n != 0) {
                        return n;
                    }
                    return Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    },

    /**
     * By SRS stage, then by level.
     */
    SRS_THEN_LEVEL() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n != 0) {
                        return n;
                    }
                    return Integer.compare(o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * By level, then by type, then by SRS stage.
     */
    LEVEL_THEN_TYPE_THEN_SRS() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = compareIntegers(o1.getLevel(), o2.getLevel(), o1.getType().getOrder(), o2.getType().getOrder());
                    if (n != 0) {
                        return n;
                    }
                    return o1.getSrsStage().compareTo(o2.getSrsStage());
                }
            };
        }
    },

    /**
     * By level, then by SRS stage, then by type.
     */
    LEVEL_THEN_SRS_THEN_TYPE() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n1 = Integer.compare(o1.getLevel(), o2.getLevel());
                    if (n1 != 0) {
                        return n1;
                    }
                    final int n2 = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n2 != 0) {
                        return n2;
                    }
                    return Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    },

    /**
     * By type, then by level, then by SRS stage.
     */
    TYPE_THEN_LEVEL_THEN_SRS() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = compareIntegers(o1.getType().getOrder(), o2.getType().getOrder(), o1.getLevel(), o2.getLevel());
                    if (n != 0) {
                        return n;
                    }
                    return o1.getSrsStage().compareTo(o2.getSrsStage());
                }
            };
        }
    },

    /**
     * By type, then by SRS stage, then by level.
     */
    TYPE_THEN_SRS_THEN_LEVEL() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n1 = Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
                    if (n1 != 0) {
                        return n1;
                    }
                    final int n2 = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n2 != 0) {
                        return n2;
                    }
                    return Integer.compare(o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * By SRS stage, then by type, then by level.
     */
    SRS_THEN_TYPE_THEN_LEVEL() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n != 0) {
                        return n;
                    }
                    return compareIntegers(o1.getType().getOrder(), o2.getType().getOrder(), o1.getLevel(), o2.getLevel());
                }
            };
        }
    },

    /**
     * By SRS stage, then by level, then by type.
     */
    SRS_THEN_LEVEL_THEN_TYPE() {
        @Override
        public Comparator<Subject> getComparator() {
            return new Comparator<Subject>() {
                @Override
                public int compare(final Subject o1, final Subject o2) {
                    final int n = o1.getSrsStage().compareTo(o2.getSrsStage());
                    if (n != 0) {
                        return n;
                    }
                    return compareIntegers(o1.getLevel(), o2.getLevel(), o1.getType().getOrder(), o2.getType().getOrder());
                }
            };
        }
    };

    /**
     * The comparator to compare two subjects according to this order.
     *
     * @return the comparator
     */
    public abstract Comparator<Subject> getComparator();
}
