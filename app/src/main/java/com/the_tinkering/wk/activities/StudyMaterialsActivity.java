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

import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.jobs.SaveStudyMaterialJob;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.services.JobRunnerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Activity to show/edit the study materials for the current subject.
 *
 * <p>
 *     The list of synonyms is (arbitrarily) limited to ten entries. It would
 *     be possible to make this dynamic, but that's a hassle so I'll do it if it
 *     becomes a problem.
 * </p>
 */
public final class StudyMaterialsActivity extends AbstractActivity {
    private @Nullable Subject currentSubject = null;
    private boolean stateSaved = false;

    private final List<ViewProxy> synonymViews = new ArrayList<>();
    private final ViewProxy label = new ViewProxy();
    private final ViewProxy meaningNote = new ViewProxy();
    private final ViewProxy readingNote = new ViewProxy();

    /**
     * The constructor.
     */
    public StudyMaterialsActivity() {
        super(R.layout.activity_study_materials, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        synonymViews.add(new ViewProxy(this, R.id.synonym0));
        synonymViews.add(new ViewProxy(this, R.id.synonym1));
        synonymViews.add(new ViewProxy(this, R.id.synonym2));
        synonymViews.add(new ViewProxy(this, R.id.synonym3));
        synonymViews.add(new ViewProxy(this, R.id.synonym4));
        synonymViews.add(new ViewProxy(this, R.id.synonym5));
        synonymViews.add(new ViewProxy(this, R.id.synonym6));
        synonymViews.add(new ViewProxy(this, R.id.synonym7));
        synonymViews.add(new ViewProxy(this, R.id.synonym8));
        synonymViews.add(new ViewProxy(this, R.id.synonym9));

        label.setDelegate(this, R.id.label);
        meaningNote.setDelegate(this, R.id.meaningNote);
        readingNote.setDelegate(this, R.id.readingNote);

        final ViewProxy saveStudyMaterialsButton1 = new ViewProxy(this, R.id.saveStudyMaterialsButton1);
        final ViewProxy saveStudyMaterialsButton2 = new ViewProxy(this, R.id.saveStudyMaterialsButton2);

        saveStudyMaterialsButton1.setOnClickListener(v -> saveStudyMaterials());
        saveStudyMaterialsButton2.setOnClickListener(v -> saveStudyMaterials());

        if (savedInstanceState != null && savedInstanceState.getBoolean("stateSaved", false)) {
            stateSaved = true;
        }

        final long id = getIntent().getLongExtra("id", -1);
        runAsync(
                this,
                () -> WkApplication.getDatabase().subjectDao().getById(id),
                result -> {
                    if (result == null || !result.getType().canHaveStudyMaterials()) {
                        finish();
                        return;
                    }
                    currentSubject = result;
                    populateForm(result);
                });
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
    protected void onSaveInstanceState(final Bundle outState) {
        safe(() -> {
            super.onSaveInstanceState(outState);
            outState.putBoolean("stateSaved", stateSaved);
        });
    }

    private void populateForm(final Subject subject) {
        updateCurrentSubject();

        if (stateSaved) {
            return;
        }
        stateSaved = true;

        final CharSequence title = subject.getInfoTitle("My notes for ", ":");
        final @Nullable Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
        label.setText(title);

        meaningNote.setText(subject.getMeaningNote());
        meaningNote.setJapaneseLocale();
        readingNote.setText(subject.getReadingNote());
        readingNote.setJapaneseLocale();
        final List<String> synonyms = new ArrayList<>(subject.getMeaningSynonyms());
        while (synonyms.size() < 10) {
            synonyms.add("");
        }
        for (int i=0; i<synonymViews.size(); i++) {
            synonymViews.get(i).setText(synonyms.get(i));
            synonymViews.get(i).setJapaneseLocale();
        }
    }

    /**
     * Handler for the Save button. Update the study materials, and push to the API.
     */
    private void saveStudyMaterials() {
        safe(() -> {
            if (!interactionEnabled || currentSubject == null) {
                return;
            }
            disableInteraction();
            final Collection<String> data = new ArrayList<>();
            data.add(Long.toString(currentSubject.getId()));
            data.add(meaningNote.getText());
            data.add(readingNote.getText());
            for (int i=0; i<synonymViews.size(); i++) {
                data.add(synonymViews.get(i).getText());
            }
            final String dataString;
            try {
                dataString = Converters.getObjectMapper().writeValueAsString(data);
            } catch (final JsonProcessingException e) {
                // This can't realistically happen.
                return;
            }
            JobRunnerService.schedule(SaveStudyMaterialJob.class, dataString);
            finish();
        });
    }
}
