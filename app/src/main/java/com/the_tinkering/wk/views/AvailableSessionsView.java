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

package com.the_tinkering.wk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.model.TimeLine;
import com.the_tinkering.wk.proxy.ViewProxy;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that describes the number of available lessons and reviews.
 */
public final class AvailableSessionsView extends ConstraintLayout {
    private final ViewProxy availableLessonsCount = new ViewProxy();
    private final ViewProxy availableReviewsCount = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public AvailableSessionsView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public AvailableSessionsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.available_sessions, this);
            availableLessonsCount.setDelegate(this, R.id.availableLessonsCount);
            availableReviewsCount.setDelegate(this, R.id.availableReviewsCount);
        });
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        safe(() -> LiveTimeLine.getInstance().observe(lifecycleOwner, t -> safe(() -> {
            if (t != null) {
                update(t);
            }
        })));
    }

    /**
     * Update the text based on the latest TimeLine data available.
     *
     * @param timeLine the timeline
     */
    private void update(final TimeLine timeLine) {
        if (GlobalSettings.getFirstTimeSetup() == 0) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final boolean narrow = (displayMetrics.widthPixels / displayMetrics.density) < 480;

        final int numLessons = timeLine.getNumAvailableLessons();
        availableLessonsCount.setText(numLessons);
        availableLessonsCount.setTextSize(narrow && numLessons >= 1000 ? 28 : 32);

        final int numReviews = timeLine.getNumAvailableReviews();
        availableReviewsCount.setText(numReviews);
        availableReviewsCount.setTextSize(narrow && numReviews >= 1000 ? 28 : 32);
    }
}
