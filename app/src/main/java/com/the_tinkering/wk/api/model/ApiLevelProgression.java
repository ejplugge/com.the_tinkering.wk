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

package com.the_tinkering.wk.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.the_tinkering.wk.components.WaniKaniApiDateDeserializer;
import com.the_tinkering.wk.components.WaniKaniApiDateSerializer;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Model class for level progression records as reported by the API.
 */
@SuppressWarnings("unused")
public final class ApiLevelProgression implements WaniKaniEntity {
    private long id = 0;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("abandoned_at") private @Nullable Date abandonedAt;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("completed_at") private @Nullable Date completedAt;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("created_at") private @Nullable Date createdAt;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("passed_at") private @Nullable Date passedAt;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("started_at") private @Nullable Date startedAt;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("unlocked_at") private @Nullable Date unlockedAt;
    @JsonProperty("level") private int level = 0;

    /**
     * Unique ID for this record.
     * @return the value
     */
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public void setObject(final @Nullable String object) {
        //
    }

    /**
     * Timestamp when this level was abandoned (because of a reset), or null if not abandoned.
     * @return the value
     */
    public @Nullable Date getAbandonedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return abandonedAt;
    }

    /**
     * Timestamp when this level was abandoned (because of a reset), or null if not abandoned.
     * @param abandonedAt the value
     */
    public void setAbandonedAt(final @Nullable Date abandonedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.abandonedAt = abandonedAt;
    }

    /**
     * Timestamp when this level was completed (all subjects burned), or null if not completed.
     * @return the value
     */
    public @Nullable Date getCompletedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return completedAt;
    }

    /**
     * Timestamp when this level was completed (all subjects burned), or null if not completed.
     * @param completedAt the value
     */
    public void setCompletedAt(final @Nullable Date completedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.completedAt = completedAt;
    }

    /**
     * Timestamp when this record was created.
     * @return the value
     */
    public @Nullable Date getCreatedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return createdAt;
    }

    /**
     * Timestamp when this record was created.
     * @param createdAt the value
     */
    public void setCreatedAt(final @Nullable Date createdAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.createdAt = createdAt;
    }

    /**
     * Timestamp when this level was passed (all subjects passed), or null if not passed.
     * @return the value
     */
    public @Nullable Date getPassedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return passedAt;
    }

    /**
     * Timestamp when this level was passed (all subjects passed), or null if not passed.
     * @param passedAt the value
     */
    public void setPassedAt(final @Nullable Date passedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.passedAt = passedAt;
    }

    /**
     * Timestamp when this level was started, or null if not started.
     * @return the value
     */
    public @Nullable Date getStartedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return startedAt;
    }

    /**
     * Timestamp when this level was started, or null if not started.
     * @param startedAt the value
     */
    public void setStartedAt(final @Nullable Date startedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.startedAt = startedAt;
    }

    /**
     * Timestamp when this level was unlocked, or null if not unlocked.
     * @return the value
     */
    public @Nullable Date getUnlockedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return unlockedAt;
    }

    /**
     * Timestamp when this level was unlocked, or null if not unlocked.
     * @param unlockedAt the value
     */
    public void setUnlockedAt(final @Nullable Date unlockedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.unlockedAt = unlockedAt;
    }

    /**
     * The level this record applies to.
     * @return the value
     */
    public int getLevel() {
        return level;
    }

    /**
     * The level this record applies to.
     * @param level the value
     */
    public void setLevel(final int level) {
        this.level = level;
    }
}
