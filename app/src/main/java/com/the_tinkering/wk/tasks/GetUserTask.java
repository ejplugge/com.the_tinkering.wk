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
import com.the_tinkering.wk.api.model.ApiSubscription;
import com.the_tinkering.wk.api.model.ApiUser;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.livedata.LiveApiState;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.livedata.LiveVacationMode;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.isEqual;
import static com.the_tinkering.wk.util.ObjectSupport.orElse;

/**
 * Task to fetch the user details for the app's user. Apart from remembering a few
 * fields for various purposes, this is also used as a sanity check to verify that
 * the API is reachable and functional.
 */
public final class GetUserTask extends ApiTask {
    /**
     * Task priority. Top priority, any other API tasks are not very useful if this cannot succeed.
     */
    public static final int PRIORITY = 2;

    /**
     * The constructor.
     *
     * @param taskDefinition the definition of this task in the database
     */
    public GetUserTask(final TaskDefinition taskDefinition) {
        super(taskDefinition);
    }

    @Override
    public boolean canRun() {
        return WkApplication.getInstance().getOnlineStatus().canCallApi() && ApiState.getCurrentApiState().canGetUserData();
    }

    @Override
    protected void runLocal() {
        final @Nullable ApiUser user = singleEntityApiCall("/v2/user", ApiUser.class);
        if (user == null) {
            return;
        }

        final AppDatabase db = WkApplication.getDatabase();
        final @Nullable String oldUserId = db.propertiesDao().getUserId();
        if (!isEqual(user.getId(), oldUserId)) {
            db.resetDatabase();
        }

        final @Nullable int oldLevel = db.propertiesDao().getUserLevel();
        if (oldLevel != user.getLevel()) {
            db.propertiesDao().setUserLevel(user.getLevel());
            db.propertiesDao().setForceLateRefresh(true);
        }

        db.propertiesDao().setUserId(user.getId());
        db.propertiesDao().setUsername(orElse(user.getUsername(), ""));

        final boolean oldVacationMode = db.propertiesDao().getVacationMode();
        final boolean newVacationMode = user.getCurrentVacationStartedAt() != 0;
        if (oldVacationMode != newVacationMode) {
            db.propertiesDao().setVacationMode(newVacationMode);
            LiveTimeLine.getInstance().update();
        }

        final @Nullable ApiSubscription subscription = user.getSubscription();
        final int maxLevelGranted = subscription == null ? user.getMaxLevelGrantedBySubscription() : subscription.getMaxLevelGranted();
        db.propertiesDao().setUserMaxLevelGranted(maxLevelGranted);

        db.propertiesDao().setApiKeyRejected(false);
        db.propertiesDao().setApiInError(false);
        db.propertiesDao().setLastApiSuccessDate(System.currentTimeMillis());
        db.propertiesDao().setLastUserSyncSuccessDate(System.currentTimeMillis());
        db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
        LiveApiState.getInstance().forceUpdate();
        LiveLevelDuration.getInstance().forceUpdate();
        LiveVacationMode.getInstance().forceUpdate();
    }
}
