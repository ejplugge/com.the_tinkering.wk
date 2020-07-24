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

package com.the_tinkering.wk.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.model.AlertContext;
import com.the_tinkering.wk.util.Logger;

import java.util.concurrent.Semaphore;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.HOUR;
import static com.the_tinkering.wk.util.ObjectSupport.getTopOfHour;
import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * The alarm receiver that gets triggered once per hour, and is responsible for
 * notifications, widgets and background sync.
 */
public final class BackgroundAlarmReceiver extends BroadcastReceiver {
    private static final Logger LOGGER = Logger.get(BackgroundAlarmReceiver.class);

    private static boolean isAlarmRequired() {
        if (GlobalSettings.Other.getEnableNotifications()) {
            return true;
        }
        if (SessionWidgetProvider.hasWidgets()) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (GlobalSettings.Api.getEnableBackgroundSync()) {
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        safe(() -> {
            LOGGER.info("Background alarm received");
            if (isAlarmRequired()) {
                @Nullable PowerManager.WakeLock wl = null;
                final @Nullable PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wk:wk");
                    wl.acquire(Constants.MINUTE);
                }
                processAlarm(wl);
            }
        });
        safe(BackgroundAlarmReceiver::scheduleOrCancelAlarm);
    }

    /**
     * Schedule the alarm for notifications. It is scheduled for the top
     * of each hour, but depending on circumstances, the delivery of the alarm
     * can be delayed a bit by the device.
     */
    private static void scheduleAlarm() {
        final long nextTrigger = getTopOfHour(System.currentTimeMillis()) + HOUR;
        final @Nullable AlarmManager alarmManager = (AlarmManager) WkApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
            }
        }
    }

    /**
     * Cancel the notification alarm.
     */
    private static void cancelAlarm() {
        final @Nullable AlarmManager alarmManager = (AlarmManager) WkApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Schedule or cancel depending on user settings.
     */
    public static void scheduleOrCancelAlarm() {
        safe(() -> {
            if (isAlarmRequired()) {
                scheduleAlarm();
            }
            else {
                cancelAlarm();
            }
            BackgroundSyncWorker.cancelWork();
        });
    }

    /**
     * Process a background alarm event, whether triggered by an actual system alarm, or a database update that can affect
     * widgets and/or notifications.
     *
     * @param wakeLock the wakeLock, if applicable. If not null, this method will release the lock after all actions for
     *                 the alarm have been processed. This may happen after this method call returns.
     */
    public static void processAlarm(final @Nullable PowerManager.WakeLock wakeLock) {
        runAsync(null, publisher -> {
            LOGGER.debug("Processing background alarm");
            if (isAlarmRequired()) {
                final AppDatabase db = WkApplication.getDatabase();
                final int maxLevel = db.propertiesDao().getUserMaxLevelGranted();
                final int userLevel = db.propertiesDao().getUserLevel();
                final long now = System.currentTimeMillis();
                final AlertContext ctx = db.subjectAggregatesDao().getAlertContext(maxLevel, userLevel, now);

                final Semaphore semaphore = new Semaphore(0);
                safe(() -> NotificationWorker.processAlarm(ctx, semaphore));
                safe(() -> SessionWidgetProvider.processAlarm(ctx, semaphore));
                safe(() -> BackgroundSyncWorker.processAlarm(semaphore));
                safe(semaphore::acquire);
                safe(semaphore::acquire);
                safe(semaphore::acquire);
            }

            if (wakeLock != null) {
                safe(wakeLock::release);
            }
            return null;
        }, null, null);
    }
}
