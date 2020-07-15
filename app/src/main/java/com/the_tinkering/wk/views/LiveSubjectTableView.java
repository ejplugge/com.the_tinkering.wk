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

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.List;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.TableLayout.LayoutParams.WRAP_CONTENT;

/**
 * A custom view that shows a compact table of subjects on the dashboard,
 * backed by a LiveData instance.
 */
public abstract class LiveSubjectTableView extends TableLayout {
    private static final Logger LOGGER = Logger.get(LiveSubjectTableView.class);

    /**
     * The constructor.
     *
     * @param context Android context
     */
    protected LiveSubjectTableView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    protected LiveSubjectTableView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.live_subject_table, this);

            setColumnStretchable(0, true);
            setColumnShrinkable(0, true);
            setColumnStretchable(1, true);
            setColumnShrinkable(1, true);
            setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param actment the lifecycle owner
     */
    public final void setLifecycleOwner(final Actment actment) {
        try {
            registerObserver(actment, t -> {
                try {
                    update(actment, t);
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the text based on the latest subject collection available.
     *
     * @param subjects the subjects for this instance
     */
    private void update(final Actment actment, final @Nullable List<Subject> subjects) {
        try {
            if (subjects == null || subjects.isEmpty() || LiveFirstTimeSetup.getInstance().get() == 0 || !canShow()) {
                setVisibility(GONE);
                return;
            }

            final long[] ids = new long[subjects.size()];
            for (int i=0; i<ids.length; i++) {
                ids[i] = subjects.get(i).getId();
            }

            int i = 1;

            for (final Subject subject: subjects) {
                if (i >= getChildCount()) {
                    final LiveSubjectTableRowView row = new LiveSubjectTableRowView(getContext());
                    final LayoutParams rowLayoutParams = new LayoutParams(0, 0);
                    rowLayoutParams.setMargins(0, 0, 0, 0);
                    rowLayoutParams.width = MATCH_PARENT;
                    rowLayoutParams.height = WRAP_CONTENT;
                    addView(row, rowLayoutParams);
                }
                final @Nullable LiveSubjectTableRowView row = (LiveSubjectTableRowView) getChildAt(i);
                if (row != null) {
                    row.setSubject(subject, this::getExtraText);
                    row.setOnClickListener(v -> actment.goToSubjectInfo(subject.getId(), ids, FragmentTransitionAnimation.RTL));
                }
                i++;
            }

            while (i < getChildCount()) {
                removeViewAt(i);
            }

            setVisibility(VISIBLE);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Register an observer with the LiveData backing this view.
     *
     * @param lifecycleOwner the lifecycle owner for observing LiveData
     * @param observer the observer to register
     */
    protected abstract void registerObserver(LifecycleOwner lifecycleOwner, Observer<? super List<Subject>> observer);

    /**
     * True if this view should be shown at all.
     *
     * @return true if it should
     */
    protected abstract boolean canShow();

    /**
     * Get the extra text shown for a subject on the right hand side of the table.
     *
     * @param subject the subject
     * @return the extra text
     */
    protected abstract String getExtraText(Subject subject);
}
