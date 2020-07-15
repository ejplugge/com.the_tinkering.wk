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
import com.the_tinkering.wk.services.SessionWidgetProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static com.the_tinkering.wk.util.ObjectSupport.orElse;

/**
 * Task to fetch assignments for a specific set of subjects that have had theirs
 * patched locally, but which haven't been updated from the API yet. This task is only
 * scheduled if the normal sync didn't resolve this already.
 */
public final class GetPatchedAssignmentsTask extends ApiTask {
    /**
     * Task priority.
     */
    public static final int PRIORITY = 21;

    private final String idList;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetPatchedAssignmentsTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
        idList = orElse(taskDefinition.getData(), "0");
    }

    @Override
    public boolean canRun() {
        return getOnlineStatus().canCallApi() && ApiState.getCurrentApiState() == ApiState.OK;
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();

        LiveApiProgress.reset(true, "assignments");

        final String uri = "/v2/assignments?subject_ids=" + idList;
        if (!collectionApiCall(uri, ApiAssignment.class, t -> db.subjectSyncDao().insertOrUpdateAssignment(t))) {
            return;
        }

        final Collection<Long> subjectIds = new ArrayList<>();
        for (final String s: idList.split(",")) {
            subjectIds.add(Long.parseLong(s, 10));
        }
        db.subjectDao().resolvePatchedAssignments(subjectIds);

        db.propertiesDao().setLastApiSuccessDate(new Date(System.currentTimeMillis()));
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
            SessionWidgetProvider.checkAndUpdateWidgets();
        }
    }
}
