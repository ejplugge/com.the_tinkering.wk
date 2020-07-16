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
import android.widget.TableRow;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a row in the overview of imported fonts.
 */
public final class FontImportRowView extends TableRow {
    private @Nullable String name;

    private final ViewProxy fontName = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public FontImportRowView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public FontImportRowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.font_import_row, this);
            setVisibility(GONE);
            fontName.setDelegate(this, R.id.fontName);
        });
    }

    /**
     * The file name for this font file.
     * @return the value
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * The file name for this font file.
     * @param name the value
     */
    public void setName(final String name) {
        safe(() -> {
            this.name = name;
            fontName.setText(name);
            setVisibility(VISIBLE);
        });
    }
}
