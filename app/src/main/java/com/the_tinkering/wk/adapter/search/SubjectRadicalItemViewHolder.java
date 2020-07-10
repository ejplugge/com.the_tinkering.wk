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

package com.the_tinkering.wk.adapter.search;

import android.view.View;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.model.SrsSystem;

/**
 * View holder class for radical subject items.
 */
public final class SubjectRadicalItemViewHolder extends SubjectItemViewHolder {
    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param actment the actment this view belongs to
     */
    public SubjectRadicalItemViewHolder(final SearchResultAdapter adapter, final View view, final Actment actment) {
        super(adapter, view, actment);
    }

    @Override
    protected void bind() {
        if (subject == null) {
            return;
        }

        itemView.setBackgroundColor(subject.getButtonBackgroundColor());

        button.setSubject(subject);
        button.setSizeSp(24);
        button.setTransparent(true);

        final String details1Text = subject.getOneMeaning();
        details1.setText(details1Text);

        final String details2Text;
        final SrsSystem.Stage stage = subject.getSrsStage();
        if (stage.isLocked()) {
            details2Text = "";
            details2.setVisibility(false);
        }
        else if (subject.getAvailableAt() == null) {
            details2Text = stage.getShortName();
            details2.setVisibility(true);
        }
        else {
            details2Text = stage.getShortName() + " - " + subject.getShortNextReviewWaitTime();
            details2.setVisibility(true);
        }
        details2.setText(details2Text);
    }
}
