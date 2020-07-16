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
import android.widget.TableLayout;

import androidx.lifecycle.LifecycleOwner;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.livedata.LiveFirstTimeSetup;
import com.the_tinkering.wk.livedata.LiveJlptProgress;
import com.the_tinkering.wk.model.JlptProgress;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a breakdown of JLPT progress.
 */
public final class JlptProgressView extends TableLayout {
    private final List<ViewProxy> locked = new ArrayList<>();
    private final List<ViewProxy> prePassed = new ArrayList<>();
    private final List<ViewProxy> passed = new ArrayList<>();
    private final List<ViewProxy> burned = new ArrayList<>();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public JlptProgressView(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public JlptProgressView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        inflate(getContext(), R.layout.jlpt_progress, this);

        locked.add(new ViewProxy(this, R.id.jlptLevel1Locked));
        locked.add(new ViewProxy(this, R.id.jlptLevel2Locked));
        locked.add(new ViewProxy(this, R.id.jlptLevel3Locked));
        locked.add(new ViewProxy(this, R.id.jlptLevel4Locked));
        locked.add(new ViewProxy(this, R.id.jlptLevel5Locked));

        prePassed.add(new ViewProxy(this, R.id.jlptLevel1PrePassed));
        prePassed.add(new ViewProxy(this, R.id.jlptLevel2PrePassed));
        prePassed.add(new ViewProxy(this, R.id.jlptLevel3PrePassed));
        prePassed.add(new ViewProxy(this, R.id.jlptLevel4PrePassed));
        prePassed.add(new ViewProxy(this, R.id.jlptLevel5PrePassed));

        passed.add(new ViewProxy(this, R.id.jlptLevel1Passed));
        passed.add(new ViewProxy(this, R.id.jlptLevel2Passed));
        passed.add(new ViewProxy(this, R.id.jlptLevel3Passed));
        passed.add(new ViewProxy(this, R.id.jlptLevel4Passed));
        passed.add(new ViewProxy(this, R.id.jlptLevel5Passed));

        burned.add(new ViewProxy(this, R.id.jlptLevel1Burned));
        burned.add(new ViewProxy(this, R.id.jlptLevel2Burned));
        burned.add(new ViewProxy(this, R.id.jlptLevel3Burned));
        burned.add(new ViewProxy(this, R.id.jlptLevel4Burned));
        burned.add(new ViewProxy(this, R.id.jlptLevel5Burned));

        setColumnShrinkable(0, true);
        setColumnShrinkable(1, true);
        setColumnShrinkable(2, true);
        setColumnShrinkable(3, true);
        setColumnShrinkable(4, true);
        setColumnStretchable(0, true);

        setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param lifecycleOwner the lifecycle owner
     */
    public void setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        safe(() -> {
            LiveJlptProgress.getInstance().observe(lifecycleOwner, t -> safe(() -> update(t)));
            LiveFirstTimeSetup.getInstance().observe(lifecycleOwner, t -> safe(() -> LiveJlptProgress.getInstance().ping()));
        });
    }

    /**
     * Update the table based on the latest progress data.
     *
     * @param jlptProgress the progress
     */
    private void update(final @Nullable JlptProgress jlptProgress) {
        if (jlptProgress == null || LiveFirstTimeSetup.getInstance().get() == 0 || !GlobalSettings.Dashboard.getShowJlptProgress()) {
            setVisibility(GONE);
            return;
        }

        for (int i=0; i<5; i++) {
            locked.get(i).setTextOrBlankIfZero(jlptProgress.getLocked(i+1));
            prePassed.get(i).setTextOrBlankIfZero(jlptProgress.getPrePassed(i+1));
            passed.get(i).setTextOrBlankIfZero(jlptProgress.getPassed(i+1));
            burned.get(i).setTextOrBlankIfZero(jlptProgress.getBurned(i+1));
        }

        setVisibility(VISIBLE);
    }
}
