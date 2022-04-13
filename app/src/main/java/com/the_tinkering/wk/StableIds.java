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

package com.the_tinkering.wk;

/**
 * A few stable IDs for which the value doesn't really matter, but which must remain constant across invocations.
 */
public final class StableIds {
    private StableIds() {
        //
    }

    /**
     * Request code for background alarm - old pre-19 variant.
     */
    public static final int BACKGROUND_ALARM_REQUEST_CODE_1 = 6;

    /**
     * Request code for background alarm - old pre-23 variant.
     */
    public static final int BACKGROUND_ALARM_REQUEST_CODE_2 = 7;

    /**
     * Request code for background alarm - post-23 variant.
     */
    public static final int BACKGROUND_ALARM_REQUEST_CODE_3 = 8;

    /**
     * Notification ID for JobRynnerService. Only used if a job can't be expedited by the WorkManager.
     */
    public static final int JOB_RUNNER_SERVICE_NOTIFICATION_ID = 9;

    /**
     * Notification ID for ApiTaskService. Only used if a task can't be expedited by the WorkManager.
     */
    public static final int API_TASK_SERVICE_NOTIFICATION_ID = 10;
}
