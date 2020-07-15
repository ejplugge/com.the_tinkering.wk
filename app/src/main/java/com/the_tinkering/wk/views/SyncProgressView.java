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

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveApiProgress;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveTaskCounts;
import com.the_tinkering.wk.util.Logger;

import java.util.Locale;

/**
 * A custom view that describes the progress of background tasks.
 */
public final class SyncProgressView extends AppCompatTextView {
    private static final Logger LOGGER = Logger.get(SyncProgressView.class);

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SyncProgressView(final Context context) {
        super(context, null, R.attr.WK_TextView_Normal);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SyncProgressView(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.attr.WK_TextView_Normal);
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        try {
            LiveApiProgress.getInstance().observe(lifecycleOwner, t -> {
                try {
                    update();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            LiveTaskCounts.getInstance().observe(lifecycleOwner, t -> {
                try {
                    update();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });

            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, t -> {
                try {
                    update();
                } catch (final Exception e) {
                    LOGGER.uerr(e);
                }
            });
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Update the text.
     */
    private void update() {
        if (LiveTaskCounts.getInstance().get().isEmpty()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        if (LiveApiProgress.getShow()) {
            setText(String.format(Locale.ROOT, "Sync: %d/%d %s", LiveApiProgress.getNumProcessedEntities(),
                    LiveApiProgress.getNumEntities(), LiveApiProgress.getEntityName()));
        }
        else if (LiveApiProgress.getInstance().getSyncReminder()) {
            setText("You may have pending unlocks - don't forget to sync");
        }
        else {
            setText("");
        }
    }
}
