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
import android.widget.TableRow;

import com.the_tinkering.wk.Actment;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.Converters;
import com.the_tinkering.wk.enums.SearchSortOrder;
import com.the_tinkering.wk.livedata.LiveLevelDuration;
import com.the_tinkering.wk.model.AdvancedSearchParameters;
import com.the_tinkering.wk.model.LevelProgress;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.WeakLcoRef;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a single bar in the level progress chart on the dashboard.
 */
public final class LevelProgressRowView extends TableRow {
    private final ViewProxy label = new ViewProxy();
    private final ViewProxy barView = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LevelProgressRowView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LevelProgressRowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.level_progress_row, this);
            label.setDelegate(this, R.id.label);
            barView.setDelegate(this, R.id.bar);
        });
    }

    /**
     * Set the bar details for this instance.
     *
     * @param actmentRef the lifecycle owner
     * @param entry the details for the bar view
     */
    public void setEntry(final @Nullable WeakLcoRef<? extends Actment> actmentRef, final LevelProgress.BarEntry entry) {
        safe(() -> {
            label.setTextFormat("Lvl %d %s", entry.getLevel(), entry.getType().getLevelProgressLabel());
            barView.setValues(entry.getBuckets());
            barView.setShowTarget(LiveLevelDuration.getInstance().get().getLevel() == entry.getLevel() && entry.getType().hasLevelUpTarget());
            setOnClickListener(v -> safe(() -> {
                if (actmentRef == null) {
                    return;
                }
                final AdvancedSearchParameters parameters = new AdvancedSearchParameters();
                parameters.minLevel = entry.getLevel();
                parameters.maxLevel = entry.getLevel();
                parameters.itemTypes.add(entry.getType());
                parameters.sortOrder = SearchSortOrder.STAGE_TYPE;
                final String searchParameters = Converters.getObjectMapper().writeValueAsString(parameters);
                actmentRef.get().goToSearchResult(2, searchParameters, null);
            }));
        });
    }
}
