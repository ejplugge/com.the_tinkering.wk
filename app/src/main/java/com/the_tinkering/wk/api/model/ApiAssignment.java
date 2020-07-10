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
 * Model class representing a user's assignment for a subject, as defined by the API.
 */
@SuppressWarnings("unused")
public final class ApiAssignment implements WaniKaniEntity {
    private long id = 0;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("available_at") private @Nullable Date availableAt = null;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("burned_at") private @Nullable Date burnedAt = null;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("passed_at") private @Nullable Date passedAt = null;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("resurrected_at") private @Nullable Date resurrectedAt = null;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("started_at") private @Nullable Date startedAt = null;
    @JsonSerialize(using = WaniKaniApiDateSerializer.class)
    @JsonDeserialize(using = WaniKaniApiDateDeserializer.class)
    @JsonProperty("unlocked_at") private @Nullable Date unlockedAt = null;
    @JsonProperty("srs_stage") private long srsStageId = 0;
    @JsonProperty("subject_id") private long subjectId = 0;

    /**
     * The WK ID of the entity.
     *
     * @return the ID.
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

    @Override
    public void setUpdatedAt(final @Nullable Date updatedAt) {
        //
    }

    /**
     * The timestamp when the next available review becomes available for this subject,
     * or null if no review is scheduled yet.
     * @return the value
     */
    public @Nullable Date getAvailableAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return availableAt;
    }

    /**
     * The timestamp when the next available review becomes available for this subject,
     * or null if no review is scheduled yet.
     * @param availableAt the value
     */
    public void setAvailableAt(final @Nullable Date availableAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.availableAt = availableAt;
    }

    /**
     * The timestamp when this subject was burned, or null if it hasn't been burned yet.
     * @return the value
     */
    public @Nullable Date getBurnedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return burnedAt;
    }

    /**
     * The timestamp when this subject was burned, or null if it hasn't been burned yet.
     * @param burnedAt the value
     */
    public void setBurnedAt(final @Nullable Date burnedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.burnedAt = burnedAt;
    }

    /**
     * The timestamp when this subject was passed, i.e. reached Guru I for the first time.
     * Note: for older assignments, this field is not filled in, but the passed boolean is
     * always reliable.
     * @return the value
     */
    public @Nullable Date getPassedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return passedAt;
    }

    /**
     * The timestamp when this subject was passed, i.e. reached Guru I for the first time.
     * Note: for older assignments, this field is not filled in, but the passed boolean is
     * always reliable.
     * @param passedAt the value
     */
    public void setPassedAt(final @Nullable Date passedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.passedAt = passedAt;
    }

    /**
     * The timestamp when this subject was resurrected from burned status, or null if it hasn't been resurrected.
     * @return the value
     */
    public @Nullable Date getResurrectedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return resurrectedAt;
    }

    /**
     * The timestamp when this subject was resurrected from burned status, or null if it hasn't been resurrected.
     * @param resurrectedAt the value
     */
    public void setResurrectedAt(final @Nullable Date resurrectedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.resurrectedAt = resurrectedAt;
    }

    /**
     * The timestamp when this subject was started, i.e. when the lesson for this subject was completed,
     * or null if it hasn't been started yet.
     * @return the value
     */
    public @Nullable Date getStartedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return startedAt;
    }

    /**
     * The timestamp when this subject was started, i.e. when the lesson for this subject was completed,
     * or null if it hasn't been started yet.
     * @param startedAt the value
     */
    public void setStartedAt(final @Nullable Date startedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.startedAt = startedAt;
    }

    /**
     * The timestamp when this subject was unlocked, or null if it is still locked.
     * @return the value
     */
    public @Nullable Date getUnlockedAt() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return unlockedAt;
    }

    /**
     * The timestamp when this subject was unlocked, or null if it is still locked.
     * @param unlockedAt the value
     */
    public void setUnlockedAt(final @Nullable Date unlockedAt) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.unlockedAt = unlockedAt;
    }

    /**
     * The current SRS stage for this subject, which is 0 for both locked and Initiate items.
     * @return the value
     */
    public long getSrsStageId() {
        return srsStageId;
    }

    /**
     * The current SRS stage for this subject, which is 0 for both locked and Initiate items.
     * @param srsStageId the value
     */
    public void setSrsStageId(final long srsStageId) {
        this.srsStageId = srsStageId;
    }

    /**
     * The subject ID this assignment is associated with.
     * @return the value
     */
    public long getSubjectId() {
        return subjectId;
    }

    /**
     * The subject ID this assignment is associated with.
     * @param subjectId the value
     */
    public void setSubjectId(final long subjectId) {
        this.subjectId = subjectId;
    }
}
