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

package com.the_tinkering.wk.components;

import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * Subclass of ArrowKeyMovementMethod that supports clicking on links in text.
 * This is used over LinkMovementMethod, since that class and selectable text don't
 * play nice together.
 */
public final class CustomMovementMethod extends ArrowKeyMovementMethod {
    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event){
        return safe(() -> super.onTouchEvent(widget, buffer, event), () -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();

                final Layout layout = widget.getLayout();
                final int line = layout.getLineForVertical(y);
                final int off = layout.getOffsetForHorizontal(line, x);

                final URLSpan[] links = buffer.getSpans(off, off, URLSpan.class);
                if (links.length != 0) {
                    widget.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(links[0].getURL())));
                }
                return true;
            }
            return false;
        });
    }
}
