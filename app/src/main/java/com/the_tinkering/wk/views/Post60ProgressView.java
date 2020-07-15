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
import android.widget.LinearLayout;

import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveSrsBreakDown;
import com.the_tinkering.wk.model.SrsBreakDown;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

/**
 * A custom view that shows the post-60 progress bar.
 */
public final class Post60ProgressView extends LinearLayout {
    private static final Logger LOGGER = Logger.get(Post60ProgressView.class);

    private final ViewProxy barView = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public Post60ProgressView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public Post60ProgressView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.post60_progress, this);
            setOrientation(VERTICAL);
            setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));
            barView.setDelegate(this, R.id.bar);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        try {
            LiveSrsBreakDown.getInstance().observe(lifecycleOwner, t -> {
                try {
                    if (t != null) {
                        update(t);
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, t -> {
                try {
                    LiveSrsBreakDown.getInstance().ping();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the bar based on the latest SRS breakdown data available.
     *
     * @param srsBreakDown the SRS breakdown data
     */
    private void update(final SrsBreakDown srsBreakDown) {
        if (GlobalSettings.getFirstTimeSetup() == 0 || !GlobalSettings.Dashboard.getShowPost60Progression()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        barView.setBreakdown(srsBreakDown);
    }
}
