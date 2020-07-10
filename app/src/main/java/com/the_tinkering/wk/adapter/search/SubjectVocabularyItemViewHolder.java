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
 * View holder class for vocabulary subject items.
 */
public final class SubjectVocabularyItemViewHolder extends SubjectItemViewHolder {
    /**
     * The view for this holder, inflated but not yet bound.
     *
     * @param adapter the adapter this holder was created for
     * @param view the view
     * @param actment the actment this view belongs to
     */
    public SubjectVocabularyItemViewHolder(final SearchResultAdapter adapter, final View view, final Actment actment) {
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
            details3.setVisibility(true);
        }
        else {
            details3Text = stage.getName() + " - " + subject.getShortNextReviewWaitTime();
            details3.setVisibility(true);
        }
        details3.setText(details3Text);
    }
}
