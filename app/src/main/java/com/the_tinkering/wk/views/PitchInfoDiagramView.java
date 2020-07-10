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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.the_tinkering.wk.Constants;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.util.Logger;
import com.the_tinkering.wk.util.ThemeUtil;
import com.the_tinkering.wk.util.ViewUtil;

import javax.annotation.Nullable;

/**
 * A custom view for a pitch info diagram. It sizes and aligns itself to fit over a TextView containing the
 * same reading.
 */
public final class PitchInfoDiagramView extends View {
    private static final Logger LOGGER = Logger.get(PitchInfoDiagramView.class);

    private static final String KANA_DIGRAPHS = "ぁぃぅぇぉゃゅょゎゕゖァィゥェォャュョヮヵヶ";
    private String text = "";
    private int textSize = Constants.FONT_SIZE_NORMAL;
    private int pitchNumber = 0;
    private boolean dirty = true;

    /**
     * The X-offsets of each mora in the reading string. The mora are numbered 0 .. n-1.
     * The width or mora i is xpos[i+1] - xpos[i]. The placeholder for the following particle is
     * directly after the last mora. To get proper alignment, the text bounds measurement is
     * done with an extra kana character at the start and end of the reading text. Its size
     * is reflected in the xpos numbers, so to get absolute offsets, xpos[0] must be subtracted
     * so the first mora starts at X-offset 0.
     */
    private int[] xpos = new int[0];

    private int height = 0;
    private int circleRadius = 0;
    private int lineThickness = 0;
    private int color = 0;
    private int backgroundColor = 0;
    private int numMora = 0;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect rect = new Rect();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public PitchInfoDiagramView(final Context context) {
        super(context);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public PitchInfoDiagramView(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     * @param defStyleAttr default style attribute
     */
    public PitchInfoDiagramView(final Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Get the width of a string in pixels, if rendered by the current paint.
     * The actually measured string has an extra kana at the start and end to get
     * clean alignment.
     *
     * @param value the string to test
     * @return the width in pixels
     */
    @SuppressWarnings("StringConcatenationMissingWhitespace")
    private int getWidth(final CharSequence value) {
        paint.getTextBounds("あ" + value + "あ", 0, value.length()+2, rect);
        return rect.width();
    }

    /**
     * Precompute the details of this instance, to speed up later drawing and measuring.
     */
    private void prepare() {
        if (!dirty) {
            return;
        }
        dirty = false;

        paint.setTextSize(sp2px(textSize));
        paint.setTypeface(Typeface.DEFAULT);
        ViewUtil.setJapaneseLocale(paint);

        circleRadius = dp2px(3);
        lineThickness = dp2px(2);
        final Paint.FontMetrics metrics = paint.getFontMetrics();
        height = (int) (Math.abs(metrics.ascent) + Math.abs(metrics.descent) + metrics.leading);

        xpos = new int[text.length() + 2];
        xpos[0] = getWidth("");
        numMora = 0;
        int i = 0;
        while (i < text.length()-1) {
            i += KANA_DIGRAPHS.indexOf(text.charAt(i+1)) >= 0 ? 2 : 1;
            numMora++;
            xpos[numMora] = getWidth(text.substring(0, i));
        }
        if (i < text.length()) {
            numMora++;
            xpos[numMora] = getWidth(text);
        }
        xpos[numMora + 1] = getWidth(text + "あ");

        if (pitchNumber == 0) {
            color = ThemeUtil.getColor(R.attr.pitchInfoHeibanColor);
        }
        else if (pitchNumber == 1) {
            color = ThemeUtil.getColor(R.attr.pitchInfoAtamadakaColor);
        }
        else if (pitchNumber == numMora) {
            color = ThemeUtil.getColor(R.attr.pitchInfoOdakaColor);
        }
        else {
            color = ThemeUtil.getColor(R.attr.pitchInfoNakadakaColor);
        }
        backgroundColor = ThemeUtil.getColor(R.attr.colorBackground);
    }

    /**
     * Is the dot/node for a specific mora drawn high or low?.
     *
     * @param mora the index of the mora, 0 .. n inclusive (may point to following particle)
     * @return true if the dot should be drawn high
     */
    private boolean isDotHigh(final int mora) {
        if (pitchNumber == 0) {
            return mora != 0;
        }
        if (pitchNumber == 1) {
            return mora == 0;
        }
        return mora > 0 && mora < pitchNumber;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        try {
            super.onDraw(canvas);
            prepare();

            int prevX = -1;
            int prevY = -1;

            for (int i=0; i<=numMora; i++) {
                final int x = getPaddingLeft() + (xpos[i] + xpos[i+1]) / 2 - xpos[0];
                final int y = getPaddingTop() + (isDotHigh(i) ? circleRadius : height - circleRadius);
                if (prevX != -1) {
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    paint.setStrokeWidth(lineThickness);
                    canvas.drawLine(prevX, prevY, x, y, paint);
                }
                prevX = x;
                prevY = y;
            }

            for (int i=0; i<=numMora; i++) {
                final int x = getPaddingLeft() + (xpos[i] + xpos[i+1]) / 2 - xpos[0];
                final int y = getPaddingTop() + (isDotHigh(i) ? circleRadius : height - circleRadius);
                if (i == numMora) {
                    paint.setColor(backgroundColor);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    paint.setStrokeWidth(0);
                    canvas.drawCircle(x, y, circleRadius, paint);
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.STROKE);
                }
                else {
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                }
                paint.setStrokeWidth(0);
                canvas.drawCircle(x, y, circleRadius, paint);
            }
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        try {
            prepare();
            setMeasuredDimension(
                    xpos[numMora+1] - xpos[0] + getPaddingLeft() + getPaddingRight() + circleRadius * 2,
                    height + getPaddingTop() + getPaddingBottom());
        } catch (final Exception e) {
            LOGGER.uerr(e);
            setMeasuredDimension(0, 0);
        }
    }

    /**
     * Set the reading text this instance is based on.
     *
     * @param text the reading text
     */
    public void setText(final String text) {
        try {
            this.text = text;
            dirty = true;
            invalidate();
            requestLayout();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Set the text (font) size.
     *
     * @param textSize the size in SP
     */
    public void setTextSize(final int textSize) {
        try {
            this.textSize = textSize;
            dirty = true;
            invalidate();
            requestLayout();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Set the pitch number for this diagram.
     *
     * @param pitchNumber the pitch number
     */
    public void setPitchNumber(final int pitchNumber) {
        try {
            this.pitchNumber = pitchNumber;
            dirty = true;
            invalidate();
            requestLayout();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Get the number of mora in this reading.
     *
     * @return the number
     */
    public int getNumMora() {
        try {
            prepare();
            return numMora;
        } catch (final Exception e) {
            LOGGER.uerr(e);
            return 0;
        }
    }

    private int dp2px(final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int sp2px(final int sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity);
    }
}
