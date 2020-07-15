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

package com.the_tinkering.wk.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.the_tinkering.wk.db.model.Property;
import com.the_tinkering.wk.enums.QuestionType;
import com.the_tinkering.wk.enums.SessionType;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.MONTH;
import static com.the_tinkering.wk.db.Converters.sessionTypeToString;
import static com.the_tinkering.wk.db.Converters.stringToSessionType;
import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.isEqualIgnoreCase;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static com.the_tinkering.wk.util.ObjectSupport.safeNullable;

/**
 * DAO for properties: various key/value records that record useful data that doesn't count as settings.
 */
@Dao
public abstract class PropertiesDao {
    /**
     * Room-generated method: get all properties.
     *
     * @return the list
     */
    @Query("SELECT * FROM properties ORDER BY name")
    public abstract List<Property> getAll();

    /**
     * Room-generated method: get a property by name.
     *
     * @param name the name of the property
     * @return the value or null if it doesn't exist
     */
    @Query("SELECT value FROM properties WHERE name = :name")
    protected abstract @Nullable String getPropertyHelper(final String name);

    /**
     * Get a property by name.
     *
     * @param name the name of the property
     * @return the value or null if it doesn't exist
     */
    private @Nullable String getProperty(final String name) {
        return safeNullable(() -> getPropertyHelper(name));
    }

    /**
     * Room-generated method: set a property.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    @Query("INSERT OR REPLACE INTO properties (name, value) VALUES (:name, :value)")
    protected abstract void setPropertyHelper(final String name, final String value);

    /**
     * Room-generated method: delete a property.
     *
     * @param name the name of the property
     */
    @Query("DELETE FROM properties WHERE name = :name")
    public abstract void deleteProperty(final String name);

    /**
     * Set a property.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    private void setProperty(final String name, final String value) {
        safe(() -> setPropertyHelper(name, value));
    }

    /**
     * Get a property (true/false) as a boolean.
     *
     * @param name the name of the property
     * @return the value of the property, false if absent
     */
    private boolean getBooleanProperty(final String name) {
        final @Nullable String value = getProperty(name);

        if (value == null) {
            return false;
        }

        return isEqualIgnoreCase(value, "true");
    }

    /**
     * Set a property with a boolean value (true/false).
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    private void setBooleanProperty(final String name, final boolean value) {
        setProperty(name, Boolean.toString(value));
    }

    /**
     * Get a property as an int.
     *
     * @param name the name of the property
     * @return the value of the property, 0 if absent
     */
    private int getIntegerProperty(final String name) {
        final @Nullable String value = getProperty(name);

        if (isEmpty(value)) {
            return 0;
        }

        return safe(0, () -> Integer.parseInt(value, 10));
    }

    /**
     * Set a property with an int value.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    private void setIntegerProperty(final String name, final int value) {
        setProperty(name, Integer.toString(value));
    }

    /**
     * Get a property as a long.
     *
     * @param name the name of the property
     * @return the value of the property, 0 if absent
     */
    private long getLongProperty(@SuppressWarnings("SameParameterValue") final String name) {
        final @Nullable String value = getProperty(name);

        if (isEmpty(value)) {
            return 0;
        }

        return safe(0L, () -> Long.parseLong(value, 10));
    }

    /**
     * Set a property with a long value.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    private void setLongProperty(@SuppressWarnings("SameParameterValue") final String name, final long value) {
        setProperty(name, Long.toString(value));
    }

    /**
     * Get a property as a Date, represented in the database as a Unix timestamp in ms.
     * A null Date is represented as 0. Subtract the delta in ms from the timestamp.
     *
     * @param name the name of the property
     * @param delta a number of ms to subtract from the value
     * @return the value of the property, null if absent
     */
    private @Nullable Date getDateProperty(final String name, final long delta) {
        final @Nullable String value = getProperty(name);

        if (value == null) {
            return null;
        }

        long ts = safe(0L, () -> Long.parseLong(value));
        if (ts == 0) {
            return null;
        }

        if (ts >= delta) {
            ts -= delta;
        }
        return new Date(ts);
    }

    /**
     * Set a property as a Date, represented in the database as a Unix timestamp in ms.
     * A null Date is represented as 0.
     *
     * @param name the name of the property
     * @param value the value of the property
     */
    private void setDateProperty(final String name, final @Nullable Date value) {
        if (value == null) {
            setProperty(name, "0");
        }
        else {
            setProperty(name, Long.toString(value.getTime()));
        }
    }

    /**
     * Was the API key rejected by WK?.
     *
     * @return true if it was.
     */
    public final boolean isApiKeyRejected() {
        return getBooleanProperty("api_key_rejected");
    }

    /**
     * Was the API key rejected by WK?.
     *
     * @param value true if it was.
     */
    public final void setApiKeyRejected(final boolean value) {
        setBooleanProperty("api_key_rejected", value);
    }

    /**
     * Did the last API call result in an error?.
     *
     * @return true if it did.
     */
    public final boolean isApiInError() {
        return getBooleanProperty("api_in_error");
    }

    /**
     * Did the last API call result in an error?.
     *
     * @param value true if it did.
     */
    public final void setApiInError(final boolean value) {
        setBooleanProperty("api_in_error", value);
    }

    /**
     * When was the last successful API call?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastApiSuccessDate() {
        return getDateProperty("last_api_success", 0);
    }

    /**
     * When was the last successful API call?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastApiSuccessDate(final @Nullable Date value) {
        setDateProperty("last_api_success", value);
    }

    /**
     * When was the last successful user sync?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastUserSyncSuccessDate() {
        return getDateProperty("last_user_sync_success", 0);
    }

    /**
     * When was the last successful user sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastUserSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_user_sync_success", value);
    }

    /**
     * When was the last successful subject sync?.
     *
     * @param delta a number of ms to subtract from the value
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastSubjectSyncSuccessDate(final long delta) {
        return getDateProperty("last_subject_sync_success", delta);
    }

    /**
     * When was the last successful subject sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastSubjectSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_subject_sync_success", value);
    }

    /**
     * When was the last successful assignment sync?.
     *
     * @param delta a number of ms to subtract from the value
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastAssignmentSyncSuccessDate(final long delta) {
        return getDateProperty("last_assignment_sync_success", delta);
    }

    /**
     * When was the last successful assignment sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastAssignmentSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_assignment_sync_success", value);
    }

    /**
     * When was the last successful review statistic sync?.
     *
     * @param delta a number of ms to subtract from the value
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastReviewStatisticSyncSuccessDate(final long delta) {
        return getDateProperty("last_review_statistic_sync_success", delta);
    }

    /**
     * When was the last successful review statistic sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastReviewStatisticSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_review_statistic_sync_success", value);
    }

    /**
     * When was the last successful study material sync?.
     *
     * @param delta a number of ms to subtract from the value
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastStudyMaterialSyncSuccessDate(final long delta) {
        return getDateProperty("last_study_material_sync_success", delta);
    }

    /**
     * When was the last successful study material sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastStudyMaterialSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_study_material_sync_success", value);
    }

    /**
     * When was the last successful SRS system sync?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastSrsSystemSyncSuccessDate() {
        return getDateProperty("last_srs_system_sync_success", 0);
    }

    /**
     * When was the last successful SRS system sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastSrsSystemSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_srs_system_sync_success", value);
    }

    /**
     * When was the last successful level progression sync?.
     *
     * @param delta a number of ms to subtract from the value
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastLevelProgressionSyncSuccessDate(final long delta) {
        return getDateProperty("last_level_progression_sync_success", delta);
    }

    /**
     * When was the last successful level progression sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastLevelProgressionSyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_level_progression_sync_success", value);
    }

    /**
     * When was the last successful summary sync?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastSummarySyncSuccessDate() {
        return getDateProperty("last_summary_sync_success", 0);
    }

    /**
     * When was the last successful summary sync?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastSummarySyncSuccessDate(final @Nullable Date value) {
        setDateProperty("last_summary_sync_success", value);
    }

    /**
     * When was the last time there was a check for audio that needed to be downloaded?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastAudioScanDate() {
        return getDateProperty("last_audio_scan", 0);
    }

    /**
     * When was the last time there was a check for audio that needed to be downloaded?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastAudioScanDate(final @Nullable Date value) {
        setDateProperty("last_audio_scan", value);
    }

    /**
     * When was the last time there was a check for pitch info that needed to be downloaded?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastPitchInfoScanDate() {
        return getDateProperty("last_pitch_info_scan", 0);
    }

    /**
     * When was the last time there was a check for pitch info that needed to be downloaded?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastPitchInfoScanDate(final @Nullable Date value) {
        setDateProperty("last_pitch_info_scan", value);
    }

    /**
     * When was the last availableAt for which a notification was issued?.
     *
     * @return the timestamp, or null if not known
     */
    public final @Nullable Date getLastNotifiedReviewDate() {
        return getDateProperty("last_notified_review_date", 0);
    }

    /**
     * When was the last availableAt for which a notification was issued?.
     *
     * @param value the timestamp, or null if not known
     */
    public final void setLastNotifiedReviewDate(final @Nullable Date value) {
        setDateProperty("last_notified_review_date", value);
    }

    /**
     * The max level granted by the user's subscription.
     *
     * @return the level, or 3 if not known
     */
    public final int getUserMaxLevelGranted() {
        final @Nullable Date end = getDateProperty("user_max_level_granted_checked", -MONTH);
        if (end == null || System.currentTimeMillis() > end.getTime()) {
            return 3;
        }
        return getIntegerProperty("user_max_level_granted");
    }

    /**
     * The max level granted by the user's subscription.
     *
     * @param value the level, or 3 if not known
     */
    public final void setUserMaxLevelGranted(final int value) {
        setIntegerProperty("user_max_level_granted", value);
        setDateProperty("user_max_level_granted_checked", new Date());
    }

    /**
     * The user's current level.
     *
     * @return the level, or 0 if not known
     */
    public final int getUserLevel() {
        return getIntegerProperty("user_level");
    }

    /**
     * The user's current level.
     *
     * @param value the level, or 0 if not known
     */
    public final void setUserLevel(final int value) {
        setIntegerProperty("user_level", value);
    }

    /**
     * The user's ID.
     *
     * @return the user ID as a UUID
     */
    public final @Nullable String getUserId() {
        return getProperty("user_id");
    }

    /**
     * The user's ID.
     *
     * @param value the user ID as a UUID
     */
    public final void setUserId(final String value) {
        setProperty("user_id", value);
    }

    /**
     * The user's username.
     *
     * @return the username
     */
    public final @Nullable String getUsername() {
        return getProperty("username");
    }

    /**
     * The user's username.
     *
     * @param value the username
     */
    public final void setUsername(final String value) {
        setProperty("username", value);
    }

    /**
     * Is the user in vacation mode?.
     *
     * @return true if they are
     */
    public final boolean getVacationMode() {
        return getBooleanProperty("vacation_mode");
    }

    /**
     * Is the user in vacation mode?.
     *
     * @param value true if they are
     */
    public final void setVacationMode(final boolean value) {
        setBooleanProperty("vacation_mode", value);
    }

    /**
     * The type of the current session.
     *
     * @return the type
     */
    public final SessionType getSessionType() {
        return stringToSessionType(getProperty("session_type"));
    }

    /**
     * The type of the current session.
     *
     * @param sessionType the type
     */
    public final void setSessionType(final SessionType sessionType) {
        setProperty("session_type", sessionTypeToString(sessionType));
    }

    /**
     * Is the on/kun setting active for this session?.
     *
     * @return true if it is
     */
    public final boolean getSessionOnkun() {
        return getBooleanProperty("session_onkun");
    }

    /**
     * Is the on/kun setting active for this session?.
     *
     * @param sessionOnkun true if it is
     */
    public final void setSessionOnkun(final boolean sessionOnkun) {
        setBooleanProperty("session_onkun", sessionOnkun);
    }

    /**
     * The version of the currently loaded reference data.
     *
     * @return the version
     */
    public final int getReferenceDataVersion() {
        return getIntegerProperty("reference_data_version");
    }

    /**
     * The version of the currently loaded reference data.
     *
     * @param referenceDataVersion the version
     */
    public final void setReferenceDataVersion(final int referenceDataVersion) {
        setIntegerProperty("reference_data_version", referenceDataVersion);
    }

    /**
     * Has a notification been set?.
     *
     * @return true if it has
     */
    public final boolean getNotificationSet() {
        return getBooleanProperty("notification_set");
    }

    /**
     * Has a notification been set?.
     *
     * @param notificationSet true if it has
     */
    public final void setNotificationSet(final boolean notificationSet) {
        setBooleanProperty("notification_set", notificationSet);
    }

    /**
     * Has a forced late refresh of all core models been requested?.
     *
     * <p>
     *     This is requested if a subject passes or if the user changes level.
     * </p>
     *
     * @return true if it has
     */
    public final boolean getForceLateRefresh() {
        return getBooleanProperty("force_late_refresh");
    }

    /**
     * Has a forced late refresh of all core models been requested?.
     *
     * <p>
     *     This is requested if a subject passes or if the user changes level.
     * </p>
     *
     * @param forceLateRefresh  true if it has
     */
    public final void setForceLateRefresh(final boolean forceLateRefresh) {
        setBooleanProperty("force_late_refresh", forceLateRefresh);
    }

    /**
     * Has a sync reminder been set?.
     *
     * @return true if it has
     */
    public final boolean getSyncReminder() {
        return getBooleanProperty("sync_reminder");
    }

    /**
     * Has a sync reminder been set?.
     *
     * @param syncReminder true if it has
     */
    public final void setSyncReminder(final boolean syncReminder) {
        setBooleanProperty("sync_reminder", syncReminder);
    }

    /**
     * Get the session item ID (subject ID) for the currently shown item in the session.
     *
     * @return the ID
     */
    public final long getCurrentItemId() {
        return getLongProperty("current_item_id");
    }

    /**
     * Get the session item ID (subject ID) for the currently shown item in the session.
     *
     * @param currentItemId the ID
     */
    public final void setCurrentItemId(final long currentItemId) {
        setLongProperty("current_item_id", currentItemId);
    }

    /**
     * Get the question type for the currently shown question in the session.
     *
     * @return the type
     */
    public final QuestionType getCurrentQuestionType() {
        final @Nullable String value = getProperty("current_question_type");
        if (value == null) {
            return QuestionType.WANIKANI_RADICAL_NAME;
        }
        try {
            return QuestionType.valueOf(value);
        }
        catch (final Exception e) {
            return QuestionType.WANIKANI_RADICAL_NAME;
        }
    }

    /**
     * Get the question type for the currently shown question in the session.
     *
     * @param currentQuestionType the type
     */
    public final void setCurrentQuestionType(final QuestionType currentQuestionType) {
        setProperty("current_question_type", currentQuestionType.toString());
    }

    /**
     * Has a specific migration been done already?.
     *
     * @return true if it has
     */
    public final boolean getMigrationDoneAnkiSplit() {
        return getBooleanProperty("migration_done_anki_split");
    }

    /**
     * Set that a specific migration been done already.
     *
     * @param value true if it has
     */
    public final void setMigrationDoneAnkiSplit(final boolean value) {
        setBooleanProperty("migration_done_anki_split", value);
    }

    /**
     * Has a specific migration been done already?.
     *
     * @return true if it has
     */
    public final boolean getMigrationDoneAudio2() {
        return getBooleanProperty("migration_done_audio2");
    }

    /**
     * Set that a specific migration been done already.
     *
     * @param value true if it has
     */
    public final void setMigrationDoneAudio2(final boolean value) {
        setBooleanProperty("migration_done_audio2", value);
    }

    /**
     * Has a specific migration been done already?.
     *
     * @return true if it has
     */
    public final boolean getMigrationDoneNotif() {
        return getBooleanProperty("migration_done_notif");
    }

    /**
     * Set that a specific migration been done already.
     *
     * @param value true if it has
     */
    public final void setMigrationDoneNotif(final boolean value) {
        setBooleanProperty("migration_done_notif", value);
    }

    /**
     * Has a specific migration been done already?.
     *
     * @return true if it has
     */
    public final boolean getMigrationDoneDump() {
        return getBooleanProperty("migration_done_dump");
    }

    /**
     * Set that a specific migration been done already.
     *
     * @param value true if it has
     */
    public final void setMigrationDoneDump(final boolean value) {
        setBooleanProperty("migration_done_dump", value);
    }
}
