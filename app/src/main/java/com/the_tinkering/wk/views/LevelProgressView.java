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
import android.widget.TableLayout;

import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveLevelProgress;
import com.the_tinkering.wk.model.LevelProgress;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.TableLayout.LayoutParams.WRAP_CONTENT;

/**
 * A custom view that shows the level progress bar chart.
 */
public final class LevelProgressView extends TableLayout {
    private static final Logger LOGGER = Logger.get(LevelProgressView.class);

    private final List<ViewProxy> legendBuckets = new ArrayList<>();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LevelProgressView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LevelProgressView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.level_progress, this);
            setColumnStretchable(0, true);
            setColumnShrinkable(0, true);
            setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));

            legendBuckets.add(new ViewProxy(this, R.id.legendBucket0));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket1));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket2));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket3));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket4));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket5));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket6));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket7));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket8));
            legendBuckets.add(new ViewProxy(this, R.id.legendBucket9));

            for (int i=0; i<legendBuckets.size(); i++) {
                legendBuckets.get(i).setBackgroundColor(ActiveTheme.getLevelProgressionBucketColors()[i]);
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
            LiveLevelProgress.getInstance().observe(lifecycleOwner, t -> {
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
                    LiveLevelProgress.getInstance().ping();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the text based on the latest LevelProgress data available.
     *
     * @param levelProgress the level progress data
     */
    private void update(final LevelProgress levelProgress) {
        if (GlobalSettings.getFirstTimeSetup() == 0 || !GlobalSettings.Dashboard.getShowLevelProgression()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        int i = 1;

        for (final LevelProgress.BarEntry entry: levelProgress.getEntries()) {
            if (i >= getChildCount()-1) {
                final LevelProgressRowView row = new LevelProgressRowView(getContext());
                final LayoutParams rowLayoutParams = new LayoutParams(0, 0);
                rowLayoutParams.setMargins(0, dp2px(4), 0, 0);
                rowLayoutParams.width = MATCH_PARENT;
                rowLayoutParams.height = WRAP_CONTENT;
                addView(row, i, rowLayoutParams);
            }
            ((LevelProgressRowView) getChildAt(i)).setEntry(entry);
            i++;
        }

        while (i < getChildCount()-1) {
            removeViewAt(i);
        }
    }

    private int dp2px(@SuppressWarnings("SameParameterValue") final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
