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

package com.the_tinkering.wk.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.activities.DigraphHelpActivity;
import com.the_tinkering.wk.db.model.SessionItem;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.enums.QuestionType;
import com.the_tinkering.wk.model.DigraphMatch;
import com.the_tinkering.wk.model.FloatingUiState;
import com.the_tinkering.wk.model.Question;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.views.SubjectInfoView;

import java.util.Locale;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Fragment for an answered non-Anki mode question.
 */
public final class AnsweredSessionFragment extends AbstractSessionFragment {
    private @Nullable Question question;
    private @Nullable Subject subject;

    private final ViewProxy scrollView = new ViewProxy();
    private final ViewProxy nextButton = new ViewProxy();
    private final ViewProxy nextButton2 = new ViewProxy();
    private final ViewProxy specialButton1 = new ViewProxy();
    private final ViewProxy specialButton2 = new ViewProxy();
    private final ViewProxy specialButton3 = new ViewProxy();
    private final ViewProxy toastAnimation = new ViewProxy();
    private final ViewProxy questionView = new ViewProxy();
    private final ViewProxy questionEdit = new ViewProxy();
    private final ViewProxy subjectInfo = new ViewProxy();
    private final ViewProxy digraphMatchText = new ViewProxy();

    /**
     * The constructor.
     */
    public AnsweredSessionFragment() {
        super(R.layout.fragment_answered_question);
    }

    /**
     * Create a new instance with arguments set.
     *
     * @param subjectId the subject ID to show
     * @param questionType the type of the question to show
     * @return the fragment
     */
    public static AnsweredSessionFragment newInstance(final long subjectId, final QuestionType questionType) {
        final AnsweredSessionFragment fragment = new AnsweredSessionFragment();

        final Bundle args = new Bundle();
        args.putLong("subjectId", subjectId);
        args.putString("questionType", questionType.name());

        fragment.setArguments(args);
        return fragment;
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
        nextButton.enableInteraction();
        nextButton.requestFocus();
    }

    @Override
    public void onViewCreatedLocal(final View view, final @Nullable Bundle savedInstanceState) {
        if (question == null || subject == null) {
            return;
        }

        onViewCreatedCommon(view, question, subject, false);

        scrollView.setDelegate(view, R.id.scrollView);
        nextButton.setDelegate(view, R.id.nextButton);
        nextButton2.setDelegate(view, R.id.nextButton2);
        specialButton1.setDelegate(view, R.id.specialButton1);
        specialButton2.setDelegate(view, R.id.specialButton2);
        specialButton3.setDelegate(view, R.id.specialButton3);
        toastAnimation.setDelegate(view, R.id.toastAnimation);
        questionView.setDelegate(view, R.id.questionView);
        questionEdit.setDelegate(view, R.id.questionEdit);
        subjectInfo.setDelegate(view, R.id.subjectInfo);
        digraphMatchText.setDelegate(view, R.id.digraphMatchText);

        final @Nullable LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) questionView.getLayoutParams();
        if (params != null) {
            final int height = GlobalSettings.Display.getQuizQuestionViewHeight();
            params.height = height > 0 ? dp2px(height): ViewGroup.LayoutParams.WRAP_CONTENT;
            params.weight = 0;
            questionView.setLayoutParams(params);
        }

        if (session.isCorrect()) {
            questionEdit.setBackgroundColor(ThemeUtil.getColor(R.attr.correctColorBackground));
        }
        else {
            questionEdit.setBackgroundColor(ThemeUtil.getColor(R.attr.incorrectColorBackground));
        }

        questionEdit.setSingleLine();
        questionEdit.setText(FloatingUiState.getCurrentAnswer());
        questionEdit.setTextSize(GlobalSettings.Font.getFontSizeQuestionEdit());
        questionEdit.setTextColor(ThemeUtil.getColor(R.attr.colorPrimary));
        if (question.getType().isAscii()) {
            questionEdit.setRootLocale();
        }
        if (question.getType().isKana()) {
            questionEdit.setJapaneseLocale();
        }

        // Show or hide the subject info dump
        subjectInfo.setToolbar(null);
        subjectInfo.setMaxFontSize(GlobalSettings.Font.getMaxFontSizeQuiz());
        subjectInfo.setContainerType(SubjectInfoView.ContainerType.ANSWERED_QUESTION);
        subjectInfo.setSubject(this, subject);

        final View.OnClickListener listener = v -> safe(() -> {
            if (!interactionEnabled) {
                return;
            }
            if (session.isNextButtonFrozen()) {
                return;
            }
            disableInteraction();
            session.advance();
        });
        nextButton.setOnClickListener(listener);
        nextButton2.setOnClickListener(listener);

        if (FloatingUiState.lastVerdict == null) {
            digraphMatchText.setVisibility(false);
        }
        else {
            final @Nullable DigraphMatch match = FloatingUiState.lastVerdict.getDigraphMatch();
            if (match != null) {
                digraphMatchText.setText(String.format(Locale.ROOT, "Your answer was incorrect because you mixed up the regular kana %c"
                                + " and the small kana %c. Tap this message for more information about the difference between small and regular kana.",
                        match.getRegularKana(), match.getSmallKana()));
                digraphMatchText.setClickableAndNotFocusable(true);
                digraphMatchText.setOnClickListener(v -> goToActivity(DigraphHelpActivity.class));
                digraphMatchText.setVisibility(true);
            }
            else {
                digraphMatchText.setVisibility(false);
            }
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
        safe(() -> {
            nextButton.enableInteraction();
            nextButton2.enableInteraction();
            specialButton1.enableInteraction();
            specialButton2.enableInteraction();
            specialButton3.enableInteraction();
            interactionEnabled = true;
        });
    }

    @Override
    public void disableInteraction() {
        safe(() -> {
            interactionEnabled = false;
            nextButton.disableInteraction();
            nextButton2.disableInteraction();
            specialButton1.disableInteraction();
            specialButton2.disableInteraction();
            specialButton3.disableInteraction();
        });
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
    public FragmentTransitionAnimation getAnimation(final AbstractSessionFragment newFragment) {
        if (newFragment instanceof UnansweredSessionFragment && question == newFragment.getQuestion()) {
            return session.getQuestionChoiceReason().getAnimation();
        }
        if (newFragment.getQuestion() != null && newFragment.getQuestion() != question) {
            return session.getQuestionChoiceReason().getAnimation();
        }
        return FragmentTransitionAnimation.RTL;
    }

    @Override
    public void updateViews() {
        safe(() -> {
            if (GlobalSettings.Review.getShowAnswerToast()) {
                showPreviousAnswerToast(toastAnimation, session.isCorrect() ? R.raw.success : R.raw.fail);
            }
        });
    }
}
