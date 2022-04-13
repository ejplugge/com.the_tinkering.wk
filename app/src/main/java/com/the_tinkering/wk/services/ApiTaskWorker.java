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
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.db.model.TaskDefinition;
import com.the_tinkering.wk.jobs.TickJob;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.tasks.ApiTask;
import com.the_tinkering.wk.util.DummyListenableFuture;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Worker class to replace the old JobIntentService which is now deprecated.
 */
public final class ApiTaskWorker extends Worker {
    /**
     * A single dummy object to synchronize on, to make sure the background sync doesn't
     * overlap with this.
     */
    private static final Object TASK_MONITOR = new Object();

    @SuppressWarnings("unused")
    public ApiTaskWorker(final Context context, final WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void runTasksImpl() throws Exception {
        final AppDatabase db = WkApplication.getDatabase();
        while (db.hasPendingApiTasks()) {
            //noinspection SynchronizationOnStaticField
            synchronized (TASK_MONITOR) {
                final @Nullable TaskDefinition taskDefinition = db.taskDefinitionDao().getNextTaskDefinition();
                if (taskDefinition == null) {
                    break;
                }

                final @Nullable Class<? extends ApiTask> clas = taskDefinition.getTaskClass();
                if (clas == null) {
                    db.taskDefinitionDao().deleteTaskDefinition(taskDefinition);
                }
                else {
                    final ApiTask apiTask = taskDefinition.getTaskClass()
                            .getConstructor(TaskDefinition.class)
                            .newInstance(taskDefinition);

                    if (!apiTask.canRun()) {
                        break;
                    }

                    apiTask.run();
                }
            }
        }
        if (db.taskDefinitionDao().getApiCount() == 0) {
            if (GlobalSettings.getFirstTimeSetup() == 0) {
                GlobalSettings.setFirstTimeSetup(1);
                LiveFirstTimeSetup.getInstance().forceUpdate();
            }
            if (Session.getInstance().isInactive()) {
                final Collection<Long> assignmentSubjectIds = db.subjectViewsDao().getPatchedAssignments();
                if (!assignmentSubjectIds.isEmpty()) {
                    db.assertGetPatchedAssignmentsTask(assignmentSubjectIds);
                }
                final Collection<Long> reviewStatisticsSubjectIds = db.subjectViewsDao().getPatchedReviewStatistics();
                if (!reviewStatisticsSubjectIds.isEmpty()) {
                    db.assertGetPatchedReviewStatisticsTask(reviewStatisticsSubjectIds);
                }
                final Collection<Long> studyMaterialsSubjectIds = db.subjectViewsDao().getPatchedStudyMaterials();
                if (!studyMaterialsSubjectIds.isEmpty()) {
                    db.assertGetPatchedStudyMaterialsTask(studyMaterialsSubjectIds);
                }
                if (db.propertiesDao().getForceLateRefresh()) {
                    db.propertiesDao().setForceLateRefresh(false);
                    db.assertRefreshForAllModels();
                    db.assertGetLevelProgressionTask();
                    JobRunnerService.schedule(TickJob.class, "");
                }
            }
        }
    }

    /**
     * Loop through all available tasks and execute them one by one, taking into
     * account the priority order and online status.
     *
     * <p>
     *     Each task is responsible for removing itself from the database when
     *     finished. Until then, the task will be retried indefinitely.
     * </p>
     */
    @Override
    public @Nonnull Result doWork() {
        safe(ApiTaskWorker::runTasksImpl);
        return Result.success();
    }

    private static Notification createNotification() {
        final String title = "Flaming Durtles SyncTask service";
        final String text = "Running background sync tasks...";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String name = "ApiTaskChannel";
            final String description = "Flaming Durtles SyncTask service";
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

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(WkApplication.getInstance(), "ApiTaskChannel");
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
        final ForegroundInfo foregroundInfo = new ForegroundInfo(StableIds.API_TASK_SERVICE_NOTIFICATION_ID, createNotification());
        return new DummyListenableFuture<>(foregroundInfo);
    }
}
