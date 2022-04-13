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

package com.the_tinkering.wk.services;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.jobs.Job;

/**
 * An intent service for running jobs. Jobs are tasks that have to be
 * pushed to a background task and should only run one at a time,
 * but should be run as soon as reasonably possible.
 *
 * <p>
 *     Jobs are mostly about doing database writes in the background,
 *     and to do regular background housekeeping.
 * </p>
 */
public final class JobRunnerService {
    private JobRunnerService() {
        //
    }

    /**
     * Schedule a job for this service. It goes into a queue of pending jobs,
     * and will be executed as soon as the service has time for it.
     *
     * @param jobClass the class that implements the job being scheduled
     * @param jobData parameters for this job, encoded in a class-specific format
     */
    public static void schedule(final Class<? extends Job> jobClass, final String jobData) {
        safe(() -> {
            final Data data = new Data.Builder()
                    .putString("com.the_tinkering.wk.JOB_CLASS", jobClass.getCanonicalName())
                    .putString("com.the_tinkering.wk.JOB_DATA", jobData)
                    .build();
            final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(JobRunnerWorker.class)
                    .setInputData(data)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build();
            final WorkManager workManager = WorkManager.getInstance(WkApplication.getInstance());
            workManager.enqueueUniqueWork("JobRunnerWorker", ExistingWorkPolicy.APPEND, request);
        });
    }
}
