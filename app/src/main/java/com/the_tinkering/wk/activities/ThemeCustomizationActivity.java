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

package com.the_tinkering.wk.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import com.airbnb.lottie.SimpleColorFilter;
import com.madrapps.pikolo.listeners.OnColorSelectionListener;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.proxy.ViewProxy;

import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.NUM_THEME_CUSTOMIZATION_OPTIONS;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Activity for theme customization.
 */
public final class ThemeCustomizationActivity extends AbstractActivity {
    private static final String EXTRA1 = "\nUsed on the timeline chart, the SRS breakdown, and the Post-60 progression bar";
    private static final String EXTRA2 = "\nUsed on the Post-60 progression bar with subsections enabled";
    private static final String EXTRA3 = "\nUsed on the Level progression chart";

    private final ViewProxy[] selectionViews = new ViewProxy[NUM_THEME_CUSTOMIZATION_OPTIONS];
    private final ViewProxy[] selectionHighlights = new ViewProxy[NUM_THEME_CUSTOMIZATION_OPTIONS];
    private final String[] selectionDescriptions = new String[NUM_THEME_CUSTOMIZATION_OPTIONS];
    private int selection = 0;
    private final int[] chosenColors = new int[NUM_THEME_CUSTOMIZATION_OPTIONS];
    private final ViewProxy selectionDescription = new ViewProxy();
    private final ViewProxy rgbValues = new ViewProxy();
    private final ViewProxy resetColorButton = new ViewProxy();
    private final ViewProxy colorPicker = new ViewProxy();
    private final ViewProxy colorPickerPreview = new ViewProxy();

    /**
     * The constructor.
     */
    public ThemeCustomizationActivity() {
        super(R.layout.activity_theme_customization, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        selectionViews[0] = new ViewProxy(this, R.id.radicalSample);
        selectionViews[1] = new ViewProxy(this, R.id.kanjiSample);
        selectionViews[2] = new ViewProxy(this, R.id.vocabularySample);
        selectionViews[3] = new ViewProxy(this, R.id.lockedSample);
        selectionViews[4] = new ViewProxy(this, R.id.initiateSample);
        selectionViews[5] = new ViewProxy(this, R.id.apprenticeSample);
        selectionViews[6] = new ViewProxy(this, R.id.guruSample);
        selectionViews[7] = new ViewProxy(this, R.id.masterSample);
        selectionViews[8] = new ViewProxy(this, R.id.enlightenedSample);
        selectionViews[9] = new ViewProxy(this, R.id.burnedSample);
        selectionViews[10] = new ViewProxy(this, R.id.prePassed1Sample);
        selectionViews[11] = new ViewProxy(this, R.id.prePassed2Sample);
        selectionViews[12] = new ViewProxy(this, R.id.prePassed3Sample);
        selectionViews[13] = new ViewProxy(this, R.id.prePassed4Sample);
        selectionViews[14] = new ViewProxy(this, R.id.passed1Sample);
        selectionViews[15] = new ViewProxy(this, R.id.passed2Sample);
        selectionViews[16] = new ViewProxy(this, R.id.levelProgression1Sample);
        selectionViews[17] = new ViewProxy(this, R.id.levelProgression2Sample);
        selectionViews[18] = new ViewProxy(this, R.id.levelProgression3Sample);
        selectionViews[19] = new ViewProxy(this, R.id.levelProgression4Sample);
        selectionViews[20] = new ViewProxy(this, R.id.levelProgression5Sample);
        selectionViews[21] = new ViewProxy(this, R.id.levelProgression6Sample);
        selectionViews[22] = new ViewProxy(this, R.id.levelProgression7Sample);
        selectionViews[23] = new ViewProxy(this, R.id.levelProgression8Sample);
        selectionViews[24] = new ViewProxy(this, R.id.levelProgression9Sample);
        selectionViews[25] = new ViewProxy(this, R.id.levelProgression10Sample);
        selectionViews[26] = new ViewProxy(this, R.id.ankiShowAnswerSample);
        selectionViews[27] = new ViewProxy(this, R.id.ankiNextSample);
        selectionViews[28] = new ViewProxy(this, R.id.ankiCorrectSample);
        selectionViews[29] = new ViewProxy(this, R.id.ankiIncorrectSample);
        selectionViews[30] = new ViewProxy(this, R.id.ankiTextSample);
        selectionViews[31] = new ViewProxy(this, R.id.ankiAnswerSample);

        selectionHighlights[0] = new ViewProxy(this, R.id.radicalSampleHighlight);
        selectionHighlights[1] = new ViewProxy(this, R.id.kanjiSampleHighlight);
        selectionHighlights[2] = new ViewProxy(this, R.id.vocabularySampleHighlight);
        selectionHighlights[3] = new ViewProxy(this, R.id.lockedSampleHighlight);
        selectionHighlights[4] = new ViewProxy(this, R.id.initiateSampleHighlight);
        selectionHighlights[5] = new ViewProxy(this, R.id.apprenticeSampleHighlight);
        selectionHighlights[6] = new ViewProxy(this, R.id.guruSampleHighlight);
        selectionHighlights[7] = new ViewProxy(this, R.id.masterSampleHighlight);
        selectionHighlights[8] = new ViewProxy(this, R.id.enlightenedSampleHighlight);
        selectionHighlights[9] = new ViewProxy(this, R.id.burnedSampleHighlight);
        selectionHighlights[10] = new ViewProxy(this, R.id.prePassed1SampleHighlight);
        selectionHighlights[11] = new ViewProxy(this, R.id.prePassed2SampleHighlight);
        selectionHighlights[12] = new ViewProxy(this, R.id.prePassed3SampleHighlight);
        selectionHighlights[13] = new ViewProxy(this, R.id.prePassed4SampleHighlight);
        selectionHighlights[14] = new ViewProxy(this, R.id.passed1SampleHighlight);
        selectionHighlights[15] = new ViewProxy(this, R.id.passed2SampleHighlight);
        selectionHighlights[16] = new ViewProxy(this, R.id.levelProgression1SampleHighlight);
        selectionHighlights[17] = new ViewProxy(this, R.id.levelProgression2SampleHighlight);
        selectionHighlights[18] = new ViewProxy(this, R.id.levelProgression3SampleHighlight);
        selectionHighlights[19] = new ViewProxy(this, R.id.levelProgression4SampleHighlight);
        selectionHighlights[20] = new ViewProxy(this, R.id.levelProgression5SampleHighlight);
        selectionHighlights[21] = new ViewProxy(this, R.id.levelProgression6SampleHighlight);
        selectionHighlights[22] = new ViewProxy(this, R.id.levelProgression7SampleHighlight);
        selectionHighlights[23] = new ViewProxy(this, R.id.levelProgression8SampleHighlight);
        selectionHighlights[24] = new ViewProxy(this, R.id.levelProgression9SampleHighlight);
        selectionHighlights[25] = new ViewProxy(this, R.id.levelProgression10SampleHighlight);
        selectionHighlights[26] = new ViewProxy(this, R.id.ankiShowAnswerSampleHighlight);
        selectionHighlights[27] = new ViewProxy(this, R.id.ankiNextSampleHighlight);
        selectionHighlights[28] = new ViewProxy(this, R.id.ankiCorrectSampleHighlight);
        selectionHighlights[29] = new ViewProxy(this, R.id.ankiIncorrectSampleHighlight);
        selectionHighlights[30] = new ViewProxy(this, R.id.ankiTextSampleHighlight);
        selectionHighlights[31] = new ViewProxy(this, R.id.ankiAnswerSampleHighlight);

        selectionDescriptions[0] = "The identifying colour for Radical subjects";
        selectionDescriptions[1] = "The identifying colour for Kanji subjects";
        selectionDescriptions[2] = "The identifying colour for Vocabulary subjects";
        selectionDescriptions[3] = "The segment colour for Locked items" + EXTRA1;
        selectionDescriptions[4] = "The segment colour for Initiate items (not started yet)" + EXTRA1;
        selectionDescriptions[5] = "The segment colour for Apprentice items" + EXTRA1;
        selectionDescriptions[6] = "The segment colour for Guru items" + EXTRA1;
        selectionDescriptions[7] = "The segment colour for Master items" + EXTRA1;
        selectionDescriptions[8] = "The segment colour for Enlightened items" + EXTRA1;
        selectionDescriptions[9] = "The segment colour for Burned items" + EXTRA1;
        selectionDescriptions[10] = "The segment colour for Apprentice I items" + EXTRA2;
        selectionDescriptions[11] = "The segment colour for Apprentice II items" + EXTRA2;
        selectionDescriptions[12] = "The segment colour for Apprentice III items" + EXTRA2;
        selectionDescriptions[13] = "The segment colour for Apprentice IV items" + EXTRA2;
        selectionDescriptions[14] = "The segment colour for Guru I items" + EXTRA2;
        selectionDescriptions[15] = "The segment colour for Guru II items" + EXTRA2;
        selectionDescriptions[16] = "The segment colour for Passed items" + EXTRA3;
        selectionDescriptions[17] = "The segment colour for stage 7 (Apprentice IV)" + EXTRA3;
        selectionDescriptions[18] = "The segment colour for stage 6" + EXTRA3;
        selectionDescriptions[19] = "The segment colour for stage 5 (Apprentice III)" + EXTRA3;
        selectionDescriptions[20] = "The segment colour for stage 4" + EXTRA3;
        selectionDescriptions[21] = "The segment colour for stage 3 (Apprentice II)" + EXTRA3;
        selectionDescriptions[22] = "The segment colour for stage 2" + EXTRA3;
        selectionDescriptions[23] = "The segment colour for stage 1 (Apprentice I)" + EXTRA3;
        selectionDescriptions[24] = "The segment colour for Initiate items" + EXTRA3;
        selectionDescriptions[25] = "The segment colour for Locked items" + EXTRA3;
        selectionDescriptions[26] = "The background colour for the Anki mode \"Show Answer\" button";
        selectionDescriptions[27] = "The background colour for the Anki mode \"Next\" button";
        selectionDescriptions[28] = "The background colour for the Anki mode \"Correct\" button";
        selectionDescriptions[29] = "The background colour for the Anki mode \"Incorrect\" button";
        selectionDescriptions[30] = "The text colour for the Anki mode buttons and answer text";
        selectionDescriptions[31] = "The background colour for the Anki mode answer text";

        selectionDescription.setDelegate(this, R.id.selectionDescription);
        rgbValues.setDelegate(this, R.id.rgbValues);
        resetColorButton.setDelegate(this, R.id.resetColorButton);
        colorPicker.setDelegate(this, R.id.colorPicker);
        colorPickerPreview.setDelegate(this, R.id.colorPickerPreview);

        resetColorButton.setOnClickListener(v -> resetColor());

        final List<Integer> prefColors = GlobalSettings.Display.getThemeCustomizations(ActiveTheme.getCurrentTheme());
        for (int i=0; i<chosenColors.length && i<prefColors.size(); i++) {
            chosenColors[i] = prefColors.get(i);
        }

        colorPicker.setColorSelectionListener(new OnColorSelectionListener() {
            @Override
            public void onColorSelected(final int i) {
                safe(() -> setColor(selection, i, true));
            }

            @Override
            public void onColorSelectionStart(final int i) {
                //
            }

            @Override
            public void onColorSelectionEnd(final int i) {
                safe(() -> saveColors());
            }
        });

        final View.OnClickListener listener = v -> safe(() -> {
            final int index = (int) v.getTag(R.id.themeCustomizationIndex);
            setSelection(index);
        });

        for (int i=0; i<NUM_THEME_CUSTOMIZATION_OPTIONS; i++) {
            selectionViews[i].setTag(R.id.themeCustomizationIndex, i);
            selectionViews[i].setOnClickListener(listener);
        }

        for (int i=0; i<NUM_THEME_CUSTOMIZATION_OPTIONS; i++) {
            updateSelection(i);
        }

        if (savedInstanceState == null) {
            setSelection(0);
        }
        else {
            setSelection(savedInstanceState.getInt("selection", 0));
        }
    }

    @Override
    protected void onResumeLocal() {
        //
    }

    @Override
    protected void onPauseLocal() {
        //
    }

    @Override
    protected void enableInteractionLocal() {
        //
    }

    @Override
    protected void disableInteractionLocal() {
        //
    }

    @Override
    protected boolean showWithoutApiKey() {
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final PersistableBundle outPersistentState) {
        safe(() -> {
            super.onSaveInstanceState(outState, outPersistentState);
            outState.putInt("selection", selection);
        });
    }

    private static int getBaseColor(final int index) {
        if (index < 3) {
            return ActiveTheme.getBaseSubjectTypeBucketColors()[index];
        }
        if (index < 10) {
            return ActiveTheme.getBaseShallowStageBucketColors()[index-3];
        }
        if (index < 14) {
            return ActiveTheme.getBasePrePassedBucketColors()[index-10];
        }
        if (index < 16) {
            return ActiveTheme.getBasePassedBucketColors()[index-14];
        }
        if (index < 26) {
            return ActiveTheme.getBaseLevelProgressionBucketColors()[index-16];
        }
        if (index < 32) {
            return ActiveTheme.getBaseAnkiColors()[index-26];
        }
        return 0;
    }

    private void updateSelection(final int index) {
        final int chosenColor = chosenColors[index];

        if (index < 3) {
            final int textColor = (ActiveTheme.getCurrentTheme().hasIdentBackground() || chosenColor == 0)
                    ? ActiveTheme.getBaseSubjectTypeTextColors()[index] : chosenColor;
            final int backgroundColor = (!ActiveTheme.getCurrentTheme().hasIdentBackground() || chosenColor == 0)
                    ? ActiveTheme.getBaseSubjectTypeBackgroundColors()[index] : chosenColor;
            selectionViews[index].setTextColor(textColor);
            selectionViews[index].setBackgroundColor(backgroundColor);
        }
        else if (index < 10) {
            final int backgroundColor = chosenColor == 0 ? ActiveTheme.getBaseShallowStageBucketColors()[index-3] : chosenColor;
            selectionViews[index].setBackgroundColor(backgroundColor);
        }
        else if (index < 14) {
            final int backgroundColor = chosenColor == 0 ? ActiveTheme.getBasePrePassedBucketColors()[index-10] : chosenColor;
            selectionViews[index].setBackgroundColor(backgroundColor);
        }
        else if (index < 16) {
            final int backgroundColor = chosenColor == 0 ? ActiveTheme.getBasePassedBucketColors()[index-14] : chosenColor;
            selectionViews[index].setBackgroundColor(backgroundColor);
        }
        else if (index < 26) {
            final int backgroundColor = chosenColor == 0 ? ActiveTheme.getBaseLevelProgressionBucketColors()[index-16] : chosenColor;
            selectionViews[index].setBackgroundColor(backgroundColor);
        }
        else if (index < 32) {
            final int backgroundColor = chosenColor == 0 ? ActiveTheme.getBaseAnkiColors()[index-26] : chosenColor;
            selectionViews[index].setBackgroundColor(backgroundColor);
        }

        selectionDescription.setText(selectionDescriptions[index]);
    }

    private void updateColor(final int index, final int color, final boolean ignorePicker) {
        final int actualColor = (color == 0) ? getBaseColor(index) : color;

        if (color == 0) {
            resetColorButton.disableInteraction();
        }
        else {
            resetColorButton.enableInteraction();
        }

        if (!ignorePicker) {
            colorPicker.setColor(actualColor | Color.BLACK);
        }

        final @Nullable Drawable background = colorPickerPreview.getBackground();
        if (background != null) {
            background.setColorFilter(new SimpleColorFilter(actualColor));
        }

        rgbValues.setTextFormat("RGB values:\n#%06X", actualColor & 0xFFFFFF);
    }

    private void setSelection(final int newSelection) {
        if (selection >= 0 && selection < NUM_THEME_CUSTOMIZATION_OPTIONS) {
            selectionHighlights[selection].setVisibility(false);
        }
        selection = newSelection;
        if (selection >= 0 && selection < NUM_THEME_CUSTOMIZATION_OPTIONS) {
            selectionHighlights[selection].setVisibility(true);
            updateSelection(selection);
            updateColor(selection, chosenColors[selection], false);
        }
    }

    private void setColor(final int index, final int color, final boolean ignorePicker) {
        chosenColors[index] = color;
        updateSelection(index);
        updateColor(index, color, ignorePicker);
    }

    private void saveColors() {
        GlobalSettings.Display.setThemeCustomizations(ActiveTheme.getCurrentTheme(), chosenColors);
    }

    /**
     * Handler for the reset color button.
     */
    private void resetColor() {
        safe(() -> {
            setColor(selection, 0, false);
            saveColors();
        });
    }
}
