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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for the level_progression table. This records the coarse-grained progression in levels.
 */
@Entity(tableName = "level_progression")
public final class LevelProgression {
    /**
     * The unique ID.
     */
    @PrimaryKey public long id = 0L;

    /**
     * Timestamp when this level was abandoned (because of a reset), or null if not abandoned.
     */
    @ColumnInfo(defaultValue = "0")
    public long abandonedAt = 0L;

    /**
     * Timestamp when this level was completed (all subjects burned), or null if not completed.
     */
    @ColumnInfo(defaultValue = "0")
    public long completedAt = 0L;

    /**
     * Timestamp when this record was created.
     */
    @ColumnInfo(defaultValue = "0")
    public long createdAt = 0L;

    /**
     * Timestamp when this level was passed (all subjects passed), or null if not passed.
     */
    @ColumnInfo(defaultValue = "0")
    public long passedAt = 0L;

    /**
     * Timestamp when this level was started, or null if not started.
     */
    @ColumnInfo(defaultValue = "0")
    public long startedAt = 0L;

    /**
     * Timestamp when this level was unlocked, or null if not unlocked.
     */
    @ColumnInfo(defaultValue = "0")
    public long unlockedAt = 0L;

    /**
     * The level this record applies to.
     */
    @ColumnInfo(defaultValue = "0")
    public int level = 0;

    /**
     * Get the timestamp since when the user reached this level.
     * Based on unlockedAt, with startedAt as fallback.
     *
     * @return the date
     */
    @Ignore
    public long getSince() {
        if (unlockedAt != 0) {
            return unlockedAt;
        }
        if (startedAt != 0) {
            return startedAt;
        }
        return 0;
    }
}
