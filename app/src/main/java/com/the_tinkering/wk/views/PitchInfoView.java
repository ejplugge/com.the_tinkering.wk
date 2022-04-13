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
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.model.PitchInfo;
import com.the_tinkering.wk.util.ViewUtil;

import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
import static com.the_tinkering.wk.Constants.FONT_SIZE_NORMAL;
import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Custom view that shows the pitch info for one reading.
 */
public final class PitchInfoView extends ConstraintLayout {
    private int textSize = FONT_SIZE_NORMAL;

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public PitchInfoView(final Context context) {
        super(context);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public PitchInfoView(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr the default style
     */
    public PitchInfoView(final Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * The normal text size for the text in the headline.
     * @param textSize the value
     */
    public void setTextSize(final int textSize) {
        this.textSize = textSize;
    }

    private void setPitchInfoImpl(final String kana, final Iterable<PitchInfo> pitchInfos) {
        removeAllViews();

        LayoutParams params;

        int lastDiagram = -1;

        for (final PitchInfo info: pitchInfos) {
            final PitchInfoDiagramView diagramView = new PitchInfoDiagramView(getContext());
            diagramView.setId(ViewCompat.generateViewId());
            diagramView.setPitchNumber(info.getPitchNumber());
            diagramView.setText(kana);
            diagramView.setTextSize(textSize + 4);
            params = new LayoutParams(0, 0);
            params.width = WRAP_CONTENT;
            params.height = WRAP_CONTENT;
            params.setMargins(0, dp2px(4), 0, 0);
            if (lastDiagram == -1) {
                params.topToTop = PARENT_ID;
            }
            else {
                params.topToBottom = lastDiagram;
            }
            params.startToStart = PARENT_ID;
            addView(diagramView, params);

            final StringBuilder sb = new StringBuilder();
            sb.append('[').append(info.getPitchNumber()).append("] ");
            switch (info.getPitchNumber()) {
                case 0:
                    sb.append("平板");
                    break;
                case 1:
                    sb.append("頭高");
                    break;
                default:
                    if (info.getPitchNumber() == diagramView.getNumMora()) {
                        sb.append("尾高");
                    }
                    else {
                        sb.append("中高");
                    }
                    break;
            }
            if (!isEmpty(info.getPartOfSpeech())) {
                sb.append(" (").append(info.getPartOfSpeech()).append(')');
            }

            final TextView textView = new TextView(getContext(), null, R.attr.WK_TextView_Normal);
            textView.setText(sb.toString());
            textView.setTextSize(textSize + 4);
            ViewUtil.setJapaneseLocale(textView);
            params = new LayoutParams(0, 0);
            params.width = WRAP_CONTENT;
            params.height = WRAP_CONTENT;
            if (lastDiagram == -1) {
                params.topToTop = PARENT_ID;
            }
            else {
                params.topToBottom = lastDiagram;
            }
            params.startToEnd = diagramView.getId();
            addView(textView, params);

            lastDiagram = diagramView.getId();
        }

        final TextView textView = new TextView(getContext(), null, R.attr.WK_TextView_Normal);
        textView.setText(kana);
        textView.setTextSize(textSize + 4);
        ViewUtil.setJapaneseLocale(textView);
        params = new LayoutParams(0, 0);
        params.width = WRAP_CONTENT;
        params.height = WRAP_CONTENT;
        if (lastDiagram == -1) {
            params.topToTop = PARENT_ID;
        }
        else {
            params.topToBottom = lastDiagram;
        }
        params.startToStart = PARENT_ID;
        addView(textView, params);
    }

    /**
     * Set the kana reading and pitch info records for this view.
     *
     * @param kana the reading as a string of kana
     * @param pitchInfos the list of applicable pitch info records
     */
    public void setPitchInfo(final String kana, final Iterable<PitchInfo> pitchInfos) {
        safe(() -> setPitchInfoImpl(kana, pitchInfos));
    }

    private int dp2px(@SuppressWarnings("SameParameterValue") final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
