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
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.livedata.LiveVacationMode;
import com.the_tinkering.wk.model.TimeLine;
import com.the_tinkering.wk.util.Logger;

import java.util.Locale;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.DAY;
import static com.the_tinkering.wk.util.ObjectSupport.getWaitTimeAsInformalString;
import static java.util.Objects.requireNonNull;

/**
 * A custom view that describes the number of upcoming reviews and when they happen.
 */
public final class UpcomingReviewsView extends AppCompatTextView {
    private static final Logger LOGGER = Logger.get(UpcomingReviewsView.class);

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public UpcomingReviewsView(final Context context) {
        super(context, null, R.attr.WK_TextView_Normal);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public UpcomingReviewsView(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.WK_TextView_Normal);
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        try {
            LiveTimeLine.getInstance().observe(lifecycleOwner, new Observer<TimeLine>() {
                @Override
                public void onChanged(final TimeLine t) {
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
                        LiveTimeLine.getInstance().ping();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            LiveVacationMode.getInstance().observe(lifecycleOwner, new Observer<Boolean>() {
                @Override
                public void onChanged(final Boolean t) {
                    try {
                        LiveTimeLine.getInstance().ping();
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
     * Update the text based on the latest TimeLine data available.
     *
     * @param timeLine the timeline
     */
    private void update(final TimeLine timeLine) {
        if (GlobalSettings.getFirstTimeSetup() == 0) {
            setVisibility(GONE);
            return;
        }

        if (LiveVacationMode.getInstance().get()) {
            setText("Vacation mode is active");
            setVisibility(VISIBLE);
            return;
        }

        @SuppressWarnings("StringConcatenationMissingWhitespace")
        final String timeLineSizeDesc = timeLine.getSize() <= 48 ? timeLine.getSize() + "h" : (timeLine.getSize() / 24) + "d";

        final boolean hasAvailableReviews = timeLine.hasAvailableReviews();
        final boolean hasUpcomingReviews = timeLine.hasUpcomingReviews();
        final boolean hasLongTermUpcomingReviews = timeLine.hasLongTermUpcomingReviews();
        final @Nullable String text;

        if (hasAvailableReviews) {
            if (hasUpcomingReviews) {
                text = String.format(Locale.ROOT, "%d reviews available now, %d in next %s",
                        timeLine.getNumAvailableReviews(), timeLine.getNumUpcomingReviews(), timeLineSizeDesc);
            }
            else {
                if (hasLongTermUpcomingReviews) {
                    final long waitTime = requireNonNull(timeLine.getLongTermUpcomingReviewDate()).getTime() - System.currentTimeMillis();
                    final float waitTimeDays = (waitTime * 1.0f) / DAY;
                    text = String.format(Locale.ROOT, "%d reviews available now, %d in %1.1f days",
                            timeLine.getNumAvailableReviews(), timeLine.getNumLongTermUpcomingReviews(), waitTimeDays);
                }
                else {
                    text = String.format(Locale.ROOT, "%d reviews available now", timeLine.getNumAvailableReviews());
                }
            }
        }
        else {
            if (hasUpcomingReviews) {
                final long waitTime = requireNonNull(timeLine.getUpcomingReviewDate()).getTime() - System.currentTimeMillis();
                text = String.format(Locale.ROOT, "%d reviews %s, %d upcoming in next %s",
                        timeLine.getNumSingleSlotUpcomingReviews(), getWaitTimeAsInformalString(waitTime), timeLine.getNumUpcomingReviews(),
                        timeLineSizeDesc);
            }
            else {
                if (hasLongTermUpcomingReviews) {
                    final long waitTime = requireNonNull(timeLine.getLongTermUpcomingReviewDate()).getTime() - System.currentTimeMillis();
                    final float waitTimeDays = (waitTime * 1.0f) / DAY;
                    text = String.format(Locale.ROOT, "%d reviews available in %1.1f days",
                            timeLine.getNumLongTermUpcomingReviews(), waitTimeDays);
                }
                else {
                    text = null;
                }
            }
        }

        if (text == null) {
            setVisibility(View.GONE);
            return;
        }

        setText(text);
        setVisibility(View.VISIBLE);
    }
}
