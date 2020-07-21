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

package com.the_tinkering.wk.jobs;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.livedata.SubjectChangeWatcher;

/**
 * Job to update a session item in the database.
 */
public final class UpdateSubjectStarsJob extends Job {
    private final long subjectId;
    private final int numStars;

    /**
     * The constructor.
     *
     * @param data parameters
     */
    public UpdateSubjectStarsJob(final String data) {
        super(data);
        final String[] parts = data.split(" ");
        subjectId = Long.parseLong(parts[0]);
        numStars = Integer.parseInt(parts[1]);
    }

    @Override
    public void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        db.subjectDao().updateStars(subjectId, numStars);
        SubjectChangeWatcher.getInstance().reportChange(subjectId);
        houseKeeping();
    }
}
