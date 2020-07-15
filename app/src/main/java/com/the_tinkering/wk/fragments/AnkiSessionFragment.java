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

package com.the_tinkering.wk.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.SessionItem;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.model.FloatingUiState;
import com.the_tinkering.wk.model.Question;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.AudioUtil;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.views.SubjectInfoView;

import javax.annotation.Nullable;

/**
 * Fragment for an Anki mode question.
 */
public final class AnkiSessionFragment extends AbstractSessionFragment {
    private static final Logger LOGGER = Logger.get(AnkiSessionFragment.class);

    private @Nullable Question question = null;
    private @Nullable Subject subject = null;
    private boolean showingAnswer = false;

    private final ViewProxy ankiShowAnswerButton = new ViewProxy();
    private final ViewProxy ankiNextButton = new ViewProxy();
    private final ViewProxy ankiCorrectButton = new ViewProxy();
    private final ViewProxy ankiIncorrectButton = new ViewProxy();
    private final ViewProxy answer = new ViewProxy();
    private final ViewProxy synonyms = new ViewProxy();
    private final ViewProxy subjectInfo = new ViewProxy();
    private final ViewProxy scrollView = new ViewProxy();
    private final ViewProxy specialButton1 = new ViewProxy();
    private final ViewProxy specialButton2 = new ViewProxy();
    private final ViewProxy specialButton3 = new ViewProxy();
    private final ViewProxy questionView = new ViewProxy();
    private final ViewProxy buttonsView = new ViewProxy();

    /**
     * The constructor.
     */
    public AnkiSessionFragment() {
        super(R.layout.fragment_anki);
    }

    @Override
    protected void onCreateLocal() {
        final @Nullable Bundle args = getArguments();
        if (args != null) {
            final long subjectId = args.getLong("subjectId", -1);
            final @Nullable SessionItem item = session.findItemBySubjectId(subjectId);
            if (item != null) {
                subject = item.getSubject();
                question = item.getQuestionByTypeStr(args.getString("questionType"));
            }
        }
    }

    @Override
    protected void onResumeLocal() {
        if (session.isAnswered()) {
            ankiNextButton.requestFocus();
        }
        else if (showingAnswer) {
            ankiCorrectButton.requestFocus();
        }
        else {
            ankiShowAnswerButton.requestFocus();
        }
        updateViews();
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        try {
            if (question == null || subject == null) {
                return;
            }

            onViewCreatedCommon(view, question, subject, true);

            ankiShowAnswerButton.setDelegate(view, R.id.ankiShowAnswerButton);
            ankiNextButton.setDelegate(view, R.id.ankiNextButton);
            ankiCorrectButton.setDelegate(view, R.id.ankiCorrectButton);
            ankiIncorrectButton.setDelegate(view, R.id.ankiIncorrectButton);
            answer.setDelegate(view, R.id.answer);
            synonyms.setDelegate(view, R.id.synonyms);
            subjectInfo.setDelegate(view, R.id.subjectInfo);
            scrollView.setDelegate(view, R.id.scrollView);
            specialButton1.setDelegate(view, R.id.specialButton1);
            specialButton2.setDelegate(view, R.id.specialButton2);
            specialButton3.setDelegate(view, R.id.specialButton3);
            questionView.setDelegate(view, R.id.questionView);
            buttonsView.setDelegate(view, R.id.buttonsView);

            // Swap the correct/incorrect buttons if the settings ask for it.
            if (GlobalSettings.Display.getSwapAnkiButtons()) {
                try {
                    final @Nullable ConstraintLayout.LayoutParams correctParams = (ConstraintLayout.LayoutParams) ankiCorrectButton.getLayoutParams();
                    final @Nullable ConstraintLayout.LayoutParams incorrectParams = (ConstraintLayout.LayoutParams) ankiIncorrectButton.getLayoutParams();
                    if (correctParams != null && incorrectParams != null) {
                        correctParams.startToEnd = ConstraintLayout.LayoutParams.UNSET;
                        correctParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        correctParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                        correctParams.endToStart = R.id.ankiIncorrectButton;
                        incorrectParams.startToEnd = R.id.ankiCorrectButton;
                        incorrectParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
                        incorrectParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        incorrectParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
                        ankiCorrectButton.setLayoutParams(correctParams);
                        ankiIncorrectButton.setLayoutParams(incorrectParams);
                    }
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            }

            final @Nullable LinearLayout.LayoutParams qvparams = (LinearLayout.LayoutParams) questionView.getLayoutParams();
            if (qvparams != null) {
                final int height = GlobalSettings.Display.getQuizQuestionViewHeight();
                qvparams.height = height > 0 ? dp2px(height): ViewGroup.LayoutParams.WRAP_CONTENT;
                qvparams.weight = 0;
                questionView.setLayoutParams(qvparams);
            }

            // Resize the Anki buttons as needed
            final @Nullable ViewGroup.LayoutParams params = buttonsView.getLayoutParams();
            if (params != null) {
                params.height = dp2px(GlobalSettings.Display.getAnkiButtonsHeight());
                buttonsView.setLayoutParams(params);
            }

            updateViews();

            ankiShowAnswerButton.setOnClickListener(v -> {
                try {
                    showingAnswer = true;
                    playAudio();
                    updateViews();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            ankiNextButton.setOnClickListener(v -> {
                try {
                    if (!interactionEnabled) {
                        return;
                    }
                    disableInteraction();
                    showingAnswer = false;
                    session.advance();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            ankiCorrectButton.setOnClickListener(v -> {
                try {
                    if (!interactionEnabled) {
                        return;
                    }
                    disableInteraction();
                    showingAnswer = false;
                    session.submitAnkiCorrect();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            ankiIncorrectButton.setOnClickListener(v -> {
                try {
                    if (!interactionEnabled) {
                        return;
                    }
                    disableInteraction();
                    showingAnswer = false;
                    session.submitAnkiIncorrect();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public String getToolbarTitle() {
        return question == null ? "" : question.getTitle();
    }

    @Override
    public int getToolbarBackgroundColor() {
        return subject == null ? 0 : subject.getBackgroundColor();
    }

    @Override
    public void enableInteraction() {
        try {
            ankiShowAnswerButton.enableInteraction();
            ankiCorrectButton.enableInteraction();
            ankiIncorrectButton.enableInteraction();
            ankiNextButton.enableInteraction();
            specialButton1.enableInteraction();
            specialButton2.enableInteraction();
            specialButton3.enableInteraction();
            interactionEnabled = true;
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public void disableInteraction() {
        try {
            interactionEnabled = false;
            ankiShowAnswerButton.disableInteraction();
            ankiCorrectButton.disableInteraction();
            ankiIncorrectButton.disableInteraction();
            ankiNextButton.disableInteraction();
            specialButton1.disableInteraction();
            specialButton2.disableInteraction();
            specialButton3.disableInteraction();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public Subject getCurrentSubject() {
        return subject;
    }

    @Override
    public @Nullable SessionItem getItem() {
        return question == null ? null : question.getItem();
    }

    @Override
    public @Nullable Question getQuestion() {
        return question;
    }

    @Override
    public @Nullable Subject getSubject() {
        return subject;
    }

    @Override
    public void showOrHideSoftInput() {
        hideSoftInput();
    }

    @Override
    public void updateViews() {
        if (question == null || subject == null) {
            return;
        }

        // Show or hide the various Anki buttons
        ankiShowAnswerButton.setVisibility(!session.isAnswered() && !showingAnswer);
        ankiNextButton.setVisibility(session.isAnswered());
        ankiCorrectButton.setVisibility(!session.isAnswered() && showingAnswer);
        ankiIncorrectButton.setVisibility(!session.isAnswered() && showingAnswer);

        answer.setVisibility(showingAnswer);
        answer.setText(question.getAnkiAnswerRichText(subject));
        answer.setTextSize(GlobalSettings.Font.getFontSizeAnkiAnswer());
        if (question.getType().isAscii()) {
            answer.setRootLocale();
        }
        if (question.getType().isKana()) {
            answer.setJapaneseLocale();
        }

        synonyms.setVisibility(!session.isAnswered() && showingAnswer && question.getType().isMeaning() && subject.hasMeaningSynonyms());
        synonyms.setText(subject.getMeaningSynonymsRichText());
        synonyms.setTextSize(GlobalSettings.Font.getFontSizeAnkiAnswer());

        // Show or hide the subject info dump
        subjectInfo.setVisibility(session.isAnswered());
        if (session.isAnswered()) {
            subjectInfo.setToolbar(null);
            subjectInfo.setMaxFontSize(GlobalSettings.Font.getMaxFontSizeQuiz());
            subjectInfo.setContainerType(SubjectInfoView.ContainerType.ANSWERED_QUESTION);
            subjectInfo.setSubject(this, subject);
        }
    }

    @Override
    public FragmentTransitionAnimation getAnimation(final AbstractSessionFragment newFragment) {
        if (newFragment.getQuestion() != null && newFragment.getQuestion() != question) {
            return session.getQuestionChoiceReason().getAnimation();
        }
        return FragmentTransitionAnimation.RTL;
    }

    private void playAudio() {
        if (question == null || subject == null) {
            return;
        }

        if (FloatingUiState.audioPlayed) {
            return;
        }

        final boolean autoplay = GlobalSettings.Audio.getAutoplayAnkiReveal();

        if (autoplay && question.getType().isReading()) {
            FloatingUiState.audioPlayed = true;
            AudioUtil.playAudio(subject, null);
        }
    }
}
