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

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.model.TaskCounts;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

/**
 * A special case LiveData for information about existing task records in the database.
 *
 * <p>
 *     It must be possible for the main activity to observe this before the backing LiveData
 *     instance is created by Room. So this just delegates to the Room's LiveData as soon
 *     as that becomes available.
 * </p>
 */
public final class LiveTaskCounts extends LiveData<TaskCounts> {
    private static final Logger LOGGER = Logger.get(LiveTaskCounts.class);

    /**
     * The singleton instance.
     */
    private static final LiveTaskCounts instance = new LiveTaskCounts();

    /**
     * The backing LiveData created by Room.
     */
    private @Nullable LiveData<TaskCounts> counts = null;

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveTaskCounts getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private LiveTaskCounts() {
        //
    }

    /**
     * Initialize this instance with the available database.
     */
    public void initialize() {
        try {
            if (counts == null) {
                final AppDatabase db = WkApplication.getDatabase();
                counts = db.taskDefinitionDao().getLiveCounts();
                counts.observeForever(new Observer<TaskCounts>() {
                    @Override
                    public void onChanged(final TaskCounts t) {
                        try {
                            if (t != null) {
                                postValue(t);
                            }
                        } catch (final Exception e) {
                            LOGGER.uerr(e);
                        }
                    }
                });
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected @Nullable Void doInBackground(final Void... params) {
                        try {
                            LiveApiProgress.getInstance().setSyncReminder(WkApplication.getDatabase().propertiesDao().getSyncReminder());
                        } catch (final Exception e) {
                            LOGGER.uerr(e);
                        }
                        return null;
                    }
                }.execute();
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Get the value, or a dummy instance if no value is available yet.
     *
     * @return the value
     */
    public TaskCounts get() {
        if (getValue() == null) {
            return new TaskCounts(0, 0, 0);
        }
        return getValue();
    }
}
