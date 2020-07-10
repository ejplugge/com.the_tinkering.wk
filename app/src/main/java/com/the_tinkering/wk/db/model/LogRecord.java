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

package com.the_tinkering.wk.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Room entity for the log_record table. This is where debug log records are stored.
 * Each row also contains the length of its message, to make it easier to trim excess
 * entries.
 */
@Entity(tableName = "log_record")
public final class LogRecord {
    @PrimaryKey(autoGenerate = true) private long id = 0L;
    private @Nullable Date timestamp;
    private @Nullable String tag;
    private int length = 0;
    private @Nullable String message;

    /**
     * The unique ID.
     * @return the value
     */
    public long getId() {
        return id;
    }

    /**
     * The unique ID.
     * @param id the value
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Timestamp when the event was generated.
     * @return the value
     */
    public @Nullable Date getTimestamp() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return timestamp;
    }

    /**
     * Timestamp when the event was generated.
     * @param timestamp the value
     */
    public void setTimestamp(final @Nullable Date timestamp) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.timestamp = timestamp;
    }

    /**
     * The tag (class name) for this record.
     * @return the value
     */
    public @Nullable String getTag() {
        return tag;
    }

    /**
     * The tag (class name) for this record.
     * @param tag the value
     */
    public void setTag(final @Nullable String tag) {
        this.tag = tag;
    }

    /**
     * The length of the message string.
     * @return the value
     */
    public int getLength() {
        return length;
    }

    /**
     * The length of the message string.
     * @param length the value
     */
    public void setLength(final int length) {
        this.length = length;
    }

    /**
     * The log message.
     * @return the value
     */
    public @Nullable String getMessage() {
        return message;
    }

    /**
     * The log message.
     * @param message the value
     */
    public void setMessage(final @Nullable String message) {
        this.message = message;
    }
}
