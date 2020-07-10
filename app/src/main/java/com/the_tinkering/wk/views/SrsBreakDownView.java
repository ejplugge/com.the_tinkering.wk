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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveSrsBreakDown;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.model.SrsBreakDown;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom view that shows the SRS breakdown on the dashboard.
 */
public final class SrsBreakDownView extends ConstraintLayout {
    private static final Logger LOGGER = Logger.get(SrsBreakDownView.class);

    private final List<ViewProxy> counts = new ArrayList<>();
    private final List<ViewProxy> views = new ArrayList<>();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SrsBreakDownView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SrsBreakDownView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.srs_breakdown, this);
            setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));

            counts.add(new ViewProxy(this, R.id.breakdownBucket0Count));
            counts.add(new ViewProxy(this, R.id.breakdownBucket1Count));
            counts.add(new ViewProxy(this, R.id.breakdownBucket2Count));
            counts.add(new ViewProxy(this, R.id.breakdownBucket3Count));
            counts.add(new ViewProxy(this, R.id.breakdownBucket4Count));

            views.add(new ViewProxy(this, R.id.breakdownBucket0View));
            views.add(new ViewProxy(this, R.id.breakdownBucket1View));
            views.add(new ViewProxy(this, R.id.breakdownBucket2View));
            views.add(new ViewProxy(this, R.id.breakdownBucket3View));
            views.add(new ViewProxy(this, R.id.breakdownBucket4View));

            for (int i=0; i<5; i++) {
                views.get(i).setBackgroundColor(ActiveTheme.getShallowStageBucketColors5()[i]);
            }
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
            LiveSrsBreakDown.getInstance().observe(lifecycleOwner, new Observer<SrsBreakDown>() {
                @Override
                public void onChanged(final SrsBreakDown t) {
                    try {
                        if (t != null) {
                            update(t);
                        }
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, new Observer<Integer>() {
                @Override
                public void onChanged(final Integer t) {
                    try {
                        LiveSrsBreakDown.getInstance().ping();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the text based on the latest SrsBreakDown data available.
     *
     * @param srsBreakDown the SRS breakdown summary
     */
    private void update(final SrsBreakDown srsBreakDown) {
        if (GlobalSettings.getFirstTimeSetup() == 0 || !GlobalSettings.Dashboard.getShowSrsBreakdown()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        for (int i=0; i<5; i++) {
            counts.get(i).setText(srsBreakDown.getSrsBreakdownCount(i));
        }
    }
}
