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

package com.the_tinkering.wk.livedata;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.model.LevelProgress;
import com.the_tinkering.wk.model.LevelProgressItem;

/**
 * LiveData that tracks the data for the level progression bars on the dashboard.
 */
public final class LiveLevelProgress extends ConservativeLiveData<LevelProgress> {
    /**
     * The singleton instance.
     */
    private static final LiveLevelProgress instance = new LiveLevelProgress();

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveLevelProgress getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private LiveLevelProgress() {
        //
    }

    @Override
    protected void updateLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final int userLevel = db.propertiesDao().getUserLevel();
        final LevelProgress levelProgress = new LevelProgress(userLevel);

        for (final LevelProgressItem item: db.subjectViewsDao().getLevelProgressTotalItems(userLevel)) {
            levelProgress.setTotalCount(item);
        }

        for (final LevelProgressItem item: db.subjectViewsDao().getLevelProgressPassedItems(userLevel)) {
            levelProgress.setNumPassed(item);
        }

        levelProgress.removePassedBars();

        for (final LevelProgress.BarEntry entry: levelProgress.getEntries()) {
            for (final Subject subject: db.subjectCollectionsDao().getLevelProgressSubjects(entry.getLevel(), entry.getType())) {
                entry.addSubject(subject);
            }
        }

        instance.postValue(levelProgress);
    }

    @Override
    public LevelProgress getDefaultValue() {
        return new LevelProgress(0);
    }
}
