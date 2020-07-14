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

package com.the_tinkering.wk.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.proxy.ViewProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that can bind a subject's data to a subject card view, as directed by configuration properties.
 */
public final class SubjectCardBinder {
    private final List<Integer> layoutIds = new ArrayList<>();

    /**
     * The constructor.
     */
    public SubjectCardBinder() {
        layoutIds.add(R.layout.subject_card_radical);
        layoutIds.add(R.layout.subject_card_kanji);
        layoutIds.add(R.layout.subject_card_vocabulary);
    }

    /**
     * Create a view for a subject type with the given parent. The returned view is not bound to anything yet,
     * but it's suitable to be passed to a later bind() call.
     *
     * @param subjectType the subject's type
     * @param parent the parent view this will belong to
     * @return the view
     */
    public View createView(final SubjectType subjectType, final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (subjectType.isRadical()) {
            return inflater.inflate(layoutIds.get(0), parent, false);
        }
        if (subjectType.isKanji()) {
            return inflater.inflate(layoutIds.get(1), parent, false);
        }
        return inflater.inflate(layoutIds.get(2), parent, false);
    }

    /**
     * Bind a subject to a view, filling in all of the relevant details, and setting an onClick listener.
     * This does not register a SubjectChangeListener.
     * @param view the view to bind to
     * @param subject the subject to bind to the view
     * @param onClickListener the onClick listener for the view
     */
    public void bind(final View view, final Subject subject, final View.OnClickListener onClickListener) {
        final ViewProxy button = new ViewProxy(view, R.id.button);
        final ViewProxy meaning = new ViewProxy(view, R.id.meaning);
        final ViewProxy reading = new ViewProxy(view, R.id.reading);
        final ViewProxy progress = new ViewProxy(view, R.id.progress);

        view.setBackgroundColor(subject.getButtonBackgroundColor());

        button.setSubject(subject);
        button.setSizeSp(24);
        button.setTransparent(true);

        if (subject.hasMeanings()) {
            meaning.setText(subject.getOneMeaning());
            meaning.setVisibility(true);
        }
        else {
            meaning.setVisibility(false);
        }

        if (subject.hasReadings()) {
            reading.setText(subject.getOneReading());
            reading.setJapaneseLocale();
            reading.setVisibility(true);
        }
        else {
            reading.setVisibility(false);
        }

        final SrsSystem.Stage stage = subject.getSrsStage();
        final String stageName = subject.getType().isVocabulary() ? stage.getName() : stage.getShortName();
        if (stage.isLocked()) {
            progress.setVisibility(false);
        }
        else if (subject.getAvailableAt() == null) {
            progress.setText(stageName);
            progress.setVisibility(true);
        }
        else {
            progress.setText(stageName + " - " + subject.getShortNextReviewWaitTime());
            progress.setVisibility(true);
        }

        view.setOnClickListener(onClickListener);
        button.setOnClickListener(onClickListener);
    }
}
