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

import android.graphics.Paint;
import android.os.Build;
import android.os.LocaleList;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * A utility class for programmatically interacting with UI elements.
 */
public final class ViewUtil {
    /**
     * Private constructor.
     */
    private ViewUtil() {
        //
    }

    /**
     * Find the nearest enclosing view of a specific type.
     *
     * @param view the view or view's parent to look at
     * @param clas the class of the view being looked for
     * @param <T> the type of the resulting view
     * @return the view or null if not found
     */
    public static @Nullable <T extends View> T getNearestEnclosingViewOfType(final Object view, final Class<T> clas) {
        if (clas.isInstance(view)) {
            return clas.cast(view);
        }
        if (view instanceof View) {
            final Object parent = ((View) view).getParent();
            return getNearestEnclosingViewOfType(parent, clas);
        }
        return null;
    }

    /**
     * Set the locale for a textview to Japanese if the Android version is Jelly Bean or newer.
     *
     * @param view the view to set the locale on
     */
    public static void setJapaneseLocale(final TextView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.setTextLocales(new LocaleList(Locale.JAPAN, Locale.ROOT));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setTextLocale(Locale.JAPAN);
        }
    }

    /**
     * Set the locale for a paint to Japanese if the Android version is Jelly Bean or newer.
     *
     * @param paint the paint to set the locale on
     */
    public static void setJapaneseLocale(final Paint paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            paint.setTextLocales(new LocaleList(Locale.JAPAN, Locale.ROOT));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paint.setTextLocale(Locale.JAPAN);
        }
    }

    /**
     * Set the locale for a textview to Root if the Android version is Jelly Bean or newer.
     *
     * @param view the view to set the locale on
     */
    public static void setRootLocale(final TextView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setTextLocale(Locale.ROOT);
        }
    }
}
