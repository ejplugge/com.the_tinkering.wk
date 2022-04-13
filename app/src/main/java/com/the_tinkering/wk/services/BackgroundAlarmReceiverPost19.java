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

package com.the_tinkering.wk.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.StableIds;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.HOUR;
import static com.the_tinkering.wk.services.BackgroundAlarmReceiver.isAlarmRequired;
import static com.the_tinkering.wk.services.BackgroundAlarmReceiver.processAlarm;
import static com.the_tinkering.wk.util.ObjectSupport.getTopOfHour;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * The alarm receiver that gets triggered once per hour, and is responsible for
 * notifications, widgets and background sync.
 */
public final class BackgroundAlarmReceiverPost19 extends BroadcastReceiver {
    private static final Logger LOGGER = Logger.get(BackgroundAlarmReceiverPost19.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        safe(() -> {
            LOGGER.info("Background alarm post19 received");
            if (isAlarmRequired()) {
                @Nullable PowerManager.WakeLock wl = null;
                final @Nullable PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wk:wk");
                    wl.acquire(3 * Constants.MINUTE);
                }
                processAlarm(wl);
            }
        });
        safe(BackgroundAlarmReceiverPost19::scheduleOrCancelAlarm);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiverPost19.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                        StableIds.BACKGROUND_ALARM_REQUEST_CODE_2, intent, flags);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
            }
        }
    }

    /**
     * Cancel the notification alarm.
     */
    private static void cancelAlarm() {
        final @Nullable AlarmManager alarmManager = (AlarmManager) WkApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiverPost19.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                    StableIds.BACKGROUND_ALARM_REQUEST_CODE_2, intent, flags);
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
        });
    }
}
