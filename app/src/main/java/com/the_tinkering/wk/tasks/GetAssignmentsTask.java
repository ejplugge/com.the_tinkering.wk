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
import com.the_tinkering.wk.api.model.ApiAssignment;
import com.the_tinkering.wk.db.AppDatabase;
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
import com.the_tinkering.wk.livedata.LiveSrsBreakDown;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.services.BackgroundAlarmReceiver;

import static com.the_tinkering.wk.Constants.HOUR;
import static com.the_tinkering.wk.util.TextUtil.formatTimestampForApi;

/**
 * Task to fetch any assignments that have been updated since the last time this task was run.
 */
public final class GetAssignmentsTask extends ApiTask {
    /**
     * Task priority.
     */
    public static final int PRIORITY = 21;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetAssignmentsTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    public boolean canRun() {
        return WkApplication.getInstance().getOnlineStatus().canCallApi() && ApiState.getCurrentApiState() == ApiState.OK;
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final long lastGetAssignmentsSuccess = db.propertiesDao().getLastAssignmentSyncSuccessDate(HOUR);

        LiveApiProgress.reset(true, "assignments");

        String uri = "/v2/assignments";
        if (lastGetAssignmentsSuccess != 0) {
            uri += "?updated_after=" + formatTimestampForApi(lastGetAssignmentsSuccess);
        }

        if (!collectionApiCall(uri, ApiAssignment.class, t -> db.subjectSyncDao().insertOrUpdateAssignment(t))) {
            return;
        }

        db.propertiesDao().setSyncReminder(false);
        db.propertiesDao().setLastApiSuccessDate(System.currentTimeMillis());
        db.propertiesDao().setLastAssignmentSyncSuccessDate(System.currentTimeMillis());
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveApiState.getInstance().forceUpdate();
        if (LiveApiProgress.getNumProcessedEntities() > 0) {
            LiveTimeLine.getInstance().update();
            LiveSrsBreakDown.getInstance().update();
            LiveLevelProgress.getInstance().update();
            LiveJoyoProgress.getInstance().update();
            LiveJlptProgress.getInstance().update();
            LiveRecentUnlocks.getInstance().update();
            LiveCriticalCondition.getInstance().update();
            LiveBurnedItems.getInstance().update();
            LiveLevelDuration.getInstance().forceUpdate();
            BackgroundAlarmReceiver.processAlarm(null, true);
        }
    }
}
