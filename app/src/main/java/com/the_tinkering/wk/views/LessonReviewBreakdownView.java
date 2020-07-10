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
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.model.TimeLine;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

import javax.annotation.Nullable;

/**
 * A custom view that shows a breakdown of available lessons and reviews.
 */
public final class LessonReviewBreakdownView extends TableLayout {
    private static final Logger LOGGER = Logger.get(LessonReviewBreakdownView.class);

    private final ViewProxy headerRad = new ViewProxy();
    private final ViewProxy headerKan = new ViewProxy();
    private final ViewProxy headerVoc = new ViewProxy();
    private final ViewProxy lessonCurrent = new ViewProxy();
    private final ViewProxy lessonCurrentRad = new ViewProxy();
    private final ViewProxy lessonCurrentKan = new ViewProxy();
    private final ViewProxy lessonCurrentVoc = new ViewProxy();
    private final ViewProxy lessonPast = new ViewProxy();
    private final ViewProxy lessonPastRad = new ViewProxy();
    private final ViewProxy lessonPastKan = new ViewProxy();
    private final ViewProxy lessonPastVoc = new ViewProxy();
    private final ViewProxy reviewCurrent = new ViewProxy();
    private final ViewProxy reviewCurrentRad = new ViewProxy();
    private final ViewProxy reviewCurrentKan = new ViewProxy();
    private final ViewProxy reviewCurrentVoc = new ViewProxy();
    private final ViewProxy reviewPast = new ViewProxy();
    private final ViewProxy reviewPastRad = new ViewProxy();
    private final ViewProxy reviewPastKan = new ViewProxy();
    private final ViewProxy reviewPastVoc = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LessonReviewBreakdownView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LessonReviewBreakdownView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.lesson_review_breakdown, this);
            setColumnStretchable(0, true);
            setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));

            headerRad.setDelegate(this, R.id.headerRad);
            headerKan.setDelegate(this, R.id.headerKan);
            headerVoc.setDelegate(this, R.id.headerVoc);
            lessonCurrent.setDelegate(this, R.id.lessonCurrent);
            lessonCurrentRad.setDelegate(this, R.id.lessonCurrentRad);
            lessonCurrentKan.setDelegate(this, R.id.lessonCurrentKan);
            lessonCurrentVoc.setDelegate(this, R.id.lessonCurrentVoc);
            lessonPast.setDelegate(this, R.id.lessonPast);
            lessonPastRad.setDelegate(this, R.id.lessonPastRad);
            lessonPastKan.setDelegate(this, R.id.lessonPastKan);
            lessonPastVoc.setDelegate(this, R.id.lessonPastVoc);
            reviewCurrent.setDelegate(this, R.id.reviewCurrent);
            reviewCurrentRad.setDelegate(this, R.id.reviewCurrentRad);
            reviewCurrentKan.setDelegate(this, R.id.reviewCurrentKan);
            reviewCurrentVoc.setDelegate(this, R.id.reviewCurrentVoc);
            reviewPast.setDelegate(this, R.id.reviewPast);
            reviewPastRad.setDelegate(this, R.id.reviewPastRad);
            reviewPastKan.setDelegate(this, R.id.reviewPastKan);
            reviewPastVoc.setDelegate(this, R.id.reviewPastVoc);

            headerRad.setTextColor(ActiveTheme.getSubjectTypeTextColors()[0]);
            headerRad.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[0]);
            headerKan.setTextColor(ActiveTheme.getSubjectTypeTextColors()[1]);
            headerKan.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[1]);
            headerVoc.setTextColor(ActiveTheme.getSubjectTypeTextColors()[2]);
            headerVoc.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[2]);

            lessonCurrentRad.setTextColor(ActiveTheme.getSubjectTypeTextColors()[0]);
            lessonCurrentRad.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[0]);
            lessonCurrentKan.setTextColor(ActiveTheme.getSubjectTypeTextColors()[1]);
            lessonCurrentKan.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[1]);
            lessonCurrentVoc.setTextColor(ActiveTheme.getSubjectTypeTextColors()[2]);
            lessonCurrentVoc.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[2]);

            lessonPastRad.setTextColor(ActiveTheme.getSubjectTypeTextColors()[0]);
            lessonPastRad.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[0]);
            lessonPastKan.setTextColor(ActiveTheme.getSubjectTypeTextColors()[1]);
            lessonPastKan.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[1]);
            lessonPastVoc.setTextColor(ActiveTheme.getSubjectTypeTextColors()[2]);
            lessonPastVoc.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[2]);

            reviewCurrentRad.setTextColor(ActiveTheme.getSubjectTypeTextColors()[0]);
            reviewCurrentRad.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[0]);
            reviewCurrentKan.setTextColor(ActiveTheme.getSubjectTypeTextColors()[1]);
            reviewCurrentKan.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[1]);
            reviewCurrentVoc.setTextColor(ActiveTheme.getSubjectTypeTextColors()[2]);
            reviewCurrentVoc.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[2]);

            reviewPastRad.setTextColor(ActiveTheme.getSubjectTypeTextColors()[0]);
            reviewPastRad.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[0]);
            reviewPastKan.setTextColor(ActiveTheme.getSubjectTypeTextColors()[1]);
            reviewPastKan.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[1]);
            reviewPastVoc.setTextColor(ActiveTheme.getSubjectTypeTextColors()[2]);
            reviewPastVoc.setBackgroundColor(ActiveTheme.getSubjectTypeBackgroundColors()[2]);
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
            LiveTimeLine.getInstance().observe(lifecycleOwner, new Observer<TimeLine>() {
                @Override
                public void onChanged(final TimeLine t) {
                    try {
                        update(t);
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
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the table based on the latest timeline data.
     *
     * @param timeLine the timeline
     */
    private void update(final @Nullable TimeLine timeLine) {
        if (timeLine == null) {
            setVisibility(GONE);
            return;
        }

        final int numLessons = timeLine.getNumAvailableLessons();
        final int numReviews = timeLine.getNumAvailableReviews();

        if (numLessons == 0 && numReviews == 0
                || LiveFirstTimeSetup.getInstance().get() == 0
                || !GlobalSettings.Dashboard.getShowLessonReviewBreakdown()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        final int userLevel = LiveLevelDuration.getInstance().get().getLevel();

        int lessonCurrentRadCount = 0;
        int lessonCurrentKanCount = 0;
        int lessonCurrentVocCount = 0;
        int lessonPastRadCount = 0;
        int lessonPastKanCount = 0;
        int lessonPastVocCount = 0;

        for (final Subject subject: timeLine.getAvailableLessons()) {
            if (subject.getLevel() == userLevel && subject.getType().isRadical()) {
                lessonCurrentRadCount++;
            }
            if (subject.getLevel() == userLevel && subject.getType().isKanji()) {
                lessonCurrentKanCount++;
            }
            if (subject.getLevel() == userLevel && subject.getType().isVocabulary()) {
                lessonCurrentVocCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isRadical()) {
                lessonPastRadCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isKanji()) {
                lessonPastKanCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isVocabulary()) {
                lessonPastVocCount++;
            }
        }

        lessonCurrent.setVisibility(lessonCurrentRadCount != 0 || lessonCurrentKanCount != 0 || lessonCurrentVocCount != 0);
        lessonCurrentRad.setTextOrBlankIfZero(lessonCurrentRadCount);
        lessonCurrentKan.setTextOrBlankIfZero(lessonCurrentKanCount);
        lessonCurrentVoc.setTextOrBlankIfZero(lessonCurrentVocCount);

        lessonPast.setVisibility(lessonPastRadCount != 0 || lessonPastKanCount != 0 || lessonPastVocCount != 0);
        lessonPastRad.setTextOrBlankIfZero(lessonPastRadCount);
        lessonPastKan.setTextOrBlankIfZero(lessonPastKanCount);
        lessonPastVoc.setTextOrBlankIfZero(lessonPastVocCount);

        int reviewCurrentRadCount = 0;
        int reviewCurrentKanCount = 0;
        int reviewCurrentVocCount = 0;
        int reviewPastRadCount = 0;
        int reviewPastKanCount = 0;
        int reviewPastVocCount = 0;

        for (final Subject subject: timeLine.getAvailableReviews()) {
            if (subject.getLevel() == userLevel && subject.getType().isRadical()) {
                reviewCurrentRadCount++;
            }
            if (subject.getLevel() == userLevel && subject.getType().isKanji()) {
                reviewCurrentKanCount++;
            }
            if (subject.getLevel() == userLevel && subject.getType().isVocabulary()) {
                reviewCurrentVocCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isRadical()) {
                reviewPastRadCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isKanji()) {
                reviewPastKanCount++;
            }
            if (subject.getLevel() < userLevel && subject.getType().isVocabulary()) {
                reviewPastVocCount++;
            }
        }

        reviewCurrent.setVisibility(reviewCurrentRadCount != 0 || reviewCurrentKanCount != 0 || reviewCurrentVocCount != 0);
        reviewCurrentRad.setTextOrBlankIfZero(reviewCurrentRadCount);
        reviewCurrentKan.setTextOrBlankIfZero(reviewCurrentKanCount);
        reviewCurrentVoc.setTextOrBlankIfZero(reviewCurrentVocCount);

        reviewPast.setVisibility(reviewPastRadCount != 0 || reviewPastKanCount != 0 || reviewPastVocCount != 0);
        reviewPastRad.setTextOrBlankIfZero(reviewPastRadCount);
        reviewPastKan.setTextOrBlankIfZero(reviewPastKanCount);
        reviewPastVoc.setTextOrBlankIfZero(reviewPastVocCount);
    }
}
