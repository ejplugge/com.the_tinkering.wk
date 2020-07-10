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

package com.the_tinkering.wk.livedata;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.model.SrsBreakDown;
import com.the_tinkering.wk.model.SrsBreakDownItem;
import com.the_tinkering.wk.model.SrsSystem;
import com.the_tinkering.wk.model.SrsSystemRepository;

import java.util.List;

/**
 * LiveData that tracks the SRS breakdown data for the dashboard.
 */
public final class LiveSrsBreakDown extends ConservativeLiveData<SrsBreakDown> {
    /**
     * The singleton instance.
     */
    private static final LiveSrsBreakDown instance = new LiveSrsBreakDown();

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveSrsBreakDown getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private LiveSrsBreakDown() {
        //
    }

    @Override
    protected void updateLocal() {
        final AppDatabase db = WkApplication.getDatabase();
        final int userLevel = db.propertiesDao().getUserLevel();
        final SrsBreakDown breakDown = new SrsBreakDown();
        final List<SrsBreakDownItem> items = db.subjectViewsDao().getSrsBreakDownItems(userLevel);
        for (final SrsBreakDownItem item: items) {
            final SrsSystem.Stage stage = SrsSystemRepository.getSrsSystem(item.getSystemId()).getStage(item.getStageId());
            breakDown.addCount(stage, item.getCount());
        }
        breakDown.setOverLevel(db.subjectViewsDao().getSrsBreakDownOverLevel(userLevel));
        instance.postValue(breakDown);
    }

    @Override
    public SrsBreakDown getDefaultValue() {
        return new SrsBreakDown();
    }
}
