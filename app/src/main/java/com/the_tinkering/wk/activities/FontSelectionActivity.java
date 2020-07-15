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

package com.the_tinkering.wk.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TableLayout;

import androidx.core.content.res.ResourcesCompat;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.model.TypefaceConfiguration;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.FontStorageUtil;
import com.the_tinkering.wk.views.FontSelectionRowView;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * An activity for choosing fonts for quiz questions.
 */
public final class FontSelectionActivity extends AbstractActivity {
    private final ViewProxy fontList = new ViewProxy();

    /**
     * The constructor.
     */
    public FontSelectionActivity() {
        super(R.layout.activity_font_selection, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        fontList.setDelegate(this, R.id.fontList);

        while (fontList.getChildCount() > 1) {
            fontList.removeViewAt(1);
        }

        addStaticRow(fontList, 1, -1, "Android default");
        addStaticRow(fontList, 2, R.font.sawarabi_mincho_medium, "Sawarabi Mincho");
        addStaticRow(fontList, 3, R.font.sawarabi_gothic_medium, "Sawarabi Gothic");
        addStaticRow(fontList, 4, R.font.mplus_1p_regular, "M⁺ P Type-1");
        addStaticRow(fontList, 5, R.font.kosugi_regular, "Kosugi");
        addStaticRow(fontList, 6, R.font.kosugi_maru_regular, "Kosugi Maru");
        addStaticRow(fontList, 7, R.font.otsutomefont_ver3, "Otsutome");
        addStaticRow(fontList, 8, R.font.gochikakutto, "851 Gochikakutto");

        //noinspection SimplifyStreamApiCallChains
        FontStorageUtil.getNames().stream().forEach(name -> addDynamicRow(fontList, name));
    }

    @Override
    protected void onResumeLocal() {
        //
    }

    @Override
    protected void onPauseLocal() {
        //
    }

    @Override
    protected void enableInteractionLocal() {
        //
    }

    @Override
    protected void disableInteractionLocal() {
        //
    }

    private void addStaticRow(final ViewProxy layout, final int fontId, final int fontResourceId, final CharSequence name) {
        final FontSelectionRowView row = new FontSelectionRowView(this);
        row.setFontId(Integer.toString(fontId));
        row.setFontName(name);
        if (fontResourceId != -1) {
            final @Nullable Typeface typeface = ResourcesCompat.getFont(this, fontResourceId);
            row.setTypeface(typeface == null ? Typeface.DEFAULT : typeface);
        }
        final TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams(0, 0);
        rowLayoutParams.setMargins(0, dp2px(4), 0, 0);
        rowLayoutParams.width = MATCH_PARENT;
        rowLayoutParams.height = WRAP_CONTENT;
        layout.addView(row, rowLayoutParams);
    }

    private void addDynamicRow(final ViewProxy layout, final String name) {
        final @Nullable TypefaceConfiguration typefaceConfiguration = FontStorageUtil.getTypefaceConfiguration(name);
        if (typefaceConfiguration == null) {
            return;
        }

        final FontSelectionRowView row = new FontSelectionRowView(this);
        row.setFontId(name);
        row.setFontName(name);
        row.setTypeface(typefaceConfiguration.getTypeface());
        final TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams(0, 0);
        rowLayoutParams.setMargins(0, dp2px(4), 0, 0);
        rowLayoutParams.width = MATCH_PARENT;
        rowLayoutParams.height = WRAP_CONTENT;
        layout.addView(row, rowLayoutParams);
    }
}
