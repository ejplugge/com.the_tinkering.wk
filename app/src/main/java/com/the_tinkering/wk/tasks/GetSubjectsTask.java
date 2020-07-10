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

package com.the_tinkering.wk.tasks;

import androidx.core.util.Consumer;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.api.ApiState;
import com.the_tinkering.wk.api.model.ApiSubject;
import com.the_tinkering.wk.api.model.Reading;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.livedata.LiveApiProgress;
import com.the_tinkering.wk.livedata.LiveApiState;
import com.the_tinkering.wk.livedata.LiveBurnedItems;
import com.the_tinkering.wk.livedata.LiveCriticalCondition;
import com.the_tinkering.wk.livedata.LiveJlptProgress;
import com.the_tinkering.wk.livedata.LiveJoyoProgress;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.livedata.LiveLevelProgress;
import com.the_tinkering.wk.livedata.LiveRecentUnlocks;
import com.the_tinkering.wk.livedata.LiveTimeLine;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.HOUR;

/**
 * Task to fetch any subjects that have been updated since the last time this task was run.
 */
public final class GetSubjectsTask extends ApiTask {
    /**
     * Task priority.
     */
    public static final int PRIORITY = 20;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetSubjectsTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    public boolean canRun() {
        return getOnlineStatus().canCallApi() && ApiState.getCurrentApiState() == ApiState.OK;
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final @Nullable Date lastGetSubjectsSuccess = db.propertiesDao().getLastSubjectSyncSuccessDate(HOUR);

        LiveApiProgress.reset(true, "subjects");

        String uri = "/v2/subjects";
        if (lastGetSubjectsSuccess != null) {
            uri += "?updated_after=" + Converters.formatDate(lastGetSubjectsSuccess);
        }

        final Set<Long> existingSubjectIds = db.subjectViewsDao().getAllSubjectIds();

        if (!collectionApiCall(uri, ApiSubject.class, new Consumer<ApiSubject>() {
            @Override
            public void accept(final ApiSubject t) {
                if (!t.getReadings().isEmpty()) {
                    int i = 0;
                    while (i < t.getReadings().size()) {
                        final Reading reading = t.getReadings().get(i);
                        if (reading.isEmptyOrNone()) {
                            t.getReadings().remove(i);
                            continue;
                        }
                        i++;
                    }
                }
                db.subjectSyncDao().insertOrUpdate(t, existingSubjectIds);
            }
        })) {
            return;
        }

        db.propertiesDao().setLastApiSuccessDate(new Date(System.currentTimeMillis()));
        db.propertiesDao().setLastSubjectSyncSuccessDate(new Date(System.currentTimeMillis()));
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveApiState.getInstance().forceUpdate();
        if (LiveApiProgress.getNumProcessedEntities() > 0) {
            db.propertiesDao().setLastAudioScanDate(null);
            LiveTimeLine.getInstance().update();
            LiveLevelProgress.getInstance().update();
            LiveJoyoProgress.getInstance().update();
            LiveJlptProgress.getInstance().update();
            LiveRecentUnlocks.getInstance().update();
            LiveCriticalCondition.getInstance().update();
            LiveBurnedItems.getInstance().update();
            LiveLevelDuration.getInstance().forceUpdate();
        }
    }
}
