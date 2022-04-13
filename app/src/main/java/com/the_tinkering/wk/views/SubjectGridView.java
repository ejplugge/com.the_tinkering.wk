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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.livedata.SubjectChangeListener;
import com.the_tinkering.wk.livedata.SubjectChangeWatcher;
import com.the_tinkering.wk.model.SubjectCardBinder;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a grid of subjects, showing the text, a meaning, a reading,
 * the SRS stage and the time until the next review.
 */
public final class SubjectGridView extends RigidGridLayout implements SubjectChangeListener, View.OnClickListener {
    private final SubjectCardBinder binder = new SubjectCardBinder(GlobalSettings.Experimental.getSubjectCardLayoutOther());
    private @Nullable WeakLcoRef<Actment> actmentRef = null;
    private List<Long> currentSubjectIds = Collections.emptyList();
    private int spans = 1;
    private boolean showMeaningText = true;
    private boolean showReadingText = true;

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SubjectGridView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SubjectGridView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        safe(() -> {
            setChildMargin(dp2px(2));
            final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            spans = (int) ((metrics.widthPixels / metrics.density + 10) / 90);
            if (spans < 1) {
                spans = 1;
            }
            setNumColumns(spans);
        });
    }

    private static void assignLayoutParams(final View view, final int numSpans) {
        final LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.columnSpan = numSpans;
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);
    }

    private View createSubjectCellView(final Subject subject) {
        final View view = binder.createView(subject.getType(), this);
        final int numSpans;
        if (subject.getType().isRadical()) {
            numSpans = 1;
        }
        else if (subject.getType().isKanji()) {
            numSpans = 1;
        }
        else {
            numSpans = spans >= 6 ? 3 : spans;
        }
        assignLayoutParams(view, numSpans);
        view.setTag(R.id.subjectId, subject.getId());
        binder.bind(view, subject, this, showMeaningText, showReadingText);
        return view;
    }

    private void setSubjectIdsImpl(final Actment actment, final Collection<Long> subjectIds, final boolean showMeaning, final boolean showReading) {
        actmentRef = new WeakLcoRef<>(actment);
        currentSubjectIds = new ArrayList<>(subjectIds);

        runAsync(actment,
                () -> WkApplication.getDatabase().subjectCollectionsDao().getByIds(subjectIds),
                result -> {
                    if (result != null) {
                        final Collection<Subject> subjects = new ArrayList<>();
                        for (final long id: subjectIds) {
                            @Nullable Subject subject = null;
                            for (final Subject candidate: result) {
                                if (candidate.getId() == id) {
                                    subject = candidate;
                                    break;
                                }
                            }
                            if (subject != null) {
                                subjects.add(subject);
                            }
                        }
                        setSubjects(actmentRef.get(), subjects, showMeaning, showReading);
                    }
                });
    }

    /**
     * Populate this table from a collection of subject IDs. The order of the
     * table is the iteration order of the IDs.
     *
     * @param actment the actment this view belongs to
     * @param subjectIds the subject IDs
     * @param showMeaning show a meaning for subjects that have meanings
     * @param showReading show a reading for subjects that have readings
     */
    public void setSubjectIds(final Actment actment, final Collection<Long> subjectIds, final boolean showMeaning, final boolean showReading) {
        safe(() -> setSubjectIdsImpl(actment, subjectIds, showMeaning, showReading));
    }

    /**
     * Populate this table from a collection of subjects. The order of the
     * table is the iteration order of the subjects.
     *
     * @param actment the actment this view belongs to
     * @param subjects the subjects
     * @param showMeaning show a meaning for subjects that have meanings
     * @param showReading show a reading for subjects that have readings
     */
    public void setSubjects(final Actment actment, final Iterable<Subject> subjects, final boolean showMeaning, final boolean showReading) {
        safe(() -> {
            showMeaningText = showMeaning;
            showReadingText = showReading;
            actmentRef = new WeakLcoRef<>(actment);
            currentSubjectIds = new ArrayList<>();
            removeAllViews();
            for (final Subject subject: subjects) {
                currentSubjectIds.add(subject.getId());
                final View cell = createSubjectCellView(subject);
                addView(cell);
            }
            SubjectChangeWatcher.getInstance().addListener(this);
        });
    }

    /**
     * Remove one subject from the table.
     *
     * @param id the subject's ID
     */
    public void removeSubject(final long id) {
        safe(() -> {
            currentSubjectIds.remove(id);
            for (int i=0; i<getChildCount(); i++) {
                final @Nullable View cell = getChildAt(i);
                if (cell != null) {
                    final @Nullable Object subjectIdTag = cell.getTag(R.id.subjectId);
                    if (subjectIdTag instanceof Long && ((long) subjectIdTag) == id) {
                        removeViewAt(i);
                        return;
                    }
                }
            }
        });
    }

    private @Nullable View getViewBySubjectId(final long id) {
        for (int i=0; i<getChildCount(); i++) {
            final @Nullable View cell = getChildAt(i);
            if (cell != null) {
                final @Nullable Object subjectIdTag = cell.getTag(R.id.subjectId);
                if (subjectIdTag instanceof Long && ((long) subjectIdTag) == id) {
                    return cell;
                }
            }
        }
        return null;
    }

    @Override
    public void onSubjectChange(final Subject subject) {
        final @Nullable View cell = getViewBySubjectId(subject.getId());
        if (cell != null) {
            binder.bind(cell, subject, this, showMeaningText, showReadingText);
        }
    }

    @Override
    public boolean isInterestedInSubject(final long subjectId) {
        final @Nullable View cell = getViewBySubjectId(subjectId);
        return cell != null;
    }

    @Override
    public void onClick(final View v) {
        safe(() -> {
            final @Nullable Object subjectIdTag = v.getTag(R.id.subjectId);
            if (subjectIdTag instanceof Long && actmentRef != null) {
                actmentRef.get().goToSubjectInfo((long) subjectIdTag, currentSubjectIds, FragmentTransitionAnimation.RTL);
            }
        });
    }

    private int dp2px(@SuppressWarnings("SameParameterValue") final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
