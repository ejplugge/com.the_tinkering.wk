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

package com.the_tinkering.wk.services;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.the_tinkering.wk.WkApplication;

/**
 * An intent service for running tasks. Tasks are actions that need to run
 * in the background, don't have to run immediately, may take a long time to
 * complete (usually because they are network calls), and must be persisted
 * so they will be executed even across restarts and when errors occur.
 *
 * <p>
 *     Tasks are recorded in the database. This service will loop over them
 *     one by one in priority order, taking into account the current online
 *     status.
 * </p>
 */
public final class ApiTaskService {
    private ApiTaskService() {
        //
    }

    /**
     * Schedule a run of the service to be executed on a background thread.
     * This is regularly called from job housekeeping.
     */
    public static void schedule() {
        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ApiTaskWorker.class)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();
        final WorkManager workManager = WorkManager.getInstance(WkApplication.getInstance());
        workManager.enqueueUniqueWork("ApiTaskWorker", ExistingWorkPolicy.APPEND, request);
    }

    public static void runTasks() {
        safe(ApiTaskWorker::runTasksImpl);
    }
}
