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
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.TextUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

/**
 * View holder class for subject items.
 */
public final class EventItemViewHolder extends LogItemViewHolder implements View.OnClickListener {
    private @Nullable EventItem event = null;
    private final WeakLcoRef<Actment> actmentRef;
    private final ViewProxy button = new ViewProxy();
    private final ViewProxy age = new ViewProxy();
    private final ViewProxy details = new ViewProxy();

    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param actment the actment this view belongs to
     */
    public EventItemViewHolder(final SessionLogAdapter adapter, final View view, final Actment actment) {
        super(adapter, view);
        actmentRef = new WeakLcoRef<>(actment);
        button.setDelegate(view, R.id.button);
        details.setDelegate(view, R.id.details);
        age.setDelegate(view, R.id.age);
    }

    private void bindHelper() {
        requireNonNull(event);

        final @Nullable SessionItem sessionItem = event.getSessionItem();
        final int bgColor;
        if (sessionItem == null) {
            button.setVisibility(false);

            itemView.setOnClickListener(null);
            button.setOnClickListener(null);
            bgColor = SubjectType.WANIKANI_KANJI.getButtonBackgroundColor();
        }
        else {
            final Subject subject = requireNonNull(sessionItem.getSubject());

            itemView.setBackgroundColor(subject.getButtonBackgroundColor());

            button.setVisibility(true);
            button.setSubject(subject);
            button.setSizeSp(24);
            button.setTransparent(true);

            itemView.setOnClickListener(this);
            button.setOnClickListener(this);
            bgColor = subject.getButtonBackgroundColor();
        }

        final String elapsed = TextUtil.formatElapsedTime(System.currentTimeMillis() - event.getTimestamp());
        age.setText(elapsed + " ago");
        details.setText(event.getText());

        final @Nullable Drawable bgDrawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.small_rounded_corners);
        if (bgDrawable != null) {
            bgDrawable.setColorFilter(new SimpleColorFilter(bgColor));
            itemView.setBackground(bgDrawable);
        }
    }

    @Override
    public void bind(final LogItem newItem) {
        safe(() -> {
            if (!(newItem instanceof EventItem)) {
                return;
            }
            event = (EventItem) newItem;
            bindHelper();
        });
    }

    @Override
    public void onClick(final View v) {
        safe(() -> {
            final @Nullable Actment theActment = actmentRef.getOrElse(null);
            if (theActment == null || event == null || event.getSessionItem() == null || event.getSessionItem().getSubject() == null) {
                return;
            }
            final Subject subject = event.getSessionItem().getSubject();
            theActment.goToSubjectInfo(subject.getId(), new long[0], FragmentTransitionAnimation.RTL);
        });
    }
}
