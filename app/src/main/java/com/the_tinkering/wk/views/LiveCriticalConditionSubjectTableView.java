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

package com.the_tinkering.wk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.livedata.LiveCriticalCondition;

import java.util.List;
import java.util.Locale;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows the critical condition list on the dashboard.
 */
public final class LiveCriticalConditionSubjectTableView extends LiveSubjectTableView {
    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LiveCriticalConditionSubjectTableView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LiveCriticalConditionSubjectTableView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            final TextView textView = (TextView) ((ViewGroup) getChildAt(0)).getChildAt(0);
            textView.setText("Critical condition items");
            textView.setTextSize(GlobalSettings.Font.getFontSizeLiveSubjectTable());
        });
    }

    @Override
    protected void registerObserver(final LifecycleOwner lifecycleOwner, final Observer<? super List<Subject>> observer) {
        safe(() -> LiveCriticalCondition.getInstance().observe(lifecycleOwner, observer));
    }

    @Override
    protected boolean canShow() {
        return safe(false, GlobalSettings.Dashboard::getShowCriticalCondition);
    }

    @Override
    protected String getExtraText(final Subject subject) {
        return safe("", () -> String.format(Locale.ROOT, "%d%%", subject.getPercentageCorrect()));
    }
}
