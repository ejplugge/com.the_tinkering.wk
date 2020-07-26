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

package com.the_tinkering.wk.adapter.sessionlog;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.SimpleColorFilter;
import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.SessionItem;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.WeakLcoRef;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

/**
 * View holder class for subject items.
 */
public final class SessionItemItemViewHolder extends LogItemViewHolder implements View.OnClickListener {
    private @Nullable SessionItem sessionItem = null;
    private final WeakLcoRef<Actment> actmentRef;
    private final ViewProxy button = new ViewProxy();
    private final ViewProxy questionMark = new ViewProxy();
    private final ViewProxy question1Status = new ViewProxy();
    private final ViewProxy question2Status = new ViewProxy();
    private final ViewProxy question3Status = new ViewProxy();
    private final ViewProxy question4Status = new ViewProxy();

    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param actment the actment this view belongs to
     */
    public SessionItemItemViewHolder(final SessionLogAdapter adapter, final View view, final Actment actment) {
        super(adapter, view);
        actmentRef = new WeakLcoRef<>(actment);
        button.setDelegate(view, R.id.button);
        questionMark.setDelegate(view, R.id.questionMark);
        question1Status.setDelegate(view, R.id.question1Status);
        question2Status.setDelegate(view, R.id.question2Status);
        question3Status.setDelegate(view, R.id.question3Status);
        question4Status.setDelegate(view, R.id.question4Status);
    }

    private void bindHelper() {
        requireNonNull(sessionItem);
        final Subject subject = requireNonNull(sessionItem.getSubject());

        itemView.setBackgroundColor(subject.getButtonBackgroundColor());

        button.setSubject(subject);
        button.setSizeSp(24);
        button.setTransparent(true);

        itemView.setOnClickListener(this);
        button.setOnClickListener(this);

        final @Nullable Drawable bgDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.small_rounded_corners);
        if (bgDrawable != null) {
            bgDrawable.setColorFilter(new SimpleColorFilter(subject.getButtonBackgroundColor()));
            itemView.setBackground(bgDrawable);
        }
    }

    @Override
    public void bind(final LogItem newItem) {
        safe(() -> {
            if (!(newItem instanceof SessionItemItem)) {
                return;
            }
            sessionItem = ((SessionItemItem) newItem).getSessionItem();
            bindHelper();
        });
    }

    @Override
    public void onClick(final View v) {
        safe(() -> {
            final @Nullable Actment theActment = actmentRef.getOrElse(null);
            if (theActment == null || sessionItem == null || sessionItem.getSubject() == null) {
                return;
            }
            // TODO fill in context list
            theActment.goToSubjectInfo(sessionItem.getSubject().getId(), new long[0], FragmentTransitionAnimation.RTL);
        });
    }
}
