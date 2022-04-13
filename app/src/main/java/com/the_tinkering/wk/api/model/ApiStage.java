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

package com.the_tinkering.wk.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

/**
 * Model class representing an SRS stage in the API.
 */
@SuppressWarnings("CanBeFinal")
public final class ApiStage {
    /**
     * The ID (position) of this stage within its system.
     */
    @JsonProperty("position") public long position = 0;

    /**
     * The interval for this stage.
     */
    @JsonProperty("interval") public long interval = 0;

    /**
     * The time unit for this stage's interval.
     */
    @JsonProperty("interval_unit") public @Nullable String intervalUnit = null;
}
