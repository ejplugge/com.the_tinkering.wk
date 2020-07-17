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

package com.the_tinkering.wk.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;

import androidx.core.text.HtmlCompat;

import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.components.WaniKaniTagHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.FONT_SIZE_NORMAL;
import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.isTrue;

/**
 * Utility class for dealing with text rendering.
 */
public final class TextUtil {
    private static final Logger LOGGER = Logger.get(TextUtil.class);

    /**
     * A map to indicate if a font can display the tile-like wavy line that some
     * vocab subjects use.
     */
    private static final Map<Typeface, Boolean> TILDE_CAPABLE = new HashMap<>();

    private static final Pattern AMP_PATTERN = Pattern.compile("&");
    private static final Pattern LT_PATTERN = Pattern.compile("<");
    private static final Pattern GT_PATTERN = Pattern.compile(">");

    private TextUtil() {
        //
    }

    /**
     * Determine if the typeface has non-empty glyphs for every character in s.
     *
     * @param typeface the typeface to test
     * @param s the text to be rendered
     * @return true if it has
     */
    public static boolean hasGlyphs(final Typeface typeface, final @Nullable String s) {
        if (isEmpty(s)) {
            return true;
        }
        int i = 0;
        while (i < s.length()) {
            if (i < s.length() - 1 && Character.isHighSurrogate(s.charAt(i)) && Character.isLowSurrogate(s.charAt(i+1))) {
                if (!hasGlyph(typeface, s.substring(i, i+2))) {
                    return false;
                }
                i += 2;
            }
            else {
                if (!hasGlyph(typeface, s.substring(i, i+1))) {
                    return false;
                }
                i++;
            }
        }
        return true;
    }

    /**
     * Is the pixel array all empty or all filled?.
     *
     * @param pixels the pixels to test
     * @return true if it is
     */
    private static boolean emptyPixels(final int[] pixels) {
        boolean all0 = true;
        boolean all1 = true;
        for (final int i: pixels) {
            if (i != 0) {
                all0 = false;
            }
            if (i != -1) {
                all1 = false;
            }
        }
        return all0 || all1;
    }

    /**
     * Determine if the typeface has a non-empty glyph for a character.
     *
     * @param typeface the typeface to test
     * @param c the single-character string to test
     * @return true if it has
     */
    private static boolean hasGlyph(final Typeface typeface, final String c) {
        try {
            final int[] tofuPixels = render(typeface, "\\uFFFE");
            final int[] actualPixels = render(typeface, c);
            return !emptyPixels(actualPixels) && !Arrays.equals(actualPixels, tofuPixels);
        }
        catch (final Exception e) {
            LOGGER.error(e, "Exception during hasGlyph");
            return false;
        }
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private static int[] renderPre23(final Typeface typeface, final String c) {
        final TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(FONT_SIZE_NORMAL);
        textPaint.setColor(0xFF000000);

        final Bitmap bitmap = Bitmap.createBitmap(FONT_SIZE_NORMAL, FONT_SIZE_NORMAL, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFFFFFFF);

        final StaticLayout layout = new StaticLayout(c, textPaint, FONT_SIZE_NORMAL, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);

        final int[] pixels = new int[FONT_SIZE_NORMAL * FONT_SIZE_NORMAL];
        bitmap.getPixels(pixels, 0, FONT_SIZE_NORMAL, 0, 0, FONT_SIZE_NORMAL, FONT_SIZE_NORMAL);
        return pixels;
    }

    @TargetApi(23)
    private static int[] renderPost23(final Typeface typeface, final CharSequence c) {
        final TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(FONT_SIZE_NORMAL);
        textPaint.setColor(0xFF000000);

        final Bitmap bitmap = Bitmap.createBitmap(FONT_SIZE_NORMAL, FONT_SIZE_NORMAL, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFFFFFFF);

        final StaticLayout.Builder builder = StaticLayout.Builder.obtain(c, 0, c.length(), textPaint, FONT_SIZE_NORMAL);
        final StaticLayout layout = builder.build();
        layout.draw(canvas);

        final int[] pixels = new int[FONT_SIZE_NORMAL * FONT_SIZE_NORMAL];
        bitmap.getPixels(pixels, 0, FONT_SIZE_NORMAL, 0, 0, FONT_SIZE_NORMAL, FONT_SIZE_NORMAL);
        return pixels;
    }

    /**
     * Render a character into an array of pixels for testing.
     *
     * @param typeface the typeface to render in
     * @param c the single-character string to render
     * @return the pixels
     */
    private static int[] render(final Typeface typeface, final String c) {
        //noinspection IfMayBeConditional
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return renderPost23(typeface, c);
        }
        else {
            return renderPre23(typeface, c);
        }
    }

    /**
     * Can a typeface display the tile-like wavy line that some vocab subjects use.
     *
     * @param typeface the typeface to test
     * @return true if it can
     */
    public static boolean isTildeCapable(final Typeface typeface) {
        if (!TILDE_CAPABLE.containsKey(typeface)) {
            TILDE_CAPABLE.put(typeface, hasGlyph(typeface, "〜"));
        }
        return isTrue(TILDE_CAPABLE.get(typeface));
    }

    /**
     * Escape HTML in the text.
     *
     * @param text the text to escape
     * @return the text with HTML markup escaped
     */
    public static String escapeHtml(final String text) {
        String s = text;
        if (s.contains("&")) {
            s = AMP_PATTERN.matcher(s).replaceAll("&amp;");
        }
        if (s.contains("<")) {
            s = LT_PATTERN.matcher(s).replaceAll("&lt;");
        }
        if (s.contains(">")) {
            s = GT_PATTERN.matcher(s).replaceAll("&gt;");
        }
        return s;
    }

    /**
     * Format a timestamp for informal display in subject info.
     *
     * @param value the date to format
     * @return the formatted timestamp
     */
    public static String formatTimestamp(final @Nullable Date value) {
        if (value == null) {
            return "";
        }
        final java.text.DateFormat dateFormatter = DateFormat.getMediumDateFormat(WkApplication.getInstance());
        final java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(WkApplication.getInstance());
        return String.format("%s %s", dateFormatter.format(value), timeFormatter.format(value));
    }

    /**
     * Render a string as HTML for TextView instances, with limited tag support.
     *
     * @param source the HTML source test
     * @return the rendered spanned CharSequence
     */
    public static CharSequence renderHtml(final String source) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, null, new WaniKaniTagHandler());
    }

    /**
     * Render a string as HTML for TextView instances, with limited tag support.
     *
     * @param source the HTML source test
     * @param imageGetter the image resolver
     * @return the rendered spanned CharSequence
     */
    public static CharSequence renderHtml(final String source, final Html.ImageGetter imageGetter) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, new WaniKaniTagHandler());
    }

    /**
     * Format a number as a roman numeral.
     *
     * @param number the number
     * @return the formatted number
     */
    public static String getRomanNumeral(final int number) {
        int n = number;
        if (n < 1) {
            return Integer.toString(n);
        }
        final StringBuilder sb = new StringBuilder();
        while (n >= 1000) {
            sb.append("M");
            n -= 1000;
        }
        if (n >= 900) {
            sb.append("CM");
            n -= 900;
        }
        if (n >= 500) {
            sb.append("D");
            n -= 500;
        }
        if (n >= 400) {
            sb.append("CD");
            n -= 400;
        }
        while (n >= 100) {
            sb.append("C");
            n -= 100;
        }
        if (n >= 90) {
            sb.append("XC");
            n -= 90;
        }
        if (n >= 50) {
            sb.append("L");
            n -= 50;
        }
        if (n >= 40) {
            sb.append("XL");
            n -= 40;
        }
        while (n >= 10) {
            sb.append("X");
            n -= 10;
        }
        if (n >= 9) {
            sb.append("IX");
            n -= 9;
        }
        if (n >= 5) {
            sb.append("V");
            n -= 5;
        }
        if (n >= 4) {
            sb.append("IV");
            n -= 4;
        }
        while (n >= 1) {
            sb.append("I");
            n--;
        }
        return sb.toString();
    }
}
