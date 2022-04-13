/*
 * Copyright 2019-2022 Ernst Jan Plugge <rmc@dds.nl>
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
import com.the_tinkering.wk.api.model.ApiStudyMaterial;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.livedata.LiveApiProgress;
import com.the_tinkering.wk.livedata.LiveApiState;

import static com.the_tinkering.wk.Constants.HOUR;
import static com.the_tinkering.wk.util.TextUtil.formatTimestampForApi;

/**
 * Task to fetch any study materials that have been updated since the last time this task was run.
 */
public final class GetStudyMaterialsTask extends ApiTask {
    /**
     * Task priority.
     */
    public static final int PRIORITY = 23;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetStudyMaterialsTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    public boolean canRun() {
        return WkApplication.getInstance().getOnlineStatus().canCallApi() && ApiState.getCurrentApiState() == ApiState.OK;
    }

    @Override
    protected void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final long lastGetStudyMaterialsSuccess = db.propertiesDao().getLastStudyMaterialSyncSuccessDate(HOUR);

        LiveApiProgress.reset(true, "study materials");

        String uri = "/v2/study_materials";
        if (lastGetStudyMaterialsSuccess != 0) {
            uri += "?updated_after=" + formatTimestampForApi(lastGetStudyMaterialsSuccess);
        }

        if (!collectionApiCall(uri, ApiStudyMaterial.class, t -> db.subjectSyncDao().insertOrUpdateStudyMaterial(t, false))) {
            return;
        }

        db.propertiesDao().setLastApiSuccessDate(System.currentTimeMillis());
        db.propertiesDao().setLastStudyMaterialSyncSuccessDate(System.currentTimeMillis());
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveApiState.getInstance().forceUpdate();
    }
}
