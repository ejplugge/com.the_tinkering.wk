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

import com.the_tinkering.wk.model.JlptProgressItem;
import com.the_tinkering.wk.model.JoyoProgressItem;
import com.the_tinkering.wk.model.NotificationContext;

import java.util.List;

/**
 * DAO for subjects.
 */
@Dao
public abstract class SubjectAggregatesDao {
    /**
     * Room-generated method: get the next timestamp when a review becomes available, after the given cutoff date.
     *
     * @param maxLevel the maximum level available on the user's subscription
     * @param userLevel the user's level
     * @param cutoff the cutoff date
     * @return the available timestamp or null if there are no long-term upcoming reviews
     */
    @Query("SELECT availableAt FROM subject"
            + " WHERE hiddenAt = 0 AND object IS NOT NULL"
            + " AND level <= :maxLevel AND level <= :userLevel"
            + " AND availableAt != 0 AND availableAt >= :cutoff"
            + " ORDER BY availableAt LIMIT 1")
    public abstract long getNextLongTermReviewDate(final int maxLevel, final int userLevel, final long cutoff);

    /**
     * Room-generated method: get the number of reviews that will become available at the specified time.
     *
     * @param maxLevel the maximum level available on the user's subscription
     * @param userLevel the user's level
     * @param targetDate the date for reviews to become available at
     * @return the count
     */
    @Query("SELECT COUNT(id) FROM subject"
            + " WHERE hiddenAt = 0 AND object IS NOT NULL"
            + " AND level <= :maxLevel AND level <= :userLevel"
            + " AND availableAt = :targetDate")
    public abstract int getNextLongTermReviewCount(final int maxLevel, final int userLevel, final long targetDate);

    /**
     * Room-generated method: get statistics for notifications.
     *
     * <ul>
     *     <li>Number of available lessons</li>
     *     <li>Number of available reviews</li>
     *     <li>Number of new reviews that became available after lastDate</li>
     *     <li>Timestamp of the newest available review</li>
     * </ul>
     *
     * @param maxLevel the maximum level available on the user's subscription
     * @param userLevel the user's level
     * @param lastDate the last availableAt date for a review reported as a notification
     * @param cutoff the current date
     * @return a POJO containing the results
     */
    @Query("SELECT numLessons, numReviews, numNewReviews, newestAvailableAt FROM "
            + "(SELECT COUNT(*) AS numLessons FROM subject WHERE hiddenAt=0 AND object IS NOT NULL "
            + "AND level <= :maxLevel AND level <= :userLevel AND unlockedAt!=0 AND startedAt=0 AND (resurrectedAt!=0 OR burnedAt=0)), "
            + "(SELECT COUNT(*) AS numReviews FROM subject WHERE hiddenAt=0 AND object IS NOT NULL "
            + "AND level <= :maxLevel AND level <= :userLevel AND availableAt!=0 AND availableAt < :cutoff), "
            + "(SELECT COUNT(*) AS numNewReviews FROM subject WHERE hiddenAt=0 AND object IS NOT NULL "
            + "AND level <= :maxLevel AND level <= :userLevel AND availableAt!=0 AND availableAt < :cutoff AND availableAt > :lastDate), "
            + "(SELECT MAX(availableAt) AS newestAvailableAt FROM subject WHERE hiddenAt=0 AND object IS NOT NULL "
            + "AND level <= :maxLevel AND level <= :userLevel AND availableAt!=0 AND availableAt < :cutoff)"
            + ";")
    public abstract NotificationContext getNotificationContext(int maxLevel, int userLevel, long lastDate, long cutoff);

    /**
     * Room-generated method: get the date the user reached a level by looking at the earliest unlockedAt date
     * for that level. This is only used as fallback if a level progression record is not available.
     *
     * @param level the level
     * @return the date or null if not reached yet
     */
    @Query("SELECT MIN(unlockedAt) FROM subject WHERE hiddenAt = 0 AND object IS NOT NULL AND unlockedAt != 0 AND level = :level")
    public abstract long getLevelReachedDate(final int level);

    /**
     * Room-generated method: get the highest level of any subject in the database.
     *
     * @return the highest level
     */
    @Query("SELECT MAX(level) FROM subject WHERE hiddenAt = 0 AND object IS NOT NULL")
    public abstract int getMaxLevel();

    /**
     * Room-generated method: get the JLPT progress detail.
     *
     * @return the list of items
     */
    @Query("SELECT srsSystemId, srsStage, jlptLevel, COUNT(id) AS count FROM subject WHERE (object = 'kanji') "
            + "AND jlptLevel > 0 GROUP BY srsSystemId, srsStage, jlptLevel")
    public abstract List<JlptProgressItem> getJlptProgress();

    /**
     * Room-generated method: get the Joyo progress detail.
     *
     * @return the list of items
     */
    @Query("SELECT srsSystemId, srsStage, joyoGrade, COUNT(id) AS count FROM subject WHERE (object = 'kanji') "
            + "AND joyoGrade > 0 GROUP BY srsSystemId, srsStage, joyoGrade")
    public abstract List<JoyoProgressItem> getJoyoProgress();
}
