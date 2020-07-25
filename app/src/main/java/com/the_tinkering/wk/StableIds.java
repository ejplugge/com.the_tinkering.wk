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
     * The ID for jobs running in the JobRunnerService.
     */
    public static final int JOB_RUNNER_SERVICE_JOB_ID = 1;

    /**
     * The ID for jobs running in the ApiTaskService.
     */
    public static final int API_TASK_SERVICE_JOB_ID = 2;

    /**
     * Activity result code when importing a font file.
     */
    public static final int FONT_IMPORT_RESULT_CODE = 3;

    /**
     * Activity result code when importing search presets.
     */
    public static final int SEARCH_PRESET_IMPORT_RESULT_CODE = 4;

    /**
     * Activity result code when importing star ratings.
     */
    public static final int STAR_RATINGS_IMPORT_RESULT_CODE = 5;
}
