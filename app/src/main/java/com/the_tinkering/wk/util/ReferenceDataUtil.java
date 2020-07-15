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

package com.the_tinkering.wk.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.model.PitchInfo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for determining reference data for a subject, backed by JSON files stored
 * as resources in the app.
 */
public final class ReferenceDataUtil {
    private static final Logger LOGGER = Logger.get(ReferenceDataUtil.class);

    private static final Map<String, Integer> FREQUENCY_MAP = new HashMap<>();
    private static final Map<String, Integer> JOYO_GRADE_MAP = new HashMap<>();
    private static final Map<String, Integer> KANJI_JLPT_LEVEL_MAP = new HashMap<>();
    private static final Map<String, Integer> VOCAB_JLPT_LEVEL_MAP = new HashMap<>();
    private static final Map<String, List<PitchInfo>> PITCH_INFO_MAP = new HashMap<>();
    private static final InternalizingCache<Integer> INT_INTERNALIZER = new InternalizingCache<>();
    private static boolean frequencyMapLoaded = false;
    private static boolean joyoGradeMapLoaded = false;
    private static boolean jlptLevelMapsLoaded = false;
    private static boolean pitchInfoMapLoaded = false;

    private ReferenceDataUtil() {
        //
    }

    /**
     * Load a JSON file into a map.
     *
     * @param map the map to store the data in
     * @param resourceId the resource ID of the JSON file to load
     */
    private static void loadMap(final Map<? super String, ? super Integer> map, final int resourceId) {
        if (!map.isEmpty()) {
            return;
        }

        @Nullable InputStream is = null;
        try {
            is = WkApplication.getInstance().getResources().openRawResource(resourceId);
            final Map<String, Integer> json = Converters.getObjectMapper().readValue(is, new TypeReference<Map<String, Integer>>() {});
            for (final Map.Entry<String, Integer> entry: json.entrySet()) {
                final @Nullable String key = entry.getKey();
                final @Nullable Integer value = entry.getValue();
                if (key != null && value != null) {
                    map.put(key.intern(), requireNonNull(INT_INTERNALIZER.internalize(value)));
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (final Exception e) {
                    //
                }
            }
        }
    }

    /**
     * Load the frequency map if not loaded already.
     */
    private static void loadFrequencyMap() {
        if (frequencyMapLoaded) {
            return;
        }
        loadMap(FREQUENCY_MAP, R.raw.kanjidic_freq);
        frequencyMapLoaded = true;
    }

    /**
     * Load the Joyo grade map if not loaded already.
     */
    private static void loadJoyoGradeMap() {
        if (joyoGradeMapLoaded) {
            return;
        }
        loadMap(JOYO_GRADE_MAP, R.raw.wikipedia_joyo_grades);
        joyoGradeMapLoaded = true;
    }

    /**
     * Load the JLPT level map if not loaded already.
     */
    private static void loadJlptLevelMaps() {
        if (jlptLevelMapsLoaded) {
            return;
        }
        loadMap(KANJI_JLPT_LEVEL_MAP, R.raw.kanji_jlpt_levels);
        loadMap(VOCAB_JLPT_LEVEL_MAP, R.raw.vocab_jlpt_levels);
        jlptLevelMapsLoaded = true;
    }

    /**
     * Load the pitch info map if not loaded already.
     */
    private static void loadPitchInfoMap() {
        if (pitchInfoMapLoaded || !PITCH_INFO_MAP.isEmpty()) {
            return;
        }

        @Nullable InputStream is = null;
        try {
            is = WkApplication.getInstance().getResources().openRawResource(R.raw.pitch_info);
            final Map<String, List<PitchInfo>> json = Converters.getObjectMapper().readValue(is, new TypeReference<Map<String, List<PitchInfo>>>() {});
            for (final Map.Entry<String, List<PitchInfo>> entry: json.entrySet()) {
                final @Nullable String key = entry.getKey();
                final @Nullable List<PitchInfo> value = entry.getValue();
                if (key != null && value != null) {
                    PITCH_INFO_MAP.put(key.intern(), value);
                }
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (final Exception e) {
                    //
                }
            }
        }
        pitchInfoMapLoaded = true;
    }

    /**
     * Get the frequency for a subject with the given type and characters.
     *
     * @param type the subject's type
     * @param characters the subject's characters
     * @return the frequency or 0 if not found
     */
    public static int getFrequency(final @Nullable SubjectType type, final @Nullable String characters) {
        if (characters == null || type == null || !type.isKanji()) {
            return 0;
        }
        loadFrequencyMap();
        final @Nullable Integer value = FREQUENCY_MAP.get(characters);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Get the Joyo grade for a subject with the given type and characters.
     *
     * @param type the subject's type
     * @param characters the subject's characters
     * @return the frequency or 0 if not found
     */
    public static int getJoyoGrade(final @Nullable SubjectType type, final @Nullable String characters) {
        if (characters == null || type == null || !type.isKanji()) {
            return 0;
        }
        loadJoyoGradeMap();
        final @Nullable Integer value = JOYO_GRADE_MAP.get(characters);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Get the JLPT level for a subject with the given type and characters.
     *
     * @param type the subject's type
     * @param characters the subject's characters
     * @return the frequency or 0 if not found
     */
    public static int getJlptLevel(final @Nullable SubjectType type, final @Nullable String characters) {
        if (characters == null || type == null || !(type.isKanji() || type.isVocabulary())) {
            return 0;
        }
        loadJlptLevelMaps();

        if (type.isKanji()) {
            final @Nullable Integer value = KANJI_JLPT_LEVEL_MAP.get(characters);
            if (value == null) {
                return 0;
            }
            return value;
        }

        final @Nullable Integer value = VOCAB_JLPT_LEVEL_MAP.get(characters);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Get the pitch info for a subject with the given type and characters.
     *
     * @param type the subject's type
     * @param characters the subject's characters
     * @return the pitch info or null if not found
     */
    public static @Nullable String getPitchInfo(final @Nullable SubjectType type, final @Nullable String characters) {
        if (characters == null || type == null || !type.canHavePitchInfo()) {
            return null;
        }
        loadPitchInfoMap();
        final @Nullable List<PitchInfo> info = PITCH_INFO_MAP.get(characters);
        if (info == null) {
            return null;
        }
        try {
            return Converters.getObjectMapper().writeValueAsString(info);
        }
        catch (final Exception e) {
            // This can't realistically happen
            return null;
        }
    }
}
