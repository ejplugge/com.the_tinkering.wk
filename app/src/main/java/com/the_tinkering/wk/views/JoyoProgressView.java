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
import com.the_tinkering.wk.livedata.LiveJoyoProgress;
import com.the_tinkering.wk.model.JoyoProgress;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a breakdown of Joyo progress.
 */
public final class JoyoProgressView extends TableLayout {
    private final List<ViewProxy> locked = new ArrayList<>();
    private final List<ViewProxy> prePassed = new ArrayList<>();
    private final List<ViewProxy> passed = new ArrayList<>();
    private final List<ViewProxy> burned = new ArrayList<>();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public JoyoProgressView(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public JoyoProgressView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        inflate(getContext(), R.layout.joyo_progress, this);

        locked.add(new ViewProxy(this, R.id.joyoGrade1Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade2Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade3Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade4Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade5Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade6Locked));
        locked.add(new ViewProxy(this, R.id.joyoGrade7Locked));

        prePassed.add(new ViewProxy(this, R.id.joyoGrade1PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade2PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade3PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade4PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade5PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade6PrePassed));
        prePassed.add(new ViewProxy(this, R.id.joyoGrade7PrePassed));

        passed.add(new ViewProxy(this, R.id.joyoGrade1Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade2Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade3Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade4Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade5Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade6Passed));
        passed.add(new ViewProxy(this, R.id.joyoGrade7Passed));

        burned.add(new ViewProxy(this, R.id.joyoGrade1Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade2Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade3Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade4Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade5Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade6Burned));
        burned.add(new ViewProxy(this, R.id.joyoGrade7Burned));

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
        safe(() -> LiveJoyoProgress.getInstance().observe(lifecycleOwner, t -> safe(() -> update(t))));
    }

    /**
     * Update the table based on the latest progress data.
     *
     * @param joyoProgress the progress
     */
    private void update(final @Nullable JoyoProgress joyoProgress) {
        if (joyoProgress == null || LiveFirstTimeSetup.getInstance().get() == 0 || !GlobalSettings.Dashboard.getShowJoyoProgress()) {
            setVisibility(GONE);
            return;
        }

        for (int i=0; i<7; i++) {
            locked.get(i).setTextOrBlankIfZero(joyoProgress.getLocked(i+1));
            prePassed.get(i).setTextOrBlankIfZero(joyoProgress.getPrePassed(i+1));
            passed.get(i).setTextOrBlankIfZero(joyoProgress.getPassed(i+1));
            burned.get(i).setTextOrBlankIfZero(joyoProgress.getBurned(i+1));
        }

        setVisibility(VISIBLE);
    }
}
