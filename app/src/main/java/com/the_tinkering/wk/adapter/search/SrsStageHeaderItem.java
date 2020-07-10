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

package com.the_tinkering.wk.adapter.search;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.model.SrsSystem;

/**
 * Item for SRS stage headers.
 */
public final class SrsStageHeaderItem extends HeaderItem {
    private final SrsSystem.Stage stage;

    /**
     * The constructor.
     *
     * @param stage the subject's stage
     */
    public SrsStageHeaderItem(final SrsSystem.Stage stage) {
        super(stage.getAdvancedSearchTag());
        this.stage = stage;
    }

    /**
     * The stage this header is for. Actually, this is one representative among possibly many that have the same tag.
     *
     * @return the stage
     */
    public SrsSystem.Stage getStage() {
        return stage;
    }

    @Override
    public int getViewType() {
        return R.id.viewTypeSrsStageHeader;
    }

    @Override
    public int getSpanSize(final int spans) {
        return spans;
    }
}
