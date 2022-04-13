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

package com.the_tinkering.wk.enums;

/**
 * The layout style for subject cards.
 */
@SuppressWarnings("unused")
public enum SubjectCardLayout {
    /**
     * Normal linear layout.
     */
    NORMAL,

    /**
     * Compact, with progression information in the top right corner.
     */
    COMPACT,

    /**
     * Compact, without progression information.
     */
    COMPACT_NO_PROGRESSION
}
