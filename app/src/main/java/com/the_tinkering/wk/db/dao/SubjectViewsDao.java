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

package com.the_tinkering.wk.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.the_tinkering.wk.db.model.SubjectPronunciationAudio;
import com.the_tinkering.wk.model.LevelProgressItem;
import com.the_tinkering.wk.model.SrsBreakDownItem;
import com.the_tinkering.wk.model.SubjectReferenceData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO for subjects.
 */
@Dao
public abstract class SubjectViewsDao {
    /**
     * Room-generated method: get a list of subject IDs for which the assignment has been locally patched
     * but not yet updated from remote. This is normally not necessary but is a last-resort option to
     * recover from sync problems.
     *
     * @return the list of subject IDs, capped at 100
     */
    @Query("SELECT id FROM subject WHERE assignmentPatched LIMIT 100")
    public abstract List<Long> getPatchedAssignments();

    /**
     * Room-generated method: get a list of subject IDs for which the statistics have been locally patched
     * but not yet updated from remote. This is normally not necessary but is a last-resort option to
     * recover from sync problems.
     *
     * @return the list of subject IDs, capped at 100
     */
    @Query("SELECT id FROM subject WHERE statisticPatched LIMIT 100")
    public abstract List<Long> getPatchedReviewStatistics();

    /**
     * Room-generated method: get a list of subject IDs for which the study materials have been locally patched
     * but not yet updated from remote. This is normally not necessary but is a last-resort option to
     * recover from sync problems.
     *
     * @return the list of subject IDs, capped at 100
     */
    @Query("SELECT id FROM subject WHERE studyMaterialPatched LIMIT 100")
    public abstract List<Long> getPatchedStudyMaterials();

    /**
     * Room-generated method: get summary records describing the SRS stages and the number of subjects in each stage.
     *
     * @param userLevel the user's level
     * @return the list of overview items
     */
    @Query("SELECT srsSystemId AS systemId, srsStage AS stageId, COUNT(id) AS count FROM subject WHERE "
            + "hiddenAt = 0 AND object IS NOT NULL AND level <= :userLevel "
            + "GROUP BY srsSystemId, srsStage")
    public abstract List<SrsBreakDownItem> getSrsBreakDownItems(int userLevel);

    /**
     * Room-generated method: get the number of locked subjects.
     * Note: unlocked items that have been moved to a level greater than the user's level
     * are also counted as locked by this method, and they are excluded from the previous
     * two methods.
     *
     * @param userLevel the user's level
     * @return the number
     */
    @Query("SELECT COUNT(id) AS count FROM subject WHERE "
            + "hiddenAt = 0 AND object IS NOT NULL "
            + "AND level > :userLevel")
    public abstract int getSrsBreakDownOverLevel(int userLevel);

    /**
     * Room-generated method: get summary records describing the number of subjects per level/type pair.
     *
     * @param userLevel the user's level
     * @return the list of overview items
     */
    @Query("SELECT level, object AS type, COUNT(id) AS count FROM subject"
            + " WHERE subject.hiddenAt = 0 AND object IS NOT NULL"
            + " AND level <= :userLevel"
            + " GROUP BY level, object")
    public abstract List<LevelProgressItem> getLevelProgressTotalItems(int userLevel);

    /**
     * Room-generated method: get summary records describing the number of passed subjects per level/type pair.
     *
     * @param userLevel the user's level
     * @return the list of overview items
     */
    @Query("SELECT level, object AS type, COUNT(id) AS count FROM subject"
            + " WHERE subject.hiddenAt = 0 AND object IS NOT NULL"
            + " AND level <= :userLevel AND passedAt != 0"
            + " GROUP BY level, object")
    public abstract List<LevelProgressItem> getLevelProgressPassedItems(int userLevel);

    /**
     * Room-generated method: get a list of all subject IDs in the database.
     *
     * @return the list
     */
    @Query("SELECT id FROM subject")
    protected abstract List<Long> getAllSubjectIdsAsList();

    /**
     * Get a set of subject IDs for all subjects in the database.
     *
     * @return the set
     */
    public final Set<Long> getAllSubjectIds() {
        return new HashSet<>(getAllSubjectIdsAsList());
    }

    /**
     * Room-generated method: get the reference data for all subjects.
     *
     * @return the list of reference data
     */
    @Query("SELECT id, object AS type, characters, frequency, joyoGrade, jlptLevel, pitchInfo, strokeData FROM subject")
    public abstract List<SubjectReferenceData> getReferenceData();

    /**
     * Room-generated method: get the pronunciation audio for all subjects in a level.
     *
     * @param level the level
     * @return the list of audio records
     */
    @Query("SELECT id, level, pronunciationAudios FROM subject WHERE hiddenAt = 0 AND level = :level")
    public abstract List<SubjectPronunciationAudio> getAudioByLevel(int level);
}
