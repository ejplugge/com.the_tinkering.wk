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
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows one row in the font selection activity.
 */
public final class FontSelectionRowView extends ConstraintLayout {
    private final ViewProxy fontName = new ViewProxy();
    private final ViewProxy fontSample = new ViewProxy();
    private final ViewProxy fontSwitch = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public FontSelectionRowView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public FontSelectionRowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr the default style
     */
    public FontSelectionRowView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.font_selection_row_view, this);
            fontName.setDelegate(this, R.id.fontName);
            fontSample.setDelegate(this, R.id.fontSample);
            fontSwitch.setDelegate(this, R.id.fontSwitch);

            fontSample.setJapaneseLocale();
        });
    }

    /**
     * Set the font ID for this row, which is used to register a selection in the settings.
     *
     * @param fontId the font ID
     */
    public void setFontId(final String fontId) {
        safe(() -> {
            fontSwitch.setChecked(GlobalSettings.Font.isFontSelected(fontId));
            fontSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> safe(() -> GlobalSettings.Font.setFontSelected(fontId, isChecked)));
        });
    }

    /**
     * Set the name of this font.
     *
     * @param name the name
     */
    public void setFontName(final CharSequence name) {
        safe(() -> fontName.setText(name));
    }

    /**
     * Set the typeface to preview the font.
     *
     * @param typeface the typeface
     */
    public void setTypeface(final Typeface typeface) {
        safe(() -> fontSample.setTypeface(typeface));
    }
}
