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

package com.the_tinkering.wk.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.util.Logger;

/**
 * A custom view that shows the first-time setup banner.
 */
public final class FirstTimeSetupView extends AppCompatTextView {
    private static final Logger LOGGER = Logger.get(FirstTimeSetupView.class);

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public FirstTimeSetupView(final Context context) {
        super(context, null, R.attr.WK_TextView_Large);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public FirstTimeSetupView(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.WK_TextView_Large);
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        try {
            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, t -> {
                try {
                    update();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the view.
     */
    private void update() {
        if (GlobalSettings.getFirstTimeSetup() == 0) {
            setText("Please wait while we prepare the app for your account. This may take a minute or more...");
            setVisibility(VISIBLE);
        }
        else {
            setVisibility(GONE);
        }
    }
}
