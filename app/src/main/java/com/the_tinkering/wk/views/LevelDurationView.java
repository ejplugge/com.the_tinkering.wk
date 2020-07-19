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

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.model.LevelDuration;

import java.util.Locale;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows the user's level and how long they've been on that level.
 */
public final class LevelDurationView extends AppCompatTextView {
    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LevelDurationView(final Context context) {
        super(context, null, R.attr.WK_TextView_Normal);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LevelDurationView(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.WK_TextView_Normal);
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        safe(() -> LiveLevelDuration.getInstance().observe(lifecycleOwner, t -> safe(() -> {
            if (t != null) {
                update(t);
            }
        })));
    }

    /**
     * Update the text based on the latest LevelDuration data available.
     *
     * @param levelDuration the level duration
     */
    private void update(final LevelDuration levelDuration) {
        if (GlobalSettings.getFirstTimeSetup() == 0) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        setText(String.format(Locale.ROOT, "You are level %d. Your time on this level is %.1f days.",
                levelDuration.getLevel(), levelDuration.getDaysAtCurrentLevel()));
    }
}
