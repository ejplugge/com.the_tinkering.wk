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
import com.the_tinkering.wk.enums.QuestionType;

import javax.annotation.Nullable;

/**
 * Job to update the current session item / question type.
 */
public final class SetCurrentSessionItemJob extends Job {
    private final long itemId;
    private final QuestionType questionType;

    /**
     * The constructor.
     *
     * @param data parameters
     */
    public SetCurrentSessionItemJob(final String data) {
        super(data);
        final String[] parts = data.split(" ");
        itemId = Long.parseLong(parts[0]);
        @Nullable QuestionType type = QuestionType.WANIKANI_RADICAL_NAME;
        try {
            type = QuestionType.valueOf(parts[1]);
        }
        catch (final Exception e) {
            //
        }
        questionType = type;
    }

    @Override
    public void runLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        db.propertiesDao().setCurrentItemId(itemId);
        db.propertiesDao().setCurrentQuestionType(questionType);
        houseKeeping();
    }
}
