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

package com.the_tinkering.wk.test;

import android.database.Cursor;

import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.the_tinkering.wk.db.AppDatabase;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_48_49;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_49_50;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_50_51;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_51_52;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_52_53;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_53_54;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_54_55;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_55_56;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_56_57;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_57_58;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_58_59;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_59_60;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_60_61;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_61_62;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_62_63;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_63_64;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_64_65;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_65_66;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_66_67;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_67_68;
import static com.the_tinkering.wk.db.AppDatabase.MIGRATION_68_69;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class to verify database migrations.
 */
@SuppressWarnings({"JavaDoc", "resource"})
@SmallTest
public final class DatabaseMigrationTest {
    private static final int LATEST_VERSION = 69;
    private static final String DATABASE_NAME_TEST = "wanikani-test";

    @Rule
    public MigrationTestHelper testHelper =
            new MigrationTestHelper(
                    InstrumentationRegistry.getInstrumentation(),
                    requireNonNull(AppDatabase.class.getCanonicalName()),
                new FrameworkSQLiteOpenHelperFactory());

    private AppDatabase getMigratedRoomDatabase() {
        final AppDatabase database = Room.databaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class, DATABASE_NAME_TEST)
                .addMigrations(
                        MIGRATION_48_49,
                        MIGRATION_49_50,
                        MIGRATION_50_51,
                        MIGRATION_51_52,
                        MIGRATION_52_53,
                        MIGRATION_53_54,
                        MIGRATION_54_55,
                        MIGRATION_55_56,
                        MIGRATION_56_57,
                        MIGRATION_57_58,
                        MIGRATION_58_59,
                        MIGRATION_59_60,
                        MIGRATION_60_61,
                        MIGRATION_61_62,
                        MIGRATION_62_63,
                        MIGRATION_63_64,
                        MIGRATION_64_65,
                        MIGRATION_65_66,
                        MIGRATION_66_67,
                        MIGRATION_67_68,
                        MIGRATION_68_69)
                .build();
        testHelper.closeWhenFinished(database);
        return database;
    }

    @Test
    public void testMigration_48() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 48);
        assertEquals(48, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_48_49, MIGRATION_49_50, MIGRATION_50_51,
                MIGRATION_51_52, MIGRATION_52_53, MIGRATION_53_54, MIGRATION_54_55, MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58,
                MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65,
                MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_49() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 49);
        assertEquals(49, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_49_50, MIGRATION_50_51, MIGRATION_51_52,
                MIGRATION_52_53, MIGRATION_53_54, MIGRATION_54_55, MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59,
                MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66,
                MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_50() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 50);
        assertEquals(50, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_50_51, MIGRATION_51_52, MIGRATION_52_53,
                MIGRATION_53_54, MIGRATION_54_55, MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60,
                MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67,
                MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_51() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 51);
        assertEquals(51, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_51_52, MIGRATION_52_53, MIGRATION_53_54,
                MIGRATION_54_55, MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61,
                MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68,
                MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_52() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 52);
        assertEquals(52, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_52_53, MIGRATION_53_54, MIGRATION_54_55,
                MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62,
                MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_53() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 53);
        assertEquals(53, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_53_54, MIGRATION_54_55, MIGRATION_55_56,
                MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63,
                MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_54() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 54);
        assertEquals(54, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_54_55, MIGRATION_55_56, MIGRATION_56_57,
                MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64,
                MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_55() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 55);
        assertEquals(55, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_55_56, MIGRATION_56_57, MIGRATION_57_58,
                MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65,
                MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_56() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 56);
        assertEquals(56, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_56_57, MIGRATION_57_58, MIGRATION_58_59,
                MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66,
                MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_57() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 57);
        assertEquals(57, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_57_58, MIGRATION_58_59, MIGRATION_59_60,
                MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67,
                MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_58() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 58);
        assertEquals(58, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_58_59, MIGRATION_59_60, MIGRATION_60_61,
                MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68,
                MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_59() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 59);
        assertEquals(59, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_59_60, MIGRATION_60_61, MIGRATION_61_62,
                MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_60() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 60);
        assertEquals(60, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_60_61, MIGRATION_61_62, MIGRATION_62_63,
                MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_61() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 61);
        assertEquals(61, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_61_62, MIGRATION_62_63, MIGRATION_63_64,
                MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_62() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 62);
        assertEquals(62, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_62_63, MIGRATION_63_64, MIGRATION_64_65,
                MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_63() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 63);
        assertEquals(63, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_63_64, MIGRATION_64_65, MIGRATION_65_66,
                MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_64() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 64);
        assertEquals(64, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_64_65, MIGRATION_65_66, MIGRATION_66_67,
                MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_65() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 65);
        assertEquals(65, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_65_66, MIGRATION_66_67, MIGRATION_67_68,
                MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_66() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 66);
        assertEquals(66, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_66_67, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    @Test
    public void testMigration_67() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 67);
        assertEquals(67, db.getVersion());
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_67_68, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());
    }

    private static void checkMigration_68_audioDownloadStatus(final SupportSQLiteDatabase migratedDb) {
        final Cursor cursor = migratedDb.query("SELECT level, numTotal, numNoAudio, numMissingAudio, numPartialAudio, numFullAudio"
                + " FROM audio_download_status");
        assertEquals(6, cursor.getColumnCount());
        assertEquals(1, cursor.getCount());

        cursor.moveToNext();
        assertEquals(1, cursor.getInt(0));
        assertEquals(2, cursor.getInt(1));
        assertEquals(3, cursor.getInt(2));
        assertEquals(4, cursor.getInt(3));
        assertEquals(5, cursor.getInt(4));
        assertEquals(6, cursor.getInt(5));

        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());

        cursor.close();
    }

    private static void checkMigration_68_levelProgression(final SupportSQLiteDatabase migratedDb) {
        final Cursor cursor = migratedDb.query("SELECT id, abandonedAt, completedAt, createdAt, passedAt, startedAt, unlockedAt, level"
                + " FROM level_progression ORDER BY id");
        assertEquals(8, cursor.getColumnCount());
        assertEquals(2, cursor.getCount());

        cursor.moveToNext();
        assertEquals(1, cursor.getLong(0));
        assertEquals(2, cursor.getLong(1));
        assertEquals(3, cursor.getLong(2));
        assertEquals(4, cursor.getLong(3));
        assertEquals(5, cursor.getLong(4));
        assertEquals(6, cursor.getLong(5));
        assertEquals(7, cursor.getLong(6));
        assertEquals(8, cursor.getInt(7));

        cursor.moveToNext();
        assertEquals(2, cursor.getLong(0));
        assertEquals(0, cursor.getLong(1));
        assertEquals(0, cursor.getLong(2));
        assertEquals(0, cursor.getLong(3));
        assertEquals(0, cursor.getLong(4));
        assertEquals(0, cursor.getLong(5));
        assertEquals(0, cursor.getLong(6));
        assertEquals(3, cursor.getInt(7));

        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());

        cursor.close();
    }

    private static void checkMigration_68_logRecord(final SupportSQLiteDatabase migratedDb) {
        final Cursor cursor = migratedDb.query("SELECT id, timestamp, tag, length, message FROM log_record ORDER BY id");
        assertEquals(5, cursor.getColumnCount());
        assertEquals(2, cursor.getCount());

        cursor.moveToNext();
        assertEquals(1, cursor.getLong(0));
        assertEquals(2, cursor.getLong(1));
        assertEquals("tag", cursor.getString(2));
        assertEquals(3, cursor.getInt(3));
        assertEquals("message", cursor.getString(4));

        cursor.moveToNext();
        assertEquals(2, cursor.getLong(0));
        assertEquals(0, cursor.getLong(1));
        assertEquals("", cursor.getString(2));
        assertEquals(3, cursor.getInt(3));
        assertEquals("", cursor.getString(4));

        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());

        cursor.close();
    }

    private static void checkMigration_68_properties(final SupportSQLiteDatabase migratedDb) {
        final Cursor cursor = migratedDb.query("SELECT name, value FROM properties ORDER BY name");
        assertEquals(2, cursor.getColumnCount());
        assertEquals(2, cursor.getCount());

        cursor.moveToNext();
        assertEquals("bar", cursor.getString(0));
        assertEquals("baz", cursor.getString(1));

        cursor.moveToNext();
        assertEquals("foo", cursor.getString(0));
        assertEquals("bar", cursor.getString(1));

        cursor.moveToNext();
        assertTrue(cursor.isAfterLast());

        cursor.close();
    }

    @Test
    public void testMigration_68() throws IOException {
        final SupportSQLiteDatabase db = testHelper.createDatabase(DATABASE_NAME_TEST, 68);
        assertEquals(68, db.getVersion());
        db.execSQL("INSERT INTO audio_download_status (level, numTotal, numNoAudio, numMissingAudio, numPartialAudio, numFullAudio) VALUES"
                + " (1, 2, 3, 4, 5, 6)");
        db.execSQL("INSERT INTO level_progression (id, abandonedAt, completedAt, createdAt, passedAt, startedAt, unlockedAt, level) VALUES"
                + " (1, 2, 3, 4, 5, 6, 7, 8)");
        db.execSQL("INSERT INTO level_progression (id, abandonedAt, completedAt, createdAt, passedAt, startedAt, unlockedAt, level) VALUES"
                + " (2, null, null, null, null, null, null, 3)");
        db.execSQL("INSERT INTO log_record (id, timestamp, tag, length, message) VALUES (1, 2, 'tag', 3, 'message')");
        db.execSQL("INSERT INTO log_record (id, timestamp, tag, length, message) VALUES (2, null, null, 3, null)");
        db.execSQL("INSERT INTO properties (name, value) VALUES ('foo', 'bar')");
        db.execSQL("INSERT INTO properties (name, value) VALUES ('bar', 'baz')");
        db.close();

        testHelper.runMigrationsAndValidate(DATABASE_NAME_TEST, LATEST_VERSION, true, MIGRATION_68_69);
        assertEquals(LATEST_VERSION, getMigratedRoomDatabase().getOpenHelper().getReadableDatabase().getVersion());

        final SupportSQLiteDatabase migratedDb = getMigratedRoomDatabase().getOpenHelper().getReadableDatabase();
        checkMigration_68_audioDownloadStatus(migratedDb);
        checkMigration_68_levelProgression(migratedDb);
        checkMigration_68_logRecord(migratedDb);
        checkMigration_68_properties(migratedDb);
    }
}
