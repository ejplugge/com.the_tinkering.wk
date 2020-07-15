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

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.api.ApiState;
import com.the_tinkering.wk.api.model.ApiReviewStatistic;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.livedata.LiveApiProgress;
import com.the_tinkering.wk.livedata.LiveApiState;
import com.the_tinkering.wk.livedata.LiveCriticalCondition;

import java.util.Date;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.HOUR;

/**
 * Task to fetch any review statistics that have been updated since the last time this task was run.
 */
public final class GetReviewStatisticsTask extends ApiTask {
    /**
     * Task priority.
     */
    public static final int PRIORITY = 22;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetReviewStatisticsTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    public boolean canRun() {
        return getOnlineStatus().canCallApi() && ApiState.getCurrentApiState() == ApiState.OK;
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final @Nullable Date lastGetReviewStatisticsSuccess = db.propertiesDao().getLastReviewStatisticSyncSuccessDate(HOUR);

        LiveApiProgress.reset(true, "statistics");

        String uri = "/v2/review_statistics";
        if (lastGetReviewStatisticsSuccess != null) {
            uri += "?updated_after=" + Converters.formatDate(lastGetReviewStatisticsSuccess);
        }

        if (!collectionApiCall(uri, ApiReviewStatistic.class, t -> db.subjectSyncDao().insertOrUpdateReviewStatistic(t))) {
            return;
        }

        db.propertiesDao().setLastApiSuccessDate(new Date(System.currentTimeMillis()));
        db.propertiesDao().setLastReviewStatisticSyncSuccessDate(new Date(System.currentTimeMillis()));
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveApiState.getInstance().forceUpdate();
        if (LiveApiProgress.getNumProcessedEntities() > 0) {
            LiveCriticalCondition.getInstance().update();
        }
    }
}
