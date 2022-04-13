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
import android.widget.LinearLayout;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.SubjectChangeListener;
import com.the_tinkering.wk.livedata.SubjectChangeWatcher;
import com.the_tinkering.wk.proxy.ViewProxy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a subject's star rating and allows the user to change it by tapping the stars.
 */
public final class StarRatingView extends LinearLayout implements SubjectChangeListener {
    private final List<ViewProxy> stars = new ArrayList<>();

    private @Nullable Subject subject = null;

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public StarRatingView(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public StarRatingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        inflate(getContext(), R.layout.star_rating, this);
        setOrientation(HORIZONTAL);

        stars.add(new ViewProxy(this, R.id.star1));
        stars.add(new ViewProxy(this, R.id.star2));
        stars.add(new ViewProxy(this, R.id.star3));
        stars.add(new ViewProxy(this, R.id.star4));
        stars.add(new ViewProxy(this, R.id.star5));

        stars.get(0).setTag(R.id.advancedSearchSwitchTag, 1);
        stars.get(1).setTag(R.id.advancedSearchSwitchTag, 2);
        stars.get(2).setTag(R.id.advancedSearchSwitchTag, 3);
        stars.get(3).setTag(R.id.advancedSearchSwitchTag, 4);
        stars.get(4).setTag(R.id.advancedSearchSwitchTag, 5);

        for (final ViewProxy star: stars) {
            star.setOnClickListener(v -> {
                final @Nullable Object tag = v.getTag(R.id.advancedSearchSwitchTag);
                final @Nullable Subject currentSubject = subject;
                if (currentSubject != null && tag instanceof Integer) {
                    final int tagInt = (int) tag;
                    final int newNumStars = subject.getNumStars() == tagInt ? 0 : tagInt;
                    runAsync(() -> WkApplication.getDatabase().subjectDao().updateStars(subject.getId(), newNumStars));
                }
            });
        }
    }

    @Override
    public void onSubjectChange(@SuppressWarnings("ParameterHidesMemberVariable") final Subject subject) {
        if (this.subject == null || subject.getId() == this.subject.getId()) {
            this.subject = subject;
            update();
        }
    }

    @Override
    public boolean isInterestedInSubject(final long subjectId) {
        return subject != null && subject.getId() == subjectId;
    }

    /**
     * Set the subject for this view.
     *
     * @param subject the subject
     */
    public void setSubject(final Subject subject) {
        safe(() -> {
            this.subject = subject;
            SubjectChangeWatcher.getInstance().addListener(this);
            update();
        });
    }

    /**
     * Update the view based on the supplied subject.
     */
    private void update() {
        final int numStars = subject == null ? 0 : subject.getNumStars();
        for (int i=0; i<5; i++) {
            final ViewProxy view = stars.get(i);
            view.setText(numStars >= i+1 ? "★" : "☆");
        }
    }
}
