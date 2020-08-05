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

package com.the_tinkering.wk.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.the_tinkering.wk.api.model.AuxiliaryMeaning;
import com.the_tinkering.wk.api.model.ContextSentence;
import com.the_tinkering.wk.api.model.Meaning;
import com.the_tinkering.wk.api.model.PronunciationAudio;
import com.the_tinkering.wk.api.model.Reading;
import com.the_tinkering.wk.enums.SubjectSource;
import com.the_tinkering.wk.enums.SubjectType;
import com.the_tinkering.wk.model.PitchInfo;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Room entity for the subject table. This class combines all information about a subject in
 * a single entity for convenience - subject, assignment, review statistics, study materials
 * and reference data.
 */
@Entity(tableName = "subject")
public final class SubjectEntity {
    // From base subject

    /**
     * The unique ID.
     */
    @PrimaryKey
    public long id = 0L;

    /**
     * The unique ID.
     */
    @ColumnInfo(defaultValue = "UNKNOWN")
    @NonNull
    public SubjectSource source = SubjectSource.UNKNOWN;

    /**
     * The type of subject, from an enum class.
     */
    @ColumnInfo(defaultValue = "UNKNOWN")
    @NonNull
    public SubjectType type = SubjectType.UNKNOWN;

    /**
     * The ID of the SRS system that applies to this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public long srsSystemId = 0L;

    /**
     * Timestamp when this subject was hidden, or null if it isn't hidden.
     * A hidden subject is still maintained, but is treated as non-existent almost everywhere.
     */
    @ColumnInfo(defaultValue = "0")
    public long hiddenAt = 0L;

    /**
     * The ordinal position within the level of this subject. Affects ordering but nothing else.
     */
    @ColumnInfo(defaultValue = "0")
    public int lessonPosition = 0;

    /**
     * The level this subject belongs to.
     */
    @ColumnInfo(index = true, defaultValue = "0")
    public int level = 0;

    /**
     * The characters representing this subject, or null for radicals that have no characters.
     */
    @ColumnInfo(index = true)
    public @Nullable String characters;

    /**
     * The slug, which can be used as an alternative to the characters for when it's null.
     */
    public @Nullable String slug;

    /**
     * The URL for the weg page for this subject.
     */
    public @Nullable String documentUrl;

    /**
     * The registered meanings for this subject. Encoded as a JSON string.
     */
    public @Nullable String meanings;

    /**
     * The meaning mnemonic for this subject.
     */
    public @Nullable String meaningMnemonic;

    /**
     * The meaning hint for this subject.
     */
    public @Nullable String meaningHint;

    /**
     * The registered auxiliary meanings for this subject. Encoded as a JSON string.
     */
    public @Nullable String auxiliaryMeanings;

    /**
     * The registered readings for this subject. Encoded as a JSON string.
     */
    public @Nullable String readings;

    /**
     * The reading mnemonic for this subject.
     */
    public @Nullable String readingMnemonic;

    /**
     * The reading hint for this subject.
     */
    public @Nullable String readingHint;

    /**
     * The IDs of subjects that are components of this subject. For kanji, these are the used radicals.
     * For vocab, these are the used kanji. Encoded as a JSON string.
     */
    public @Nullable String componentSubjectIds;

    /**
     * The IDs of subjects that this subject is a component of. For radicals, these are the kanji it's used in.
     * For kanji, these are the vocab it's used in. Encoded as a JSON string.
     */
    public @Nullable String amalgamationSubjectIds;

    /**
     * The IDs of kanji that are visually similar to this kanji. Empty for radicals and vocab. Encoded as a JSON string.
     */
    public @Nullable String visuallySimilarSubjectIds;

    /**
     * This subject's parts of speech. Encoded as a JSON string.
     */
    public @Nullable String partsOfSpeech;

    /**
     * The context sentences for this subject. Encoded as a JSON string.
     */
    public @Nullable String contextSentences;

    /**
     * The audio for this vocab, empty for radicals and kanji. Encoded as a JSON string.
     */
    public @Nullable String pronunciationAudios;

    /**
     * A concatenation of all searchable text in this subject. Used to speed up searches.
     */
    public @Nullable String searchTarget;

    /**
     * A concatenation of the most important searchable text in this subject. Used to speed up searches.
     */
    public @Nullable String smallSearchTarget;

    /**
     * The unique ID of this subject's assignment, or 0 if it doesn't exist.
     */
    @ColumnInfo(defaultValue = "0")
    public long assignmentId = 0L;

    /**
     * The timestamp when the next available review becomes available for this subject,
     * or null if no review is scheduled yet.
     */
    @ColumnInfo(index = true, defaultValue = "0")
    public long availableAt = 0L;

    /**
     * The timestamp when this subject was burned, or null if it hasn't been burned yet.
     */
    @ColumnInfo(index = true, defaultValue = "0")
    public long burnedAt = 0L;

    /**
     * The timestamp when this subject was passed, i.e. reached Guru I for the first time.
     * Note: for older assignments, this field used to be empty. They have been backfilled since then.
     */
    @ColumnInfo(defaultValue = "0")
    public long passedAt = 0L;

    /**
     * The timestamp when this subject was resurrected from burned status, or null if it hasn't been resurrected.
     */
    @ColumnInfo(defaultValue = "0")
    public long resurrectedAt = 0L;

    /**
     * The timestamp when this subject was started, i.e. when the lesson for this subject was completed,
     * or null if it hasn't been started yet.
     */
    @ColumnInfo(defaultValue = "0")
    public long startedAt = 0L;

    /**
     * The timestamp when this subject was unlocked, or null if it is still locked.
     */
    @ColumnInfo(defaultValue = "0")
    public long unlockedAt = 0L;

    /**
     * The current SRS stage for this subject.
     */
    @ColumnInfo(index = true, defaultValue = "-999")
    public long srsStageId = -999L;

    /**
     * The timestamp when the last incorrect answer was given for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public long lastIncorrectAnswer = 0L;

    /**
     * True if this subject's assignment has been patched locally but this hasn't been replaced with an API updated version yet.
     */
    @ColumnInfo(defaultValue = "0")
    public boolean assignmentPatched = false;

    // From study material

    /**
     * The unique ID of this subject's study material, or 0 if it doesn't exist.
     */
    @ColumnInfo(defaultValue = "0")
    public long studyMaterialId = 0L;

    /**
     * The user's meaning note.
     */
    public @Nullable String meaningNote;

    /**
     * The user's meaning synonyms. Encoded as a JSON string.
     */
    public @Nullable String meaningSynonyms;

    /**
     * The user's reading note.
     */
    public @Nullable String readingNote;

    /**
     * True if this subject's study material has been patched locally but this hasn't been replaced with an API updated version yet.
     */
    @ColumnInfo(defaultValue = "0")
    public boolean studyMaterialPatched = false;

    // From review statistics

    /**
     * The unique ID of this subject's review statistics, or 0 if it doesn't exist.
     */
    @ColumnInfo(defaultValue = "0")
    public long reviewStatisticId = 0L;

    /**
     * Number of times the meaning has been answered correctly.
     */
    @ColumnInfo(defaultValue = "0")
    public int meaningCorrect = 0;

    /**
     * Number of times the meaning has been answered incorrectly.
     */
    @ColumnInfo(defaultValue = "0")
    public int meaningIncorrect = 0;

    /**
     * The longest streak of correct meaning answers for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public int meaningMaxStreak = 0;

    /**
     * The current streak of correct meaning answers for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public int meaningCurrentStreak = 0;

    /**
     * Number of times the reading has been answered correctly.
     */
    @ColumnInfo(defaultValue = "0")
    public int readingCorrect = 0;

    /**
     * Number of times the reading has been answered incorrectly.
     */
    @ColumnInfo(defaultValue = "0")
    public int readingIncorrect = 0;

    /**
     * The longest streak of correct reading answers for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public int readingMaxStreak = 0;

    /**
     * The current streak of correct reading answers for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public int readingCurrentStreak = 0;

    /**
     * The overall percentage of correct answers for this subject.
     */
    @ColumnInfo(defaultValue = "0")
    public int percentageCorrect = 0;

    /**
     * The leech score for this subject, precomputed for the self-study quiz filters.
     */
    @ColumnInfo(defaultValue = "0")
    public int leechScore = 0;

    /**
     * True if this subject's review statistic has been patched locally but this hasn't been replaced with an API updated version yet.
     */
    @ColumnInfo(defaultValue = "0")
    public boolean statisticPatched = false;

    // From reference data

    /**
     * The frequency (1-2500) in everyday use of a kanji.
     */
    @ColumnInfo(defaultValue = "0")
    public int frequency = 0;

    /**
     * The Joyo grade where this kanji is first taught. 0 = not in Joyo, 7 = middle school.
     */
    @ColumnInfo(defaultValue = "0")
    public int joyoGrade = 0;

    /**
     * The JLPT level where this kanji/vocab is thought to be required. 0 = not in JLPT, 1-5 = N1-N5.
     */
    @ColumnInfo(defaultValue = "0")
    public int jlptLevel = 0;

    /**
     * The pitch info for this subject.
     */
    public @Nullable String pitchInfo;

    /**
     * The stroke order data for this subject.
     */
    public @Nullable String strokeData;

    /**
     * The star rating (0-5) given to the subject by the user.
     */
    @ColumnInfo(defaultValue = "0")
    public int numStars = 0;

    /**
     * Parsed version of meanings, inflated on demand.
     */
    @Ignore public @Nullable List<Meaning> parsedMeanings;

    /**
     * Parsed version of auxiliaryMeanings, inflated on demand.
     */
    @Ignore public @Nullable List<AuxiliaryMeaning> parsedAuxiliaryMeanings;

    /**
     * Parsed version of readings, inflated on demand.
     */
    @Ignore public @Nullable List<Reading> parsedReadings;

    /**
     * Parsed version of componentSubjectIds, inflated on demand.
     */
    @Ignore public @Nullable List<Long> parsedComponentSubjectIds;

    /**
     * Parsed version of amalgamationSubjectIds, inflated on demand.
     */
    @Ignore public @Nullable List<Long> parsedAmalgamationSubjectIds;

    /**
     * Parsed version of visuallySimilarSubjectIds, inflated on demand.
     */
    @Ignore public @Nullable List<Long> parsedVisuallySimilarSubjectIds;

    /**
     * Parsed version of partsOfSpeech, inflated on demand.
     */
    @Ignore public @Nullable List<String> parsedPartsOfSpeech;

    /**
     * Parsed version of contextSentences, inflated on demand.
     */
    @Ignore public @Nullable List<ContextSentence> parsedContextSentences;

    /**
     * Parsed version of pronunciationAudios, inflated on demand.
     */
    @Ignore public @Nullable List<PronunciationAudio> parsedPronunciationAudios;

    /**
     * Parsed version of meaningSynonyms, inflated on demand.
     */
    @Ignore public @Nullable List<String> parsedMeaningSynonyms;

    /**
     * Parsed version of meaningSynonyms, inflated on demand.
     */
    @Ignore public @Nullable List<PitchInfo> parsedPitchInfo;

    /**
     * Parsed version of meaningSynonyms, inflated on demand.
     */
    @Ignore public @Nullable List<String> parsedStrokeData;
}
/*
id, object, typeCode, hiddenAt, lessonPosition, srsSystemId, level, characters,
slug, documentUrl, meanings, meaningMnemonic, meaningHint,
auxiliaryMeanings, readings, readingMnemonic, readingHint,
componentSubjectIds, amalgamationSubjectIds, visuallySimilarSubjectIds, partsOfSpeech,
contextSentences, pronunciationAudios, audioDownloadStatus, searchTarget, smallSearchTarget,
assignmentId, availableAt, burnedAt, passedAt, resurrectedAt, startedAt,
unlockedAt, passed, resurrected, srsStage, levelProgressScore,
lastIncorrectAnswer, assignmentPatched,
studyMaterialId, meaningNote, meaningSynonyms, readingNote, studyMaterialPatched,
reviewStatisticId,
meaningCorrect, meaningIncorrect, meaningMaxStreak, meaningCurrentStreak,
readingCorrect, readingIncorrect, readingMaxStreak, readingCurrentStreak,
percentageCorrect, leechScore, statisticPatched,
frequency, joyoGrade, jlptLevel, pitchInfo, strokeData
 */
//    public long id = 0L;
//    @ColumnInfo(name = "object")
//    public @Nullable SubjectType type;
//    @ColumnInfo(name = "typeCode")
//    public int numStars = 0;
//    public long hiddenAt = 0L;
//    public int lessonPosition = 0;
//    public long srsSystemId = 0L;
//    @ColumnInfo(index = true) public int level = 0;
//    @ColumnInfo(index = true) public @Nullable String characters;
//    public @Nullable String slug;
//    public @Nullable String documentUrl;
//    public @Nullable String meanings;
//    public @Nullable String meaningMnemonic;
//    public @Nullable String meaningHint;
//    public @Nullable String auxiliaryMeanings;
//    public @Nullable String readings;
//    public @Nullable String readingMnemonic;
//    public @Nullable String readingHint;
//    public @Nullable String componentSubjectIds;
//    public @Nullable String amalgamationSubjectIds;
//    public @Nullable String visuallySimilarSubjectIds;
//    public @Nullable String partsOfSpeech;
//    public @Nullable String contextSentences;
//    public @Nullable String pronunciationAudios;
//    @ColumnInfo(name = "audioDownloadStatus")
//    public int unused3 = 0;
//    public @Nullable String searchTarget;
//    public @Nullable String smallSearchTarget;
//    public long assignmentId = 0L;
//    @ColumnInfo(index = true) public long availableAt = 0L;
//    @ColumnInfo(index = true) public long burnedAt = 0L;
//    public long passedAt = 0L;
//    public long resurrectedAt = 0L;
//    public long startedAt = 0L;
//    public long unlockedAt = 0L;
//    @ColumnInfo(name = "passed")
//    public boolean unused5 = false;
//    @ColumnInfo(name = "resurrected")
//    public boolean unused2 = false;
//    @ColumnInfo(index = true, name = "srsStage") public long srsStageId = 0L;
//    @ColumnInfo(name = "levelProgressScore")
//    public int unused4 = 0;
//    public long lastIncorrectAnswer = 0L;
//    public boolean assignmentPatched = false;
//    public long studyMaterialId = 0L;
//    public @Nullable String meaningNote;
//    public @Nullable String meaningSynonyms;
//    public @Nullable String readingNote;
//    public boolean studyMaterialPatched = false;
//    public long reviewStatisticId = 0L;
//    public int meaningCorrect = 0;
//    public int meaningIncorrect = 0;
//    public int meaningMaxStreak = 0;
//    public int meaningCurrentStreak = 0;
//    public int readingCorrect = 0;
//    public int readingIncorrect = 0;
//    public int readingMaxStreak = 0;
//    public int readingCurrentStreak = 0;
//    public int percentageCorrect = 0;
//    public int leechScore = 0;
//    public boolean statisticPatched = false;
//    public int frequency = 0;
//    public int joyoGrade = 0;
//    public int jlptLevel = 0;
//    public @Nullable String pitchInfo;
//    public @Nullable String strokeData;
//    @Ignore public @Nullable List<Meaning> parsedMeanings;
//    @Ignore public @Nullable List<AuxiliaryMeaning> parsedAuxiliaryMeanings;
//    @Ignore public @Nullable List<Reading> parsedReadings;
//    @Ignore public @Nullable List<Long> parsedComponentSubjectIds;
//    @Ignore public @Nullable List<Long> parsedAmalgamationSubjectIds;
//    @Ignore public @Nullable List<Long> parsedVisuallySimilarSubjectIds;
//    @Ignore public @Nullable List<String> parsedPartsOfSpeech;
//    @Ignore public @Nullable List<ContextSentence> parsedContextSentences;
//    @Ignore public @Nullable List<PronunciationAudio> parsedPronunciationAudios;
//    @Ignore public @Nullable List<String> parsedMeaningSynonyms;
//    @Ignore public @Nullable List<PitchInfo> parsedPitchInfo;
//    @Ignore public @Nullable List<String> parsedStrokeData;
