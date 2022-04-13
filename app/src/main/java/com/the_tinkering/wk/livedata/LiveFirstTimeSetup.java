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

package com.the_tinkering.wk.livedata;

import com.the_tinkering.wk.GlobalSettings;

/**
 * LiveData for the first-time setup setting. This indicates that the user is
 * in first-time setup mode and most functionality is locked out on the dashboard
 * until this is complete. This LiveData lets the dashboard components know when
 * it's safe to show themselves.
 */
public final class LiveFirstTimeSetup extends ConservativeLiveData<Integer> {
    /**
     * The singleton instance.
     */
    private static final LiveFirstTimeSetup instance = new LiveFirstTimeSetup();

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveFirstTimeSetup getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private LiveFirstTimeSetup() {
        //
    }

    @Override
    protected void updateLocal() {
        instance.postValue(GlobalSettings.getFirstTimeSetup());
    }

    @Override
    public Integer getDefaultValue() {
        return 1;
    }
}
