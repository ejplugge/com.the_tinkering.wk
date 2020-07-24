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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.activities.MainActivity;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.enums.NotificationUpdateFrequency;
import com.the_tinkering.wk.model.AlertContext;
import com.the_tinkering.wk.util.Logger;

import java.util.Locale;
import java.util.concurrent.Semaphore;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.getTopOfHour;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * The worker that sets or cancels notifications as needed.
 */
public final class NotificationWorker {
    private static final Logger LOGGER = Logger.get(NotificationWorker.class);

    private NotificationWorker() {
        //
    }

    private static void postNotification(final boolean needsSound, final AlertContext ctx) {
        final String title;
        final String text;
        if (ctx.getNumLessons() > 0) {
            title = "New lessons and reviews available";
            text = String.format(Locale.ROOT, "%d lessons and %d reviews available", ctx.getNumLessons(), ctx.getNumReviews());
        }
        else {
            title = "New reviews available";
            text = String.format(Locale.ROOT, "%d reviews available", ctx.getNumReviews());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String name = "NewReviewsChannel";
            final String description = "New reviews available";
            final int importance = GlobalSettings.Other.getNotificationPriority().getManagerImportance();
            final NotificationChannel channel = new NotificationChannel(name, name, importance);
            channel.setDescription(description);
            final @Nullable NotificationManager notificationManager = WkApplication.getInstance().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        final Intent intent2 = new Intent(WkApplication.getInstance(), MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(WkApplication.getInstance(), 0, intent2, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(WkApplication.getInstance(), "NewReviewsChannel");
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setPriority(GlobalSettings.Other.getNotificationPriority().getCompatPriority());
        builder.setCategory(GlobalSettings.Other.getNotificationCategory().getCompatCategory());
        builder.setContentIntent(pendingIntent);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setOnlyAlertOnce(!needsSound);
        builder.setAutoCancel(true);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WkApplication.getInstance());
        notificationManager.notify(1, builder.build());

        final AppDatabase db = WkApplication.getDatabase();
        db.propertiesDao().setNotificationSet(true);
    }

    private static void cancelNotification() {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WkApplication.getInstance());
        notificationManager.cancel(1);
        final AppDatabase db = WkApplication.getDatabase();
        db.propertiesDao().setNotificationSet(false);
    }

    private static void postOrCancelNotification(final boolean needsSound, final AlertContext ctx) {
        safe(() -> {
            if (ctx.getNumLessons() == 0 && ctx.getNumReviews() == 0) {
                cancelNotification();
            }
            else {
                postNotification(needsSound, ctx);
            }
        });
    }

    private static void processAlarmHelper(final AlertContext ctx) {
        if (GlobalSettings.Other.getEnableNotifications()) {
            final NotificationUpdateFrequency frequency = GlobalSettings.Other.getNotificationUpdateFrequency();
            final long topOfHour1 = getTopOfHour(System.currentTimeMillis());
            boolean needsPost = false;
            boolean needsSound = false;
            final AlertContext lastCtx = WkApplication.getDatabase().propertiesDao().getLastNotificationAlertContext();
            if (ctx.getNewestAvailableAt() != lastCtx.getNewestAvailableAt()) {
                needsPost = true;
                needsSound = true;
            }
            if (ctx.getNumLessons() == 0 && ctx.getNumReviews() == 0 && (lastCtx.getNumLessons() != 0 || lastCtx.getNumReviews() != 0)) {
                needsPost = true;
            }
            switch (frequency) {
                case ONLY_NEW_REVIEWS:
                    break;
                case ONCE_PER_HOUR: {
                    final long topOfHour2 = WkApplication.getDatabase().propertiesDao().getLastNotificationUpdate();
                    if (topOfHour1 != topOfHour2 && (lastCtx.getNumLessons() != ctx.getNumLessons()
                            || lastCtx.getNumReviews() != ctx.getNumReviews())) {
                        needsPost = true;
                    }
                    break;
                }
                case CONTINUOUSLY:
                    if (lastCtx.getNumLessons() != ctx.getNumLessons()
                            || lastCtx.getNumReviews() != ctx.getNumReviews()) {
                        needsPost = true;
                    }
                    break;
            }
            if (needsPost) {
                LOGGER.info("Notification update starts: %s %s", ctx.getNumLessons(), ctx.getNumReviews());
                WkApplication.getDatabase().propertiesDao().setLastNotificationUpdate(topOfHour1);
                WkApplication.getDatabase().propertiesDao().setLastNotificationAlertContext(ctx);
                postOrCancelNotification(needsSound, ctx);
                LOGGER.info("Notification update ends");
            }
        }
    }

    /**
     * Process a background alarm event, whether triggered by an actual system alarm, or a database update that can affect
     * widgets and/or notifications. Always runs on a background thread.
     *
     * @param ctx the details for the notifications
     * @param semaphore the method will call release() on this semaphone when the work is done
     */
    public static void processAlarm(final AlertContext ctx, final Semaphore semaphore) {
        LOGGER.debug("NotificationWorker.processAlarm");
        safe(() -> new Handler(Looper.getMainLooper()).post(() -> {
            safe(() -> processAlarmHelper(ctx));
            semaphore.release();
        }));
    }
}
