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
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.enums.SearchSortOrder;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.SrsSystemRepository;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.TextUtil.getRomanNumeral;

/**
 * Custom view that shows the subject info dump.
 */
public final class AdvancedSearchFormView extends TableLayout {
    private static final Logger LOGGER = Logger.get(AdvancedSearchFormView.class);

    private String searchButtonLabel = "Search";
    private boolean sortOrderVisibility = true;

    private final List<ViewProxy> srsStageSwitches = new ArrayList<>();
    private final List<ViewProxy> srsStageLabels = new ArrayList<>();
    private final List<ViewProxy> itemTypeSwitches = new ArrayList<>();
    private final List<ViewProxy> jlptLevelSwitches = new ArrayList<>();
    private final List<ViewProxy> joyoGradeSwitches = new ArrayList<>();

    private final ViewProxy searchButton1 = new ViewProxy();
    private final ViewProxy searchButton2 = new ViewProxy();
    private final ViewProxy minLevel = new ViewProxy();
    private final ViewProxy maxLevel = new ViewProxy();
    private final ViewProxy minFrequency = new ViewProxy();
    private final ViewProxy maxFrequency = new ViewProxy();
    private final ViewProxy limitToLeeches = new ViewProxy();
    private final ViewProxy reviewLessHours = new ViewProxy();
    private final ViewProxy reviewMoreHours = new ViewProxy();
    private final ViewProxy incorrectHours = new ViewProxy();
    private final ViewProxy sortOrder = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public AdvancedSearchFormView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public AdvancedSearchFormView(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the instance.
     */
    private void init() {
        try {
            inflate(getContext(), R.layout.advanced_search, this);
            setShrinkAllColumns(true);

            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage00));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage01));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage02));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage03));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage04));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage05));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage06));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage07));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage08));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage09));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage10));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage11));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage12));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage13));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage14));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage15));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage16));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage17));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage18));
            srsStageSwitches.add(new ViewProxy(this, R.id.srsStage19));

            srsStageLabels.add(new ViewProxy(this, R.id.srsStage00Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage01Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage02Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage03Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage04Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage05Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage06Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage07Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage08Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage09Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage10Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage11Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage12Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage13Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage14Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage15Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage16Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage17Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage18Label));
            srsStageLabels.add(new ViewProxy(this, R.id.srsStage19Label));

            itemTypeSwitches.add(new ViewProxy(this, R.id.itemTypeRadical));
            itemTypeSwitches.add(new ViewProxy(this, R.id.itemTypeKanji));
            itemTypeSwitches.add(new ViewProxy(this, R.id.itemTypeVocabulary));

            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelNone));
            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelN5));
            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelN4));
            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelN3));
            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelN2));
            jlptLevelSwitches.add(new ViewProxy(this, R.id.jlptLevelN1));

            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGradeNone));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade1));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade2));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade3));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade4));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade5));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade6));
            joyoGradeSwitches.add(new ViewProxy(this, R.id.joyoGrade7));

            searchButton1.setDelegate(this, R.id.searchButton1);
            searchButton2.setDelegate(this, R.id.searchButton2);
            minLevel.setDelegate(this, R.id.minLevel);
            maxLevel.setDelegate(this, R.id.maxLevel);
            minFrequency.setDelegate(this, R.id.minFrequency);
            maxFrequency.setDelegate(this, R.id.maxFrequency);
            limitToLeeches.setDelegate(this, R.id.limitToLeeches);
            reviewLessHours.setDelegate(this, R.id.reviewLessHours);
            reviewMoreHours.setDelegate(this, R.id.reviewMoreHours);
            incorrectHours.setDelegate(this, R.id.incorrectHours);
            sortOrder.setDelegate(this, R.id.sortOrder);

            searchButton1.setText(searchButtonLabel);
            searchButton2.setText(searchButtonLabel);

            final SpinnerAdapter adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinner_item, SearchSortOrder.descriptionValues());
            sortOrder.setAdapter(adapter);
            sortOrder.setSelection(0);
            sortOrder.setParentVisibility(sortOrderVisibility);

            srsStageLabels.get(0).setText("Locked");
            srsStageSwitches.get(0).setTag(R.id.advancedSearchSwitchTag, "locked");
            srsStageSwitches.get(0).setParentVisibility(true);
            srsStageLabels.get(1).setText("Initiate");
            srsStageSwitches.get(1).setTag(R.id.advancedSearchSwitchTag, "initial");
            srsStageSwitches.get(1).setParentVisibility(true);
            int index = 2;

            final int maxNumApprenticeStages = SrsSystemRepository.getMaxNumApprenticeStages();
            for (int i=0; i<maxNumApprenticeStages; i++) {
                srsStageLabels.get(index).setText("Apprentice " + getRomanNumeral(i+1));
                srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, "prepass:" + i);
                srsStageSwitches.get(index).setParentVisibility(true);
                index++;
            }

            final int maxNumGuruStages = SrsSystemRepository.getMaxNumGuruStages();
            for (int i=0; i<maxNumGuruStages; i++) {
                srsStageLabels.get(index).setText("Guru " + getRomanNumeral(i+1));
                srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, "pass:" + i);
                srsStageSwitches.get(index).setParentVisibility(true);
                index++;
            }

            srsStageLabels.get(index).setText("Master");
            srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, "master");
            srsStageSwitches.get(index).setParentVisibility(true);
            index++;

            srsStageLabels.get(index).setText("Enlightened");
            srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, "enlightened");
            srsStageSwitches.get(index).setParentVisibility(true);
            index++;

            srsStageLabels.get(index).setText("Burned");
            srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, "burned");
            srsStageSwitches.get(index).setParentVisibility(true);
            index++;

            while (index < 20) {
                srsStageSwitches.get(index).setTag(R.id.advancedSearchSwitchTag, null);
                srsStageSwitches.get(index).setParentVisibility(false);
                index++;
            }

            itemTypeSwitches.get(0).setTag(R.id.advancedSearchSwitchTag, SubjectType.WANIKANI_RADICAL);
            itemTypeSwitches.get(1).setTag(R.id.advancedSearchSwitchTag, SubjectType.WANIKANI_KANJI);
            itemTypeSwitches.get(2).setTag(R.id.advancedSearchSwitchTag, SubjectType.WANIKANI_VOCAB);

            jlptLevelSwitches.get(0).setTag(R.id.advancedSearchSwitchTag, 0);
            jlptLevelSwitches.get(1).setTag(R.id.advancedSearchSwitchTag, 5);
            jlptLevelSwitches.get(2).setTag(R.id.advancedSearchSwitchTag, 4);
            jlptLevelSwitches.get(3).setTag(R.id.advancedSearchSwitchTag, 3);
            jlptLevelSwitches.get(4).setTag(R.id.advancedSearchSwitchTag, 2);
            jlptLevelSwitches.get(5).setTag(R.id.advancedSearchSwitchTag, 1);

            joyoGradeSwitches.get(0).setTag(R.id.advancedSearchSwitchTag, 0);
            joyoGradeSwitches.get(1).setTag(R.id.advancedSearchSwitchTag, 1);
            joyoGradeSwitches.get(2).setTag(R.id.advancedSearchSwitchTag, 2);
            joyoGradeSwitches.get(3).setTag(R.id.advancedSearchSwitchTag, 3);
            joyoGradeSwitches.get(4).setTag(R.id.advancedSearchSwitchTag, 4);
            joyoGradeSwitches.get(5).setTag(R.id.advancedSearchSwitchTag, 5);
            joyoGradeSwitches.get(6).setTag(R.id.advancedSearchSwitchTag, 6);
            joyoGradeSwitches.get(7).setTag(R.id.advancedSearchSwitchTag, 7);
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Label for the search buttons at the top and bottom of the form.
     *
     * @param searchButtonLabel the label
     */
    public void setSearchButtonLabel(final String searchButtonLabel) {
        this.searchButtonLabel = searchButtonLabel;
        searchButton1.setText(searchButtonLabel);
        searchButton2.setText(searchButtonLabel);
    }

    /**
     * Visibility for the sort order option in the form.
     *
     * @param visibility true if the sort order option should be shown
     */
    public void setSortOrderVisibility(final boolean visibility) {
        sortOrderVisibility = visibility;
        sortOrder.setParentVisibility(visibility);
    }

    /**
     * Extract the current contents of the form as an object.
     *
     * @return the search parameters
     */
    public AdvancedSearchParameters extractParameters() {
        final AdvancedSearchParameters parameters = new AdvancedSearchParameters();

        final String minLevelStr = minLevel.getText();
        if (!minLevelStr.isEmpty()) {
            try {
                parameters.minLevel = Integer.parseInt(minLevelStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String maxLevelStr = maxLevel.getText();
        if (!maxLevelStr.isEmpty()) {
            try {
                parameters.maxLevel = Integer.parseInt(maxLevelStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String minFrequencyStr = minFrequency.getText();
        if (!minFrequencyStr.isEmpty()) {
            try {
                parameters.minFrequency = Integer.parseInt(minFrequencyStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String maxFrequencyStr = maxFrequency.getText();
        if (!maxFrequencyStr.isEmpty()) {
            try {
                parameters.maxFrequency = Integer.parseInt(maxFrequencyStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String reviewLessHoursStr = reviewLessHours.getText();
        if (!reviewLessHoursStr.isEmpty()) {
            try {
                parameters.upcomingReviewLessThan = Integer.parseInt(reviewLessHoursStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String reviewMoreHoursStr = reviewMoreHours.getText();
        if (!reviewMoreHoursStr.isEmpty()) {
            try {
                parameters.upcomingReviewMoreThan = Integer.parseInt(reviewMoreHoursStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        final String incorrectHoursStr = incorrectHours.getText();
        if (!incorrectHoursStr.isEmpty()) {
            try {
                parameters.incorrectAnswerWithin = Integer.parseInt(incorrectHoursStr, 10);
            } catch (final Exception e) {
                LOGGER.uerr(e);
            }
        }

        parameters.leechesOnly = limitToLeeches.isChecked();

        final @Nullable Object selectedSortOrder = sortOrder.getSelection();
        if (selectedSortOrder instanceof String) {
            parameters.sortOrder = SearchSortOrder.forDescription((String) selectedSortOrder, SearchSortOrder.TYPE);
        }

        for (final ViewProxy stage: srsStageSwitches) {
            final @Nullable Object tag = stage.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof String && stage.isChecked()) {
                parameters.srsStages.add((String) tag);
            }
        }

        for (final ViewProxy itemType: itemTypeSwitches) {
            final @Nullable Object tag = itemType.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof SubjectType && itemType.isChecked()) {
                parameters.itemTypes.add((SubjectType) tag);
            }
        }

        for (final ViewProxy level: jlptLevelSwitches) {
            final @Nullable Object tag = level.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof Integer && level.isChecked()) {
                parameters.jlptLevels.add((Integer) tag);
            }
        }

        for (final ViewProxy grade: joyoGradeSwitches) {
            final @Nullable Object tag = grade.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof Integer && grade.isChecked()) {
                parameters.joyoGrades.add((Integer) tag);
            }
        }

        return parameters;
    }

    /**
     * Inject the supplied parameters into the form.
     *
     * @param parameters the search parameters
     */
    public void injectParameters(final AdvancedSearchParameters parameters) {
        if (parameters.minLevel == null) {
            minLevel.setText("");
        }
        else {
            minLevel.setText(parameters.minLevel);
        }

        if (parameters.maxLevel == null) {
            maxLevel.setText("");
        }
        else {
            maxLevel.setText(parameters.maxLevel);
        }

        if (parameters.minFrequency == null) {
            minFrequency.setText("");
        }
        else {
            minFrequency.setText(parameters.minFrequency);
        }

        if (parameters.maxFrequency == null) {
            maxFrequency.setText("");
        }
        else {
            maxFrequency.setText(parameters.maxFrequency);
        }

        if (parameters.upcomingReviewLessThan == null) {
            reviewLessHours.setText("");
        }
        else {
            reviewLessHours.setText(parameters.upcomingReviewLessThan);
        }

        if (parameters.upcomingReviewMoreThan == null) {
            reviewMoreHours.setText("");
        }
        else {
            reviewMoreHours.setText(parameters.upcomingReviewMoreThan);
        }

        if (parameters.incorrectAnswerWithin == null) {
            incorrectHours.setText("");
        }
        else {
            incorrectHours.setText(parameters.incorrectAnswerWithin);
        }

        limitToLeeches.setChecked(parameters.leechesOnly);

        sortOrder.setSelection(parameters.sortOrder.ordinal());

        for (final ViewProxy stage: srsStageSwitches) {
            final @Nullable Object tag = stage.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof String) {
                stage.setChecked(parameters.srsStages.contains(tag));
            }
        }

        for (final ViewProxy itemType: itemTypeSwitches) {
            final @Nullable Object tag = itemType.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof SubjectType) {
                itemType.setChecked(parameters.itemTypes.contains(tag));
            }
        }

        for (final ViewProxy level: jlptLevelSwitches) {
            final @Nullable Object tag = level.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof Integer) {
                level.setChecked(parameters.jlptLevels.contains(tag));
            }
        }

        for (final ViewProxy grade: joyoGradeSwitches) {
            final @Nullable Object tag = grade.getTag(R.id.advancedSearchSwitchTag);
            if (tag instanceof Integer) {
                grade.setChecked(parameters.joyoGrades.contains(tag));
            }
        }
    }
}
