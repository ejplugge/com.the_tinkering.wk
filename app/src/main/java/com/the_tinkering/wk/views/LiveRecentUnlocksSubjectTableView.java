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

package com.the_tinkering.wk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveRecentUnlocks;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows recent unlocks on the dashboard.
 */
public final class LiveRecentUnlocksSubjectTableView extends LiveSubjectTableView {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.ENGLISH);

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LiveRecentUnlocksSubjectTableView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LiveRecentUnlocksSubjectTableView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            final TextView textView = (TextView) ((ViewGroup) getChildAt(0)).getChildAt(0);
            textView.setText("Recent unlocks in the last 30 days");
            textView.setTextSize(GlobalSettings.Font.getFontSizeLiveSubjectTable());
        });
    }

    @Override
    protected void registerObserver(final LifecycleOwner lifecycleOwner, final Observer<? super List<Subject>> observer) {
        safe(() -> {
            LiveRecentUnlocks.getInstance().observe(lifecycleOwner, observer);
            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, t -> safe(() -> LiveRecentUnlocks.getInstance().ping()));
        });
    }

    @Override
    protected boolean canShow() {
        return safe(false, GlobalSettings.Dashboard::getShowRecentUnlocks);
    }

    @Override
    protected String getExtraText(final Subject subject) {
        return safe("", () -> {
            if (subject.getUnlockedAt() == null) {
                return "";
            }
            //noinspection AccessToNonThreadSafeStaticField
            return DATE_FORMAT.format(subject.getUnlockedAt());
        });
    }
}
