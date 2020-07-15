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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.model.SessionItem;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.FragmentTransitionAnimation;
import com.the_tinkering.wk.jobs.ReportSessionItemJob;
import com.the_tinkering.wk.livedata.LiveBurnedItems;
import com.the_tinkering.wk.livedata.LiveCriticalCondition;
import com.the_tinkering.wk.livedata.LiveLevelProgress;
import com.the_tinkering.wk.livedata.LiveSrsBreakDown;
import com.the_tinkering.wk.livedata.LiveTimeLine;
import com.the_tinkering.wk.model.Question;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.services.SessionWidgetProvider;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.compareIntegersAndLongs;
import static java.util.Objects.requireNonNull;

/**
 * Fragment for the session summary.
 */
public final class SummarySessionFragment extends AbstractSessionFragment {
    private static final Logger LOGGER = Logger.get(SummarySessionFragment.class);

    private final ViewProxy specialButton1 = new ViewProxy();
    private final ViewProxy specialButton2 = new ViewProxy();
    private final ViewProxy specialButton3 = new ViewProxy();
    private final ViewProxy finishButton = new ViewProxy();
    private final ViewProxy showButton = new ViewProxy();
    private final ViewProxy correctHeader = new ViewProxy();
    private final ViewProxy correctTable = new ViewProxy();
    private final ViewProxy correctSummary = new ViewProxy();
    private final ViewProxy correctPercentage = new ViewProxy();
    private final ViewProxy correctRadicals = new ViewProxy();
    private final ViewProxy correctKanji = new ViewProxy();
    private final ViewProxy correctVocabulary = new ViewProxy();
    private final ViewProxy incorrectHeader = new ViewProxy();
    private final ViewProxy incorrectTable = new ViewProxy();
    private final ViewProxy incorrectSummary = new ViewProxy();
    private final ViewProxy incorrectPercentage = new ViewProxy();
    private final ViewProxy incorrectRadicals = new ViewProxy();
    private final ViewProxy incorrectKanji = new ViewProxy();
    private final ViewProxy incorrectVocabulary = new ViewProxy();
    private final ViewProxy finishProgressBar = new ViewProxy();

    /**
     * The constructor.
     */
    public SummarySessionFragment() {
        super(R.layout.fragment_summary);
    }

    @Override
    protected void onCreateLocal() {
        //
    }

    @Override
    protected void onResumeLocal() {
        finishButton.requestFocus();
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        try {
            specialButton1.setDelegate(view, R.id.specialButton1);
            specialButton2.setDelegate(view, R.id.specialButton2);
            specialButton3.setDelegate(view, R.id.specialButton3);
            finishButton.setDelegate(view, R.id.finishButton);
            showButton.setDelegate(view, R.id.showButton);
            correctHeader.setDelegate(view, R.id.correctHeader);
            correctTable.setDelegate(view, R.id.correctTable);
            correctSummary.setDelegate(view, R.id.correctSummary);
            correctPercentage.setDelegate(view, R.id.correctPercentage);
            correctRadicals.setDelegate(view, R.id.correctRadicals);
            correctKanji.setDelegate(view, R.id.correctKanji);
            correctVocabulary.setDelegate(view, R.id.correctVocabulary);
            incorrectHeader.setDelegate(view, R.id.incorrectHeader);
            incorrectTable.setDelegate(view, R.id.incorrectTable);
            incorrectSummary.setDelegate(view, R.id.incorrectSummary);
            incorrectPercentage.setDelegate(view, R.id.incorrectPercentage);
            incorrectRadicals.setDelegate(view, R.id.incorrectRadicals);
            incorrectKanji.setDelegate(view, R.id.incorrectKanji);
            incorrectVocabulary.setDelegate(view, R.id.incorrectVocabulary);
            finishProgressBar.setDelegate(view, R.id.finishProgressBar);

            int numCorrectRadicals = 0;
            int numCorrectKanji = 0;
            int numCorrectVocabulary = 0;
            int correctTotal = 0;
            int totalRadical = 0;
            int totalKanji = 0;
            int totalVocabulary = 0;
            int total = 0;
            for (final SessionItem item: session.getItems()) {
                if (item.isAbandoned()) {
                    continue;
                }
                final int incorrect = item.getQuestion1Incorrect() + item.getQuestion2Incorrect() + item.getQuestion3Incorrect() + item.getQuestion4Incorrect();
                final Subject subject = requireNonNull(item.getSubject());
                if (subject.getType().isRadical()) {
                    total++;
                    totalRadical++;
                    if (incorrect == 0) {
                        numCorrectRadicals++;
                        correctTotal++;
                    }
                }
                if (subject.getType().isKanji()) {
                    total++;
                    totalKanji++;
                    if (incorrect == 0) {
                        numCorrectKanji++;
                        correctTotal++;
                    }
                }
                if (subject.getType().isVocabulary()) {
                    total++;
                    totalVocabulary++;
                    if (incorrect == 0) {
                        numCorrectVocabulary++;
                        correctTotal++;
                    }
                }
            }
            final float correctFraction = (total == 0) ? 0 : ((float) correctTotal) / total;
            final int totalPercentage = Math.round(correctFraction * 100);

            correctPercentage.setTextFormat("%d%%", totalPercentage);
            correctRadicals.setText(numCorrectRadicals);
            correctKanji.setText(numCorrectKanji);
            correctVocabulary.setText(numCorrectVocabulary);

            incorrectPercentage.setTextFormat("%d%%", 100-totalPercentage);
            incorrectRadicals.setText(totalRadical-numCorrectRadicals);
            incorrectKanji.setText(totalKanji-numCorrectKanji);
            incorrectVocabulary.setText(totalVocabulary-numCorrectVocabulary);

            specialButton1.setVisibility(GlobalSettings.AdvancedOther.getSpecialButton1Behavior().canShow());
            specialButton1.setText(GlobalSettings.AdvancedOther.getSpecialButton1Behavior().getLabel());

            specialButton2.setVisibility(GlobalSettings.AdvancedOther.getSpecialButton2Behavior().canShow());
            specialButton2.setText(GlobalSettings.AdvancedOther.getSpecialButton2Behavior().getLabel());

            specialButton3.setVisibility(GlobalSettings.AdvancedOther.getSpecialButton3Behavior().canShow());
            specialButton3.setText(GlobalSettings.AdvancedOther.getSpecialButton3Behavior().getLabel());

            specialButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        if (!interactionEnabled) {
                            return;
                        }
                        disableInteraction();
                        GlobalSettings.AdvancedOther.getSpecialButton1Behavior().perform();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            specialButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        if (!interactionEnabled) {
                            return;
                        }
                        disableInteraction();
                        GlobalSettings.AdvancedOther.getSpecialButton2Behavior().perform();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            specialButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        if (!interactionEnabled) {
                            return;
                        }
                        disableInteraction();
                        GlobalSettings.AdvancedOther.getSpecialButton3Behavior().perform();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        if (!interactionEnabled) {
                            return;
                        }
                        disableInteraction();
                        finishSession();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });

            showButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        if (!interactionEnabled) {
                            return;
                        }
                        disableInteraction();
                        showItems();
                        enableInteraction();
                    } catch (final Exception e) {
                        LOGGER.uerr(e);
                    }
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public String getToolbarTitle() {
        return "Session summary";
    }

    @Override
    public int getToolbarBackgroundColor() {
        return ThemeUtil.getColor(R.attr.toolbarColorBackground);
    }

    @Override
    public void enableInteraction() {
        try {
            finishButton.enableInteraction();
            showButton.enableInteraction();
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
            finishButton.disableInteraction();
            showButton.disableInteraction();
            specialButton1.disableInteraction();
            specialButton2.disableInteraction();
            specialButton3.disableInteraction();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    public @Nullable Subject getCurrentSubject() {
        return null;
    }

    @Override
    public @Nullable SessionItem getItem() {
        return null;
    }

    @Override
    public @Nullable Question getQuestion() {
        return null;
    }

    @Override
    public @Nullable Subject getSubject() {
        return null;
    }

    @Override
    public void showOrHideSoftInput() {
        hideSoftInput();
    }

    @Override
    public void updateViews() {
        //
    }

    @Override
    public FragmentTransitionAnimation getAnimation(final AbstractSessionFragment newFragment) {
        return FragmentTransitionAnimation.LTR;
    }

    private void finishSession() {
        if (!session.isDelayed()) {
            session.finish();
            return;
        }

        final int total = session.getNumPendingItems();
        finishProgressBar.setMax(total);
        finishProgressBar.setProgress(0);
        finishProgressBar.setVisibility(View.VISIBLE);

        new Task(this, total).execute();
    }

    private void showItems() {
        showButton.setVisibility(false);
        incorrectSummary.setVisibility(false);
        correctSummary.setVisibility(false);
        incorrectHeader.setVisibility(true);
        correctHeader.setVisibility(true);

        final List<SessionItem> incorrect = new ArrayList<>();
        final List<SessionItem> correct = new ArrayList<>();
        for (final SessionItem item: session.getItems()) {
            if (item.isAbandoned()) {
                continue;
            }
            if (item.hasIncorrectAnswers()) {
                incorrect.add(item);
            }
            else {
                correct.add(item);
            }
        }

        final boolean srsRelevant = session.getType().isSrsRelevant();

        Collections.sort(incorrect, new Comparator<SessionItem>() {
            @Override
            public int compare(final SessionItem o1, final SessionItem o2) {
                final @Nullable Subject s1 = o1.getSubject();
                final @Nullable Subject s2 = o2.getSubject();
                if (s1 == null) {
                    return s2 == null ? 0 : -1;
                }
                if (s2 == null) {
                    return 1;
                }
                if (s1.getType().getOrder() != s2.getType().getOrder()) {
                    return Integer.compare(s1.getType().getOrder(), s2.getType().getOrder());
                }
                if (srsRelevant) {
                    final int n = o1.getNewSrsStage().compareTo(o2.getNewSrsStage());
                    if (n != 0) {
                        return n;
                    }
                }
                else {
                    if (s1.getSrsStage() != s2.getSrsStage()) {
                        return s1.getSrsStage().compareTo(s2.getSrsStage());
                    }
                }
                return compareIntegersAndLongs(s1.getLevel(), s2.getLevel(), s1.getLessonPosition(), s2.getLessonPosition(),
                        s1.getId(), s2.getId());
            }
        });

        Collections.sort(correct, new Comparator<SessionItem>() {
            @Override
            public int compare(final SessionItem o1, final SessionItem o2) {
                final @Nullable Subject s1 = o1.getSubject();
                final @Nullable Subject s2 = o2.getSubject();
                if (s1 == null) {
                    return s2 == null ? 0 : -1;
                }
                if (s2 == null) {
                    return 1;
                }
                return compareIntegersAndLongs(s1.getLevel(), s2.getLevel(), s1.getLessonPosition(), s2.getLessonPosition(),
                        s1.getId(), s2.getId());
            }
        });

        final List<Subject> incorrectSubjects = new ArrayList<>();
        for (final SessionItem item: incorrect) {
            final @Nullable Subject subject = item.getSubject();
            if (subject != null) {
                incorrectSubjects.add(subject);
            }
        }
        Collections.sort(incorrectSubjects, new Comparator<Subject>() {
            @Override
            public int compare(final Subject o1, final Subject o2) {
                return Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
            }
        });

        final List<Subject> correctSubjects = new ArrayList<>();
        for (final SessionItem item: correct) {
            final @Nullable Subject subject = item.getSubject();
            if (subject != null) {
                correctSubjects.add(subject);
            }
        }
        Collections.sort(correctSubjects, new Comparator<Subject>() {
            @Override
            public int compare(final Subject o1, final Subject o2) {
                return Integer.compare(o1.getType().getOrder(), o2.getType().getOrder());
            }
        });

        incorrectTable.setVisibility(View.VISIBLE);
        incorrectTable.setSubjects(this, incorrectSubjects);

        correctTable.setVisibility(View.VISIBLE);
        correctTable.setSubjects(this, correctSubjects);
    }

    private static final class Task extends AsyncTask<Void, Integer, Void> {
        private final WeakLcoRef<SummarySessionFragment> fragmentRef;
        private final int total;

        private Task(final SummarySessionFragment fragment, final int total) {
            fragmentRef = new WeakLcoRef<>(fragment);
            this.total = total;
        }

        @Override
        protected @Nullable Void doInBackground(final Void... params) {
            try {
                int count = 0;
                for (final SessionItem item: WkApplication.getDatabase().sessionItemDao().getAll()) {
                    if (item.isPending()) {
                        final ReportSessionItemJob job = new ReportSessionItemJob(
                                item.getId(),
                                item.getAssignmentId(),
                                session.getType(),
                                item.getQuestion1Incorrect(),
                                item.getQuestion2Incorrect() + item.getQuestion3Incorrect() + item.getQuestion4Incorrect(),
                                item.getLastAnswer(),
                                false);
                        job.run();
                        publishProgress(++count);
                    }
                }

                LiveTimeLine.getInstance().update();
                LiveSrsBreakDown.getInstance().update();
                LiveLevelProgress.getInstance().update();
                LiveCriticalCondition.getInstance().update();
                LiveBurnedItems.getInstance().update();
                SessionWidgetProvider.checkAndUpdateWidgets();
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            try {
                int progress = values[0];
                if (progress < 0) {
                    progress = 0;
                }
                if (progress > total) {
                    progress = total;
                }
                fragmentRef.get().finishProgressBar.setProgress(progress);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        @Override
        protected void onPostExecute(final @Nullable Void result) {
            try {
                fragmentRef.get().finishProgressBar.setVisibility(false);
                session.finish();
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }
    }
}
