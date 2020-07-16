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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.activities.MainActivity;
import com.the_tinkering.wk.db.AppDatabase;
import com.the_tinkering.wk.model.NotificationContext;
import com.the_tinkering.wk.util.ObjectSupport;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.the_tinkering.wk.Constants.DAY;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Implementation of the app widget.
 */
public final class SessionWidgetProvider extends AppWidgetProvider {
    /**
     * Update all instances of the widget, using the supplied data.
     *
     * @param ctx notification context for the lesson/review counts
     */
    private static void updateWidgets(final NotificationContext ctx) {
        final Context context = WkApplication.getInstance();
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            return;
        }
        final AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final ComponentName name = new ComponentName(context, SessionWidgetProvider.class);
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final @Nullable Date upcoming = ctx.getMoreReviewsDate();

        final int lessonCount = ctx.getNumLessons();
        final int reviewCount = ctx.getNumReviews();
        final @Nullable String upcomingMessage;
        if (upcoming == null) {
            upcomingMessage = null;
        }
        else if (upcoming.getTime() - System.currentTimeMillis() < DAY) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(upcoming);
            upcomingMessage = String.format(Locale.ROOT, "More at %02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        else {
            final float days = ((float) (upcoming.getTime() - System.currentTimeMillis())) / DAY;
            upcomingMessage = String.format(Locale.ROOT, "More in %.1fd", days);
        }

        for (final int id: manager.getAppWidgetIds(name)) {
            final Bundle options = manager.getAppWidgetOptions(id);
            final int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 40);
            final int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40);

            final RemoteViews views;
            if (minWidth < 100) {
                views = new RemoteViews(context.getPackageName(), R.layout.session_appwidget_tall);
            }
            else if (minWidth < 200) {
                views = new RemoteViews(context.getPackageName(), R.layout.session_appwidget);
            }
            else {
                views = new RemoteViews(context.getPackageName(), R.layout.session_appwidget_wide);
            }
            views.setOnClickPendingIntent(R.id.widgetSurface, pendingIntent);

            if (minWidth < 100) {
                if (minHeight < 80) {
                    views.setViewVisibility(R.id.header1, GONE);
                    views.setViewVisibility(R.id.header2, GONE);

                    if (lessonCount > 0) {
                        views.setTextViewText(R.id.line1, String.format(Locale.ROOT, "L: %d", lessonCount));
                        views.setViewVisibility(R.id.line1, VISIBLE);
                    }
                    else {
                        views.setViewVisibility(R.id.line1, GONE);
                    }

                    views.setTextViewText(R.id.line2, String.format(Locale.ROOT, "R: %d", reviewCount));
                }
                else {
                    views.setViewVisibility(R.id.header2, VISIBLE);

                    if (lessonCount > 0) {
                        views.setTextViewText(R.id.header1, "Lessons:");
                        views.setViewVisibility(R.id.header1, VISIBLE);

                        views.setTextViewText(R.id.line1, Integer.toString(lessonCount));
                        views.setTextViewTextSize(R.id.line1, TypedValue.COMPLEX_UNIT_SP, 18);
                        views.setViewVisibility(R.id.line1, VISIBLE);
                    }
                    else {
                        views.setViewVisibility(R.id.header1, GONE);
                        views.setViewVisibility(R.id.line1, GONE);
                    }

                    views.setTextViewText(R.id.header2, "Reviews:");
                    views.setViewVisibility(R.id.header2, VISIBLE);

                    views.setTextViewText(R.id.line2, Integer.toString(reviewCount));
                    views.setTextViewTextSize(R.id.line2, TypedValue.COMPLEX_UNIT_SP, 18);
                }
            }
            else if (minWidth < 200) {
                if (lessonCount > 0) {
                    views.setTextViewText(R.id.header, "Lessons/reviews:");
                    views.setTextViewText(R.id.body, String.format(Locale.ROOT, "%d/%d", lessonCount, reviewCount));
                }
                else {
                    views.setTextViewText(R.id.header, "Reviews:");
                    views.setTextViewText(R.id.body, Integer.toString(reviewCount));
                }
            }
            else if (minWidth < 250) {
                if (lessonCount > 0) {
                    views.setTextViewText(R.id.leader, "L/R");
                    views.setTextViewText(R.id.body, String.format(Locale.ROOT, "%d/%d", lessonCount, reviewCount));
                }
                else {
                    views.setTextViewText(R.id.leader, "Rev");
                    views.setTextViewText(R.id.body, Integer.toString(reviewCount));
                }
            }
            else {
                if (lessonCount > 0) {
                    views.setTextViewText(R.id.leader, "Lessons/\nReviews");
                    views.setTextViewText(R.id.body, String.format(Locale.ROOT, "%d/%d", lessonCount, reviewCount));
                }
                else {
                    views.setTextViewText(R.id.leader, "Reviews");
                    views.setTextViewText(R.id.body, Integer.toString(reviewCount));
                }
            }

            if (upcomingMessage == null) {
                views.setViewVisibility(R.id.footer, GONE);
            }
            else {
                views.setViewVisibility(R.id.footer, VISIBLE);
                views.setTextViewText(R.id.footer, upcomingMessage);
            }

            manager.updateAppWidget(id, views);
        }
    }

    /**
     * Update all instances of the widget. This method retrieves the relevant data from the database,
     * and then uses the method above to actually perform the update.
     *
     * @param context Android context
     */
    private static void updateWidgets(final Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            return;
        }

        @Nullable PowerManager.WakeLock wl = null;
        final @Nullable PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wk:wkw");
            wl.acquire(Constants.MINUTE);
        }

        final @Nullable PowerManager.WakeLock heldWakeLock = wl;

        ObjectSupport.<Void, Void, NotificationContext>runAsync(null, publisher -> {
            final AppDatabase db = WkApplication.getDatabase();
            final int maxLevel = db.propertiesDao().getUserMaxLevelGranted();
            final int userLevel = db.propertiesDao().getUserLevel();
            final Date now = new Date();
            final NotificationContext ctx = db.subjectAggregatesDao().getNotificationContext(maxLevel, userLevel, now, now);
            ctx.setMoreReviewsDate(db.subjectAggregatesDao().getNextLongTermReviewDate(maxLevel, userLevel, now));

            if (heldWakeLock != null) {
                heldWakeLock.release();
            }

            return ctx;
        }, null, result -> {
            if (result != null) {
                updateWidgets(result);
            }
        });
    }

    /**
     * Does the current app installation have any deployed widgets?.
     *
     * @return true if it has
     */
    public static boolean hasWidgets() {
        return safe(false, () -> {
            final Context context = WkApplication.getInstance();
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
                final AppWidgetManager manager = AppWidgetManager.getInstance(context);
                final ComponentName name = new ComponentName(context, SessionWidgetProvider.class);
                return manager.getAppWidgetIds(name).length > 0;
            }
            return false;
        });
    }

    /**
     * Same as updateWidgets(), but will first check to see if there are any widgets to update, and
     * skip the process if there are none.
     */
    public static void checkAndUpdateWidgets() {
        safe(() -> {
            if (hasWidgets()) {
                updateWidgets(WkApplication.getInstance());
            }
        });
    }

    /**
     * Same as updateWidgets(), but will first check to see if there are any widgets to update, and
     * skip the process if there are none.
     *
     * @param ctx notification context for the lesson/review counts
     */
    public static void checkAndUpdateWidgets(final NotificationContext ctx) {
        safe(() -> {
            if (hasWidgets()) {
                updateWidgets(ctx);
            }
        });
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }

    @Override
    public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final Bundle newOptions) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }

    @Override
    public void onEnabled(final Context context) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }

    @Override
    public void onDisabled(final Context context) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }

    @Override
    public void onRestored(final Context context, final int[] oldWidgetIds, final int[] newWidgetIds) {
        safe(() -> updateWidgets(context.getApplicationContext()));
    }
}
