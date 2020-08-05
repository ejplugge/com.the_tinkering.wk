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
import androidx.room.PrimaryKey;

/**
 * Room entity for the audio_download_status table. This caches the status
 * of audio downloads, for the audio download page.
 */
@Entity(tableName = "audio_download_status")
public final class AudioDownloadStatus {
    /**
     * The level for this summary, also primary key.
     */
    @PrimaryKey
    public int level = 0;

    /**
     * The total number of subjects in this level.
     */
    @ColumnInfo(defaultValue = "0")
    public int numTotal = 0;

    /**
     * The number of subjects that have no audio.
     */
    @ColumnInfo(defaultValue = "0")
    public int numNoAudio = 0;

    /**
     * The number of subjects that have audio but none are available.
     */
    @ColumnInfo(defaultValue = "0")
    public int numMissingAudio = 0;

    /**
     * The number of subjects that have audio and some are available, but not all.
     */
    @ColumnInfo(defaultValue = "0")
    public int numPartialAudio = 0;

    /**
     * The number of subjects that have audio and all are available.
     */
    @ColumnInfo(defaultValue = "0")
    public int numFullAudio = 0;
}
