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

package com.the_tinkering.wk.activities;

import android.os.Bundle;
import android.view.View;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.model.PitchInfo;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.PitchInfoUtil;
import com.the_tinkering.wk.util.ReferenceDataUtil;
import com.the_tinkering.wk.views.StrokeDiagramView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

/**
 * A simple activity with a few test views, for generic testing purposes.
 * Unless the test option in the menu is activated, there is no path to
 * get here.
 */
@SuppressWarnings("JavaDoc")
public final class TestActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(TestActivity.class);

    private static boolean activePitchInfoDownload = false;

    private final ViewProxy document = new ViewProxy();

    public TestActivity() {
        super(R.layout.activity_test, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        document.setDelegate(this, R.id.document);
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

    public void downloadPitchInfo(@SuppressWarnings("unused") final View view) {
        if (activePitchInfoDownload) {
            activePitchInfoDownload = false;
            return;
        }

        activePitchInfoDownload = true;

        runAsync(this, publisher -> {
            final AppDatabase db = WkApplication.getDatabase();
            final int maxLevel = db.subjectAggregatesDao().getMaxLevel();

            LOGGER.info("Number of weblio files: %d", PitchInfoUtil.getNumWeblioFiles());

            int count = 0;
            final int max = 250;
            for (int i=1; i<=maxLevel; i++) {
                if (!activePitchInfoDownload || count >= max) {
                    break;
                }
                final List<Subject> subjects = db.subjectCollectionsDao().getByLevelRange(i, i);
                for (final Subject subject: subjects) {
                    if (!activePitchInfoDownload || count >= max) {
                        break;
                    }
                    if (!subject.getType().canHavePitchInfo()) {
                        continue;
                    }
//                    if (subject.getId() == 4549 || subject.getId() == 4551 || subject.getId() == 5294 || subject.getId() == 6091) {
//                        continue;
//                    }
                    final String characters = requireNonNull(subject.getCharacters());

                    if (!PitchInfoUtil.existsWeblioFile(characters)) {
                        LOGGER.info("Subject: %s (%d) (%s)",
                                characters, subject.getId(), subject.getOneMeaning());
                        PitchInfoUtil.downloadWeblioFile(characters);
                        count++;
                    }
                }
            }

            activePitchInfoDownload = false;
            return null;
        }, null, null);
    }

    public void generatePitchInfo(@SuppressWarnings("unused") final View view) {
        runAsync(this, publisher -> {
            final AppDatabase db = WkApplication.getDatabase();
            final int maxLevel = db.subjectAggregatesDao().getMaxLevel();

            LOGGER.info("Number of weblio files: %d", PitchInfoUtil.getNumWeblioFiles());

            final Map<String, List<PitchInfo>> map = new HashMap<>();
            for (int i=1; i<=maxLevel; i++) {
                final List<Subject> subjects = db.subjectCollectionsDao().getByLevelRange(i, i);
                // LOGGER.debug("%d: %d", System.currentTimeMillis(), subjects.size());
                for (final Subject subject: subjects) {
                    if (!subject.getType().canHavePitchInfo()) {
                        continue;
                    }
                    final String characters = requireNonNull(subject.getCharacters());

                    if (subject.isPrefix() || subject.isSuffix()) {
                        map.put(characters, Collections.emptyList());
                    }
                    else if (PitchInfoUtil.existsWeblioFile(characters)) {
                        final Set<PitchInfo> pitchInfo = PitchInfoUtil.parseWeblioFile(characters);
                        final List<PitchInfo> list = new ArrayList<>(pitchInfo);
                        Collections.sort(list);
                        map.put(characters, list);
                    }
                    else {
                        map.put(characters, Collections.emptyList());
                    }
                }
            }
            PitchInfoUtil.saveMap(map);
            return null;
        }, null, null);
    }

    public void checkPitchInfo(@SuppressWarnings("unused") final View view) {
        runAsync(this, publisher -> {
            final AppDatabase db = WkApplication.getDatabase();

            LOGGER.info("Number of weblio files: %d", PitchInfoUtil.getNumWeblioFiles());

            final int maxLevel = db.subjectAggregatesDao().getMaxLevel();
            for (final Subject subject: db.subjectCollectionsDao().getByLevelRange(1, maxLevel)) {
                if (subject.getType().canHavePitchInfo()) {
                    final @Nullable String pitchInfo =
                            ReferenceDataUtil.getPitchInfo(SubjectType.WANIKANI_VOCAB, subject.getCharacters());
                    if (pitchInfo == null) {
                        LOGGER.info("No pitch info for: %s (%d) (%s) (%s)", subject.getCharacters(), subject.getId(),
                                subject.getOneMeaning(), subject.getRawPitchInfo());
                    }
                }
            }

            return null;
        }, null, null);
    }

    public void theButton(@SuppressWarnings("unused") final View view) {
        safe(() -> {
            LOGGER.info("Test button clicked!");
            document.setText("Click!");
            goToActivity(DigraphHelpActivity.class);
        });
    }

    public void theButton2(@SuppressWarnings("unused") final View view) {
        safe(() -> {
            LOGGER.info("Test button 2 clicked!");
            document.setText("Click 2!");
//            goToActivity(NoApiKeyHelpActivity.class);

            final @Nullable StrokeDiagramView diagram = findViewById(R.id.strokeDiagram);
            if (diagram != null) {
                final Collection<String> strokeData = new ArrayList<>();
                strokeData.add("M34.25,16.25c1,1,1.48,2.38,1.5,4c0.38,33.62,2.38,59.38-11,73.25T1,27.50,23.43");
                //noinspection StringConcatenationMissingWhitespace
                strokeData.add("M36.25,19c4.12-0.62,31.49-4.78,33.25-5c4-0.5,5.5,1.12,5.5,4.75c0,2.76-0.5,49.25-0.5,69.5"
                        + "c0,13-6.25,4-8.75,1.75T2,37.50,15.50");
                strokeData.add("M37.25,38c10.25-1.5,27.25-3.75,36.25-4.5T3,40.00,33.50");
                strokeData.add("M37,58.25c8.75-1.12,27-3.5,36.25-4T4,40.00,54.50");
                diagram.setStrokeData(strokeData);
                diagram.setAnimated(true);
                diagram.setSize((int) (400 * getResources().getDisplayMetrics().density));
            }
        });
    }
}
