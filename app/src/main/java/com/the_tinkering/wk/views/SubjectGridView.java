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
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.gridlayout.widget.GridLayout;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.livedata.SubjectChangeListener;
import com.the_tinkering.wk.livedata.SubjectChangeWatcher;
import com.the_tinkering.wk.model.SrsSystem;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A custom view that shows a grid of subjects, showing the text, a meaning, a reading,
 * the SRS stage and the time until the next review.
 */
public final class SubjectGridView extends GridLayout implements SubjectChangeListener, View.OnClickListener {
    private static final Logger LOGGER = Logger.get(SubjectGridView.class);

    private @Nullable WeakLcoRef<Actment> actmentRef = null;
    private List<Long> currentSubjectIds = Collections.emptyList();
    private int spans = 1;
    private int currentRow = 0;
    private int currentColumn = 0;

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
        try {
            setOrientation(HORIZONTAL);

            final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            spans = (int) ((metrics.widthPixels / metrics.density + 10) / 90);
            if (spans < 1) {
                spans = 1;
            }
            setColumnCount(spans);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private void bind(final View view, final Subject subject) {
        final ViewProxy button = new ViewProxy(view, R.id.button);
        final ViewProxy details1 = new ViewProxy(view, R.id.details1);
        final ViewProxy details2 = new ViewProxy(view, R.id.details2);
        final ViewProxy details3 = new ViewProxy(view, R.id.details3);

        view.setBackgroundColor(subject.getButtonBackgroundColor());

        button.setSubject(subject);
        button.setSizeSp(24);
        button.setTransparent(true);

        final String details1Text = subject.getOneMeaning();
        details1.setText(details1Text);

        if (subject.getType().isRadical()) {
            final String details2Text;
            final SrsSystem.Stage stage = subject.getSrsStage();
            if (stage.isLocked()) {
                details2Text = "";
                details2.setVisibility(false);
            }
            else if (subject.getAvailableAt() == null) {
                details2Text = stage.getShortName();
            }
            else {
                details2Text = stage.getShortName() + " - " + subject.getShortNextReviewWaitTime();
            }
            details2.setText(details2Text);
        }
        else if (subject.getType().isKanji()) {
            final String details2Text = subject.getOneReading();
            details2.setText(details2Text);

            final String details3Text;
            final SrsSystem.Stage stage = subject.getSrsStage();
            if (stage.isLocked()) {
                details3Text = "";
                details3.setVisibility(false);
            }
            else if (subject.getAvailableAt() == null) {
                details3Text = stage.getShortName();
            }
            else {
                details3Text = stage.getShortName() + " - " + subject.getShortNextReviewWaitTime();
            }
            details3.setText(details3Text);
        }
        else {
            final String details2Text = subject.getOneReading();
            details2.setText(details2Text);

            final String details3Text;
            final SrsSystem.Stage stage = subject.getSrsStage();
            if (stage.isLocked()) {
                details3Text = "";
                details3.setVisibility(false);
            }
            else if (subject.getAvailableAt() == null) {
                details3Text = stage.getName();
            }
            else {
                details3Text = stage.getName() + " - " + subject.getShortNextReviewWaitTime();
            }
            details3.setText(details3Text);
        }

        view.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private void addSpace() {
        final Space space = new Space(getContext());
        final LayoutParams params = new LayoutParams(spec(currentRow, 1, FILL, 1), spec(currentColumn, 1, FILL, 1));
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        addView(space, params);
        currentColumn++;
    }

    private void assignSpecs(final View view, final int numSpans) {
        final LayoutParams params = (LayoutParams) view.getLayoutParams();

        if (currentColumn > 0 && currentColumn + numSpans > spans) {
            while (currentColumn < spans) {
                addSpace();
            }

            currentColumn = 0;
            currentRow++;
        }

        params.rowSpec = spec(currentRow, 1, FILL, 1);
        params.columnSpec = spec(currentColumn, numSpans, FILL, 1);
        currentColumn += numSpans;

        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        view.setLayoutParams(params);
    }

    private View createSubjectCellView(final Subject subject) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view;
        final int numSpans;
        if (subject.getType().isRadical()) {
            view = inflater.inflate(R.layout.search_result_subject_radical, this, false);
            numSpans = 1;
        }
        else if (subject.getType().isKanji()) {
            view = inflater.inflate(R.layout.search_result_subject_kanji, this, false);
            numSpans = 1;
        }
        else {
            view = inflater.inflate(R.layout.search_result_subject_vocabulary, this, false);
            numSpans = spans >= 6 ? 3 : spans;
        }
        assignSpecs(view, numSpans);
        view.setTag(R.id.subjectId, subject.getId());
        bind(view, subject);
        return view;
    }

    /**
     * Populate this table from a collection of subject IDs. The order of the
     * table is the iteration order of the IDs.
     *
     * @param actment the actment this view belongs to
     * @param subjectIds the subject IDs
     */
    public void setSubjectIds(final Actment actment, final Collection<Long> subjectIds) {
        try {
            actmentRef = new WeakLcoRef<>(actment);
            currentSubjectIds = new ArrayList<>(subjectIds);
            new Task(actment, this, subjectIds).execute();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Populate this table from a collection of subjects. The order of the
     * table is the iteration order of the subjects.
     *
     * @param actment the actment this view belongs to
     * @param subjects the subjects
     */
    public void setSubjects(final Actment actment, final Iterable<Subject> subjects) {
        try {
            actmentRef = new WeakLcoRef<>(actment);
            currentSubjectIds = new ArrayList<>();
            removeAllViews();
            for (final Subject subject: subjects) {
                currentSubjectIds.add(subject.getId());
                final View cell = createSubjectCellView(subject);
                addView(cell);
            }
            while (currentColumn > 0 && currentColumn < spans) {
                addSpace();
            }
            SubjectChangeWatcher.getInstance().addListener(this);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Remove one subject from the table.
     *
     * @param id the subject's ID
     */
    public void removeSubject(final long id) {
        try {
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
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
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
            bind(cell, subject);
        }
    }

    @Override
    public boolean isInterestedInSubject(final long subjectId) {
        final @Nullable View cell = getViewBySubjectId(subjectId);
        return cell != null;
    }

    @Override
    public void onClick(final View v) {
        try {
            final @Nullable Object subjectIdTag = v.getTag(R.id.subjectId);
            if (subjectIdTag instanceof Long && actmentRef != null) {
                actmentRef.get().goToSubjectInfo((long) subjectIdTag, currentSubjectIds, FragmentTransitionAnimation.RTL);
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    private static final class Task extends AsyncTask<Void, Void, List<Subject>> {
        private final WeakLcoRef<Actment> actmentRef;
        private final SubjectGridView view;
        private final Collection<Long> subjectIds;

        private Task(final Actment actment, final SubjectGridView view, final Collection<Long> subjectIds) {
            actmentRef = new WeakLcoRef<>(actment);
            this.view = view;
            this.subjectIds = subjectIds;
        }

        @Override
        protected List<Subject> doInBackground(final Void... params) {
            try {
                final AppDatabase db = WkApplication.getDatabase();
                return db.subjectCollectionsDao().getByIds(subjectIds);
            } catch (final Exception e) {
                LOGGER.uerr(e);
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(final List<Subject> result) {
            try {
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
                view.setSubjects(actmentRef.get(), subjects);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
