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

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static java.util.Objects.requireNonNull;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.StableIds;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.activities.MainActivity;
import com.the_tinkering.wk.jobs.Job;
import com.the_tinkering.wk.util.DummyListenableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Worker class to replace the old JobIntentService which is now deprecated.
 */
public final class JobRunnerWorker extends Worker {
    public JobRunnerWorker(final Context context, final WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Run one job. The implementing class is instantiated, and the instance is then
     * left to do the actual work.
     */
    @Override
    public @Nonnull Result doWork() {
        safe(() -> {
            final String jobClassName = requireNonNull(getInputData().getString("com.the_tinkering.wk.JOB_CLASS"));
            final Class<? extends Job> jobClass = Class.forName(jobClassName).asSubclass(Job.class);
            final String jobData = requireNonNull(getInputData().getString("com.the_tinkering.wk.JOB_DATA"));
            final Job job = jobClass
                    .getConstructor(String.class)
                    .newInstance(jobData);
            job.run();
        });
        return Result.success();
    }

    private static Notification createNotification() {
        final String title = "Flaming Durtles JobRunner service";
        final String text = "Running background jobs...";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String name = "JobRunnerChannel";
            final String description = "Flaming Durtles JobRunner service";
            final NotificationChannel channel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            final @Nullable NotificationManager notificationManager = WkApplication.getInstance().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        final Intent intent2 = new Intent(WkApplication.getInstance(), MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = 0;
        }
        final PendingIntent pendingIntent = PendingIntent.getActivity(WkApplication.getInstance(), 0, intent2, flags);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(WkApplication.getInstance(), "JobRunnerChannel");
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(GlobalSettings.Other.getNotificationCategory().getCompatCategory());
        builder.setContentIntent(pendingIntent);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setAutoCancel(true);

        return builder.build();
    }

    @Override
    public @Nonnull ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
        final ForegroundInfo foregroundInfo = new ForegroundInfo(StableIds.JOB_RUNNER_SERVICE_NOTIFICATION_ID, createNotification());
        return new DummyListenableFuture<>(foregroundInfo);
    }
}
