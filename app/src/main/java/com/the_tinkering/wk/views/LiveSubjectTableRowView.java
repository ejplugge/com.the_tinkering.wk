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

import androidx.arch.core.util.Function;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.proxy.ViewProxy;

import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static com.the_tinkering.wk.util.TextUtil.renderHtml;

/**
 * A custom view for a row in a LiveSubjectTableView.
 */
public final class LiveSubjectTableRowView extends TableRow {
    private final ViewProxy rowText = new ViewProxy();
    private final ViewProxy rowExtra = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public LiveSubjectTableRowView(final Context context) {
        super(context);
        init();
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public LiveSubjectTableRowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the view.
     */
    private void init() {
        safe(() -> {
            inflate(getContext(), R.layout.live_subject_table_row, this);
            rowText.setDelegate(this, R.id.rowText);
            rowExtra.setDelegate(this, R.id.rowExtra);
        });
    }

    /**
     * Set the subject for this row, supplying a callback to get the relevant extra text when needed.
     *
     * @param subject the subject
     * @param extraTextFunction the function producing the extra text
     */
    public void setSubject(final Subject subject, final Function<? super Subject, String> extraTextFunction) {
        safe(() -> {
            setBackgroundColor(subject.getBackgroundColor());
            rowText.setBackgroundColor(subject.getBackgroundColor());
            rowExtra.setBackgroundColor(subject.getBackgroundColor());

            rowText.setTextColor(subject.getTextColor());
            rowExtra.setTextColor(subject.getTextColor());

            rowText.setTextSize(GlobalSettings.Font.getFontSizeLiveSubjectTable());
            rowExtra.setTextSize(GlobalSettings.Font.getFontSizeLiveSubjectTable());

            rowText.setText(renderHtml(subject.getCharactersHtml()));
            rowText.setJapaneseLocale();
            rowExtra.setText(extraTextFunction.apply(subject));
        });
    }
}
