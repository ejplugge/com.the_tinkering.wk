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
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.activities.BurnActivity;
import com.the_tinkering.wk.activities.ResurrectActivity;
import com.the_tinkering.wk.api.model.ContextSentence;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SubjectInfoDump;
import com.the_tinkering.wk.jobs.SaveStudyMaterialJob;
import com.the_tinkering.wk.livedata.SubjectChangeListener;
import com.the_tinkering.wk.livedata.SubjectChangeWatcher;
import com.the_tinkering.wk.model.FloatingUiState;
import com.the_tinkering.wk.model.Question;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.services.JobRunnerService;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.isTrue;
import static com.the_tinkering.wk.util.ObjectSupport.orElse;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Custom view that shows the subject info dump.
 */
public final class SubjectInfoView extends LinearLayout implements SubjectChangeListener {
    private long subjectId = -1;
    private @Nullable WeakLcoRef<Actment> actmentRef = null;
    private ContainerType containerType = ContainerType.BROWSE;
    private @Nullable Subject subject;

    private final List<ViewProxy> contextSentenceEnglish = new ArrayList<>();
    private final List<ViewProxy> contextSentenceJapanese = new ArrayList<>();
    private final ViewProxy addSynonymButton = new ViewProxy();
    private final ViewProxy resurrectButton = new ViewProxy();
    private final ViewProxy burnButton = new ViewProxy();
    private final ViewProxy onYomi = new ViewProxy();
    private final ViewProxy kunYomi = new ViewProxy();
    private final ViewProxy nanori = new ViewProxy();
    private final ViewProxy meaningDivider = new ViewProxy();
    private final ViewProxy meaningMnemonic = new ViewProxy();
    private final ViewProxy meaningHint = new ViewProxy();
    private final ViewProxy meaningNote = new ViewProxy();
    private final ViewProxy synonyms = new ViewProxy();
    private final ViewProxy legacyName = new ViewProxy();
    private final ViewProxy legacyMnemonic = new ViewProxy();
    private final ViewProxy readingDivider = new ViewProxy();
    private final ViewProxy readingMnemonic = new ViewProxy();
    private final ViewProxy readingHint = new ViewProxy();
    private final ViewProxy readingNote = new ViewProxy();
    private final ViewProxy partsOfSpeechDivider = new ViewProxy();
    private final ViewProxy partsOfSpeech = new ViewProxy();
    private final ViewProxy contextSentencesDivider = new ViewProxy();
    private final ViewProxy contextSentencesHeader = new ViewProxy();
    private final ViewProxy headline = new ViewProxy();

    private final ViewProxy tableSrsSystem = new ViewProxy();
    private final ViewProxy tableSrsStage = new ViewProxy();
    private final ViewProxy tableUnlockedAt = new ViewProxy();
    private final ViewProxy tableStartedAt = new ViewProxy();
    private final ViewProxy tablePassedAt = new ViewProxy();
    private final ViewProxy tableBurnedAt = new ViewProxy();
    private final ViewProxy tableResurrectedAt = new ViewProxy();
    private final ViewProxy tableAvailableAt = new ViewProxy();
    private final ViewProxy tablePercentageCorrect = new ViewProxy();
    private final ViewProxy tableMeaningCorrect = new ViewProxy();
    private final ViewProxy tableReadingCorrect = new ViewProxy();
    private final ViewProxy tableFrequency = new ViewProxy();
    private final ViewProxy tableJoyoGrade = new ViewProxy();
    private final ViewProxy tableJlptLevel = new ViewProxy();
    private final ViewProxy detailsDivider = new ViewProxy();
    private final ViewProxy detailsTable = new ViewProxy();

    private final ViewProxy componentsDivider = new ViewProxy();
    private final ViewProxy componentsHeader = new ViewProxy();
    private final ViewProxy componentsTable = new ViewProxy();
    private final ViewProxy amalgamationsDivider = new ViewProxy();
    private final ViewProxy amalgamationsHeader = new ViewProxy();
    private final ViewProxy amalgamationsTable = new ViewProxy();
    private final ViewProxy visuallySimilarsDivider = new ViewProxy();
    private final ViewProxy visuallySimilarsHeader = new ViewProxy();
    private final ViewProxy visuallySimilarsTable = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SubjectInfoView(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SubjectInfoView(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr the default style
     */
    public SubjectInfoView(final Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        safe(this::init);
    }

    /**
     * The type of container that has this view in it - has an impact on what information is shown.
     * @param containerType the type
     */
    public void setContainerType(final ContainerType containerType) {
        this.containerType = containerType;
    }

    /**
     * Get the subject info dump that applies to the current state.
     *
     * @return the dump
     */
    public SubjectInfoDump getSubjectInfoDump() {
        switch (containerType) {
            case LESSON_PRESENTATION: {
                @Nullable final SubjectInfoDump dump = FloatingUiState.showDumpStage;
                if (dump != null) {
                    return dump;
                }
                final SubjectInfoDump startingDump = GlobalSettings.Display.getHideLessonReadings()
                        ? SubjectInfoDump.HIDE_READING_RELATED
                        : SubjectInfoDump.ALL;
                FloatingUiState.showDumpStage = startingDump;
                return startingDump;
            }
            case ANSWERED_QUESTION: {
                @Nullable final SubjectInfoDump dump = FloatingUiState.showDumpStage;
                if (dump != null) {
                    return dump;
                }
                final @Nullable Question question = Session.getInstance().getCurrentQuestion();
                if (question == null) {
                    return SubjectInfoDump.ALL;
                }
                final SubjectInfoDump startingDump = GlobalSettings.Review.getInfoDump(question.getType(), Session.getInstance().isCorrect());
                FloatingUiState.showDumpStage = startingDump;
                return startingDump;
            }
            case BROWSE:
            default:
                return SubjectInfoDump.ALL;
        }
    }

    /**
     * Set the text size of all text views under this instance, but doesn't
     * dive into SubjectGridView instances.
     *
     * @param view the view, starting with the SubjectInfoView, and drilling down recursively
     * @param textSize the font size in SP
     */
    private static void setTextSize(final View view, final int textSize) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(textSize);
        }
        if (view instanceof SubjectInfoHeadlineView) {
            ((SubjectInfoHeadlineView) view).setTextSize(textSize);
            return;
        }
        if (view instanceof SubjectGridView) {
            return;
        }

        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            for (int i=0; i<viewGroup.getChildCount(); i++) {
                final @Nullable View child = viewGroup.getChildAt(i);
                if (child != null) {
                    setTextSize(child, textSize);
                }
            }
        }
    }

    /**
     * Initialize the instance.
     */
    private void init() {
        inflate(getContext(), R.layout.subject_info, this);
        setOrientation(VERTICAL);

        final int textSize = GlobalSettings.Font.getFontSizeSubjectInfo();
        setTextSize(this, textSize);

        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish0));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish1));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish2));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish3));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish4));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish5));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish6));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish7));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish8));
        contextSentenceEnglish.add(new ViewProxy(this, R.id.contextSentenceEnglish9));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese0));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese1));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese2));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese3));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese4));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese5));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese6));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese7));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese8));
        contextSentenceJapanese.add(new ViewProxy(this, R.id.contextSentenceJapanese9));

        addSynonymButton.setDelegate(this, R.id.addSynonymButton);
        resurrectButton.setDelegate(this, R.id.resurrectButton);
        burnButton.setDelegate(this, R.id.burnButton);
        onYomi.setDelegate(this, R.id.onYomi);
        kunYomi.setDelegate(this, R.id.kunYomi);
        nanori.setDelegate(this, R.id.nanori);
        meaningDivider.setDelegate(this, R.id.meaningDivider);
        meaningMnemonic.setDelegate(this, R.id.meaningMnemonic);
        meaningHint.setDelegate(this, R.id.meaningHint);
        meaningNote.setDelegate(this, R.id.meaningNote);
        synonyms.setDelegate(this, R.id.synonyms);
        legacyName.setDelegate(this, R.id.legacyName);
        legacyMnemonic.setDelegate(this, R.id.legacyMnemonic);
        readingDivider.setDelegate(this, R.id.readingDivider);
        readingMnemonic.setDelegate(this, R.id.readingMnemonic);
        readingHint.setDelegate(this, R.id.readingHint);
        readingNote.setDelegate(this, R.id.readingNote);
        partsOfSpeechDivider.setDelegate(this, R.id.partsOfSpeechDivider);
        partsOfSpeech.setDelegate(this, R.id.partsOfSpeech);
        contextSentencesDivider.setDelegate(this, R.id.contextSentencesDivider);
        contextSentencesHeader.setDelegate(this, R.id.contextSentencesHeader);
        headline.setDelegate(this, R.id.headline);

        tableSrsSystem.setDelegate(this, R.id.tableSrsSystem);
        tableSrsStage.setDelegate(this, R.id.tableSrsStage);
        tableUnlockedAt.setDelegate(this, R.id.tableUnlockedAt);
        tableStartedAt.setDelegate(this, R.id.tableStartedAt);
        tablePassedAt.setDelegate(this, R.id.tablePassedAt);
        tableBurnedAt.setDelegate(this, R.id.tableBurnedAt);
        tableResurrectedAt.setDelegate(this, R.id.tableResurrectedAt);
        tableAvailableAt.setDelegate(this, R.id.tableAvailableAt);
        tablePercentageCorrect.setDelegate(this, R.id.tablePercentageCorrect);
        tableMeaningCorrect.setDelegate(this, R.id.tableMeaningCorrect);
        tableReadingCorrect.setDelegate(this, R.id.tableReadingCorrect);
        tableFrequency.setDelegate(this, R.id.tableFrequency);
        tableJoyoGrade.setDelegate(this, R.id.tableJoyoGrade);
        tableJlptLevel.setDelegate(this, R.id.tableJlptLevel);
        detailsDivider.setDelegate(this, R.id.detailsDivider);
        detailsTable.setDelegate(this, R.id.detailsTable);

        componentsDivider.setDelegate(this, R.id.componentsDivider);
        componentsHeader.setDelegate(this, R.id.componentsHeader);
        componentsTable.setDelegate(this, R.id.componentsTable);
        if (GlobalSettings.Display.getSwapSimilarAndAmalgamations()) {
            visuallySimilarsDivider.setDelegate(this, R.id.amalgamationsDivider);
            visuallySimilarsHeader.setDelegate(this, R.id.amalgamationsHeader);
            visuallySimilarsTable.setDelegate(this, R.id.amalgamationsTable);
            amalgamationsDivider.setDelegate(this, R.id.visuallySimilarsDivider);
            amalgamationsHeader.setDelegate(this, R.id.visuallySimilarsHeader);
            amalgamationsTable.setDelegate(this, R.id.visuallySimilarsTable);
        }
        else {
            amalgamationsDivider.setDelegate(this, R.id.amalgamationsDivider);
            amalgamationsHeader.setDelegate(this, R.id.amalgamationsHeader);
            amalgamationsTable.setDelegate(this, R.id.amalgamationsTable);
            visuallySimilarsDivider.setDelegate(this, R.id.visuallySimilarsDivider);
            visuallySimilarsHeader.setDelegate(this, R.id.visuallySimilarsHeader);
            visuallySimilarsTable.setDelegate(this, R.id.visuallySimilarsTable);
        }

        SubjectChangeWatcher.getInstance().addListener(this);
    }

    /**
     * The the toolbar for the activity showing this info dump. If set,
     * this view will update the title based on the subject contents.
     *
     * @param toolbar the toolbar instance
     */
    public void setToolbar(final @Nullable Toolbar toolbar) {
        safe(() -> headline.setToolbar(toolbar));
    }

    /**
     * Set the maximum font size allowed for the subject text in the headline.
     *
     * @param maxFontSize the maximum size
     */
    public void setMaxFontSize(final int maxFontSize) {
        safe(() -> headline.setMaxFontSize(maxFontSize));
    }

    @Override
    public void onSubjectChange(@SuppressWarnings("ParameterHidesMemberVariable") final Subject subject) {
        if (subjectId == subject.getId()) {
            safe(() -> {
                this.subject = subject;
                layoutSubject(true);
            });
        }
    }

    @Override
    public boolean isInterestedInSubject(@SuppressWarnings("ParameterHidesMemberVariable") final long subjectId) {
        return this.subjectId == subjectId;
    }

    /**
     * Show a context sentence. Takes into account that the user may want the
     * english translation to be hidden until tapped.
     *
     * @param show show or hide it
     * @param sentences the list of all sentences
     * @param index the index 0-9 of the sentence to show
     * @param japanese the view containing the japanese text of the sentence
     * @param english the view containing the english text of the sentence
     */
    private static void showContextSentence(final boolean show, final List<ContextSentence> sentences,
                                            final int index, final ViewProxy japanese, final ViewProxy english) {
        if (index < sentences.size()) {
            final ContextSentence sentence = sentences.get(index);

            japanese.setText(sentence.getJapanese());
            japanese.setJapaneseLocale();
            if (GlobalSettings.Display.getHideSentenceTranslations()) {
                english.setText("-- Tap to reveal translation --");
                english.setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));
                english.setClickableAndNotFocusable(true);
                english.setOnClickListener(v -> safe(() -> {
                    english.setText("- " + sentence.getEnglish());
                    english.setClickableAndNotFocusable(false);
                    english.setBackgroundColor(Constants.TRANSPARENT);
                }));
            }
            else {
                english.setText("- " + sentence.getEnglish());
                english.setClickableAndNotFocusable(false);
                english.setBackgroundColor(Constants.TRANSPARENT);
            }
            japanese.setVisibility(show);
            english.setVisibility(show);
        }
        else {
            japanese.setVisibility(false);
            english.setVisibility(false);
        }
    }

    private void addSynonym(final @Nullable String value) {
        if (subject == null || isEmpty(value)) {
            return;
        }

        final List<String> list = new ArrayList<>(subject.getMeaningSynonyms());
        if (!list.contains(value)) {
            list.add(value);
        }

        subject.setMeaningSynonyms(list);
        synonyms.setText(subject.getMeaningSynonymsRichText());

        list.add(0, Long.toString(subject.getId()));
        list.add(1, orElse(subject.getMeaningNote(), ""));
        list.add(2, orElse(subject.getReadingNote(), ""));
        final String dataString;
        try {
            dataString = Converters.getObjectMapper().writeValueAsString(list);
        } catch (final JsonProcessingException e) {
            // This can't realistically happen.
            return;
        }
        JobRunnerService.schedule(SaveStudyMaterialJob.class, dataString);
    }

    private boolean onSynonymEditorAction(final DialogInterface alertDialog, final EditText synonym,
                                          final int actionId, final @Nullable KeyEvent event) {
        final boolean done = isTrue(synonym.getTag());

        if (done) {
            return true;
        }
        boolean ok = false;
        if (event == null && actionId == EditorInfo.IME_ACTION_DONE) {
            ok = true;
        }
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            ok = true;
        }
        if (ok) {
            synonym.setTag(true);
            try {
                alertDialog.dismiss();
            }
            catch (final Exception e) {
                //
            }
            final @Nullable CharSequence text = synonym.getText();
            if (text != null) {
                addSynonym(text.toString().trim());
            }
            return true;
        }
        return false;
    }

    private void prepareAddSynonymButton(final boolean showMeaningAnswers) {
        if (subject == null) {
            return;
        }

        addSynonymButton.setClickableAndNotFocusable(true);
        addSynonymButton.setVisibility(showMeaningAnswers);
        addSynonymButton.setOnClickListener(v -> safe(() -> {
            final EditText synonym = new EditText(getContext());
            synonym.setTag(false);

            final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Add synonym")
                    .setView(synonym)
                    .setNegativeButton("Cancel", (dialog, which) -> {})
                    .setPositiveButton("Save", (dialog, which) -> safe(() -> {
                        final @Nullable CharSequence text = synonym.getText();
                        if (text != null) {
                            addSynonym(text.toString().trim());
                        }
                    })).create();

            synonym.setOnEditorActionListener((v1, actionId, event) -> safe(
                    false, () -> onSynonymEditorAction(alertDialog, synonym, actionId, event)));

            alertDialog.show();
        }));
    }

    /**
     * Set the subject for this instance.
     *
     * @param actment the actment this view belongs to
     * @param newSubject the subject
     */
    public void setSubject(final Actment actment, final Subject newSubject) {
        safe(() -> {
            actmentRef = new WeakLcoRef<>(actment);
            final boolean sameSubject = subjectId == newSubject.getId();
            subject = newSubject;
            subjectId = subject.getId();
            layoutSubject(sameSubject);
        });
    }

    private void layoutSubjectImpl(final boolean sameSubject) {
        @Nullable Actment actment = null;
        if (actmentRef != null) {
            actment = actmentRef.getOrElse(null);
        }
        if (actment == null || subject == null) {
            return;
        }

        final boolean showMeaningAnswers = getSubjectInfoDump().getShowMeaningAnswers(Session.getInstance().getCurrentQuestion());
        final boolean showReadingAnswers = getSubjectInfoDump().getShowReadingAnswers(Session.getInstance().getCurrentQuestion());
        final boolean showMeaningRelated = getSubjectInfoDump().getShowMeaningRelated(Session.getInstance().getCurrentQuestion());
        final boolean showReadingRelated = getSubjectInfoDump().getShowReadingRelated(Session.getInstance().getCurrentQuestion());

        headline.setSubject(subject);

        // Kanji on'yomi, kun'yomi, nanori
        onYomi.setText(subject.getOnYomiRichText());
        onYomi.setJapaneseLocale();
        onYomi.setVisibility(showReadingAnswers && subject.hasOnYomi());
        kunYomi.setText(subject.getKunYomiRichText());
        kunYomi.setJapaneseLocale();
        kunYomi.setVisibility(showReadingAnswers && subject.hasKunYomi());
        nanori.setText(subject.getNanoriRichText());
        nanori.setJapaneseLocale();
        nanori.setVisibility(showReadingAnswers && subject.hasNanori());

        // Meaning mnemonic
        meaningDivider.setVisibility(subject.hasMeaningMnemonic() && (showMeaningRelated || showMeaningAnswers && subject.hasMeaningSynonyms()));
        meaningMnemonic.setText(subject.getMeaningMnemonicRichText());
        meaningMnemonic.setJapaneseLocale();
        meaningMnemonic.setLinkMovementMethod();
        meaningMnemonic.setVisibility(showMeaningRelated && subject.hasMeaningMnemonic());
        meaningHint.setText(subject.getMeaningHintRichText());
        meaningHint.setJapaneseLocale();
        meaningHint.setLinkMovementMethod();
        meaningHint.setVisibility(showMeaningRelated && subject.hasMeaningHint());
        legacyName.setText(subject.getLegacyNameRichText());
        legacyName.setVisibility(showMeaningRelated && GlobalSettings.Display.getShowLegacy() && subject.hasLegacy());
        legacyMnemonic.setText(subject.getLegacyMnemonicRichText());
        legacyMnemonic.setVisibility(showMeaningRelated && GlobalSettings.Display.getShowLegacy() && subject.hasLegacy());
        meaningNote.setText(subject.getMeaningNoteRichText());
        meaningNote.setJapaneseLocale();
        meaningNote.setLinkMovementMethod();
        meaningNote.setVisibility(showMeaningRelated && subject.hasMeaningNote());
        synonyms.setText(subject.getMeaningSynonymsRichText());
        synonyms.setJapaneseLocale();
        synonyms.setLinkMovementMethod();
        synonyms.setVisibility(showMeaningAnswers);
        prepareAddSynonymButton(showMeaningAnswers);

        // Reading mnemonic
        readingDivider.setVisibility(showReadingRelated && subject.hasReadingMnemonic());
        readingMnemonic.setText(subject.getReadingMnemonicRichText());
        readingMnemonic.setJapaneseLocale();
        readingMnemonic.setLinkMovementMethod();
        readingMnemonic.setVisibility(showReadingRelated && subject.hasReadingMnemonic());
        readingHint.setText(subject.getReadingHintRichText());
        readingHint.setJapaneseLocale();
        readingHint.setLinkMovementMethod();
        readingHint.setVisibility(showReadingRelated && subject.hasReadingHint());
        readingNote.setText(subject.getReadingNoteRichText());
        readingNote.setJapaneseLocale();
        readingNote.setLinkMovementMethod();
        readingNote.setVisibility(showReadingRelated && subject.hasReadingNote());

        // Parts of speech
        partsOfSpeechDivider.setVisibility(showMeaningRelated && subject.hasPartsOfSpeech());
        partsOfSpeech.setText(subject.getPartsOfSpeechRichText());
        partsOfSpeech.setVisibility(showMeaningRelated && subject.hasPartsOfSpeech());

        // Context sentences
        final boolean hasContextSentences = showMeaningRelated && subject.hasContextSentences();
        contextSentencesDivider.setVisibility(hasContextSentences);
        contextSentencesHeader.setVisibility(hasContextSentences);
        final List<ContextSentence> sentences = subject.getContextSentences();
        for (int i=0; i<contextSentenceJapanese.size(); i++) {
            showContextSentence(hasContextSentences, sentences, i, contextSentenceJapanese.get(i), contextSentenceEnglish.get(i));
        }

        // Bottom table
        tableSrsSystem.setText(subject.getSrsSystem().getName());
        tableSrsStage.setText(subject.getSrsStage().getName());
        tableUnlockedAt.setText(subject.getFormattedUnlockedAt());
        tableUnlockedAt.setParentVisibility(subject.getUnlockedAt() != 0);
        tableStartedAt.setText(subject.getFormattedStartedAt());
        tableStartedAt.setParentVisibility(subject.getStartedAt() != 0);
        tablePassedAt.setText(subject.getFormattedPassedAt());
        tablePassedAt.setParentVisibility(subject.getPassedAt() != null);
        tableBurnedAt.setText(subject.getFormattedBurnedAt());
        tableBurnedAt.setParentVisibility(subject.getBurnedAt() != 0);
        tableResurrectedAt.setText(subject.getFormattedResurrectedAt());
        tableResurrectedAt.setParentVisibility(subject.getResurrectedAt() != 0);
        tableAvailableAt.setText(subject.getFormattedAvailableAt());
        tableAvailableAt.setParentVisibility(subject.getAvailableAt() != null);

        final int meaningTotal = subject.getMeaningCorrect() + subject.getMeaningIncorrect();
        final int readingTotal = subject.getReadingCorrect() + subject.getReadingIncorrect();
        final int total = meaningTotal + readingTotal;

        tablePercentageCorrect.setTextFormat("%d%%", subject.getPercentageCorrect());
        tablePercentageCorrect.setParentVisibility(total > 0);

        if (meaningTotal > 0) {
            tableMeaningCorrect.setTextFormat("%d%%, max streak %d, current streak %s",
                    (100*subject.getMeaningCorrect()) / meaningTotal, subject.getMeaningMaxStreak(),
                    subject.getMeaningCurrentStreak());
        }
        tableMeaningCorrect.setParentVisibility(meaningTotal > 0);

        if (readingTotal > 0) {
            tableReadingCorrect.setTextFormat("%d%%, max streak %d, current streak %s",
                    (100*subject.getReadingCorrect()) / readingTotal, subject.getReadingMaxStreak(),
                    subject.getReadingCurrentStreak());
        }
        tableReadingCorrect.setParentVisibility(readingTotal > 0);

        if (subject.getFrequency() > 0) {
            tableFrequency.setText(subject.getFrequency());
        }
        tableFrequency.setParentVisibility(subject.getFrequency() > 0);

        if (subject.getJoyoGrade() > 0) {
            tableJoyoGrade.setText(subject.getJoyoGradeAsString());
        }
        tableJoyoGrade.setParentVisibility(subject.getJoyoGrade() > 0);

        if (subject.getJlptLevel() > 0) {
            tableJlptLevel.setText(subject.getJlptLevelAsString());
        }
        tableJlptLevel.setParentVisibility(subject.getJlptLevel() > 0);

        detailsDivider.setVisibility(showMeaningAnswers || showReadingAnswers);
        detailsTable.setVisibility(showMeaningAnswers || showReadingAnswers);

        // Components table
        final boolean hasComponents = showMeaningRelated && subject.hasComponents();
        componentsDivider.setVisibility(hasComponents);
        componentsHeader.setText(subject.getType().getComponentsHeaderText());
        componentsHeader.setVisibility(hasComponents);
        componentsTable.setVisibility(hasComponents);
        if (!hasComponents || !sameSubject) {
            componentsTable.removeAllViews();
        }
        if (hasComponents) {
            componentsTable.setSubjectIds(actment, subject.getComponentSubjectIds(), true, showReadingRelated);
        }

        // VisuallySimilars table
        final boolean hasVisuallySimilars = showMeaningRelated && subject.hasVisuallySimilar();
        visuallySimilarsDivider.setVisibility(hasVisuallySimilars);
        visuallySimilarsHeader.setVisibility(hasVisuallySimilars);
        visuallySimilarsHeader.setText("Visually similar:");
        visuallySimilarsTable.setVisibility(hasVisuallySimilars);
        if (!hasVisuallySimilars || !sameSubject) {
            visuallySimilarsTable.removeAllViews();
        }
        if (hasVisuallySimilars) {
            visuallySimilarsTable.setSubjectIds(actment, subject.getVisuallySimilarSubjectIds(), true, showReadingRelated);
        }

        // Amalgamations table
        final boolean hasAmalgamations = showMeaningRelated && subject.hasAmalgamations();
        amalgamationsDivider.setVisibility(hasAmalgamations);
        amalgamationsHeader.setVisibility(hasAmalgamations);
        amalgamationsHeader.setText("Used in:");
        amalgamationsTable.setVisibility(hasAmalgamations);
        if (!hasAmalgamations || !sameSubject) {
            amalgamationsTable.removeAllViews();
        }
        if (hasAmalgamations) {
            amalgamationsTable.setSubjectIds(actment, subject.getAmalgamationSubjectIds(), true, showReadingRelated);
        }

        if (subject.isResurrectable() && !isEmpty(GlobalSettings.Api.getWebPassword())) {
            resurrectButton.setVisibility(true);
            resurrectButton.setOnClickListener(v -> goToResurrectActivity(subject.getId()));
        }
        else {
            resurrectButton.setVisibility(false);
        }

        if (subject.isBurnable() && !isEmpty(GlobalSettings.Api.getWebPassword())) {
            burnButton.setVisibility(true);
            burnButton.setOnClickListener(v -> goToBurnActivity(subject.getId()));
        }
        else {
            burnButton.setVisibility(false);
        }
    }

    /**
     * Layout the current subject.
     *
     * @param sameSubject true if this is a re-layout of the same subject
     */
    public void layoutSubject(final boolean sameSubject) {
        safe(() -> layoutSubjectImpl(sameSubject));
    }

    private void goToResurrectActivity(final long id) {
        safe(() -> {
            final Intent intent = new Intent(getContext(), ResurrectActivity.class);
            intent.putExtra("ids", new long[] {id});
            getContext().startActivity(intent);
        });
    }

    private void goToBurnActivity(final long id) {
        safe(() -> {
            final Intent intent = new Intent(getContext(), BurnActivity.class);
            intent.putExtra("ids", new long[] {id});
            getContext().startActivity(intent);
        });
    }

    /**
     * The type of container that has this view in it - has an impact on what information is shown.
     */
    public enum ContainerType {
        /**
         * Container is the lesson presentation fragment.
         */
        LESSON_PRESENTATION,

        /**
         * Container is the answered quiz fragment or the Anki-mode fragment.
         */
        ANSWERED_QUESTION,

        /**
         * Container is the subject info activity.
         */
        BROWSE
    }
}
