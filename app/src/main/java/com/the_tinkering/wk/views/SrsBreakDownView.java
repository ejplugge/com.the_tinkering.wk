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
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.enums.ActiveTheme;
import com.the_tinkering.wk.livedata.LiveSrsBreakDown;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.SrsBreakDown;
import com.the_tinkering.wk.model.SrsSystemRepository;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.WeakLcoRef;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows the SRS breakdown on the dashboard.
 */
public final class SrsBreakDownView extends ConstraintLayout {
    private final List<ViewProxy> counts = new ArrayList<>();
    private final List<ViewProxy> views = new ArrayList<>();

    private @Nullable WeakLcoRef<Actment> actmentRef = null;

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SrsBreakDownView(final Context context) {
        super(context);
        safe(this::init);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SrsBreakDownView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        safe(this::init);
    }

    /**
     * Initialize the view by observing the relevant LiveData instances.
     */
    private void init() {
        inflate(getContext(), R.layout.srs_breakdown, this);
        setBackgroundColor(ThemeUtil.getColor(R.attr.tileColorBackground));

        counts.add(new ViewProxy(this, R.id.breakdownBucket0Count));
        counts.add(new ViewProxy(this, R.id.breakdownBucket1Count));
        counts.add(new ViewProxy(this, R.id.breakdownBucket2Count));
        counts.add(new ViewProxy(this, R.id.breakdownBucket3Count));
        counts.add(new ViewProxy(this, R.id.breakdownBucket4Count));

        views.add(new ViewProxy(this, R.id.breakdownBucket0View));
        views.add(new ViewProxy(this, R.id.breakdownBucket1View));
        views.add(new ViewProxy(this, R.id.breakdownBucket2View));
        views.add(new ViewProxy(this, R.id.breakdownBucket3View));
        views.add(new ViewProxy(this, R.id.breakdownBucket4View));

        for (int i=0; i<5; i++) {
            views.get(i).setBackgroundColor(ActiveTheme.getShallowStageBucketColors5()[i]);
            if (ActiveTheme.getCurrentTheme() == ActiveTheme.LIGHT) {
                counts.get(i).setShadowLayer(3, 1, 1, Color.BLACK);
            }
            else {
                counts.get(i).setShadowLayer(0, 0, 0, 0);
            }
        }

        views.get(0).setOnClickListener(v -> safe(() -> {
            if (actmentRef == null) {
                return;
            }
            final @Nullable Actment actment = actmentRef.get();
            final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
            for (int i=0; i<SrsSystemRepository.getMaxNumApprenticeStages(); i++) {
                parameters.srsStages.add("prepass:" + i);
            }
            final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
            actment.goToSearchResult(2, searchParameters, null);
        }));

        views.get(1).setOnClickListener(v -> safe(() -> {
            if (actmentRef == null) {
                return;
            }
            final @Nullable Actment actment = actmentRef.get();
            final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
            for (int i=0; i<SrsSystemRepository.getMaxNumGuruStages(); i++) {
                parameters.srsStages.add("pass:" + i);
            }
            final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
            actment.goToSearchResult(2, searchParameters, null);
        }));

        views.get(2).setOnClickListener(v -> safe(() -> {
            if (actmentRef == null) {
                return;
            }
            final @Nullable Actment actment = actmentRef.get();
            final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
            parameters.srsStages.add("master");
            final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
            actment.goToSearchResult(2, searchParameters, null);
        }));

        views.get(3).setOnClickListener(v -> safe(() -> {
            if (actmentRef == null) {
                return;
            }
            final @Nullable Actment actment = actmentRef.get();
            final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
            parameters.srsStages.add("enlightened");
            final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
            actment.goToSearchResult(2, searchParameters, null);
        }));

        views.get(4).setOnClickListener(v -> safe(() -> {
            if (actmentRef == null) {
                return;
            }
            final @Nullable Actment actment = actmentRef.get();
            final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
            parameters.srsStages.add("burned");
            final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
            actment.goToSearchResult(2, searchParameters, null);
        }));
    }

    /**
     * Set the lifecycle owner for this view, to hook LiveData observers to.
     *
     * @param actment the lifecycle owner
     */
    public void setLifecycleOwner(final Actment actment) {
        actmentRef = new WeakLcoRef<>(actment);
        safe(() -> LiveSrsBreakDown.getInstance().observe(actment, t -> safe(() -> {
            if (t != null) {
                update(t);
            }
        })));
    }

    /**
     * Update the text based on the latest SrsBreakDown data available.
     *
     * @param srsBreakDown the SRS breakdown summary
     */
    private void update(final SrsBreakDown srsBreakDown) {
        if (GlobalSettings.getFirstTimeSetup() == 0 || !GlobalSettings.Dashboard.getShowSrsBreakdown()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        for (int i=0; i<5; i++) {
            counts.get(i).setText(srsBreakDown.getSrsBreakdownCount(i));
        }
    }
}
