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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A custom view that shows a single bar in the level progress chart on the dashboard.
 */
public final class SynonymRowView extends LinearLayout {
    private int index = 0;

    private final ViewProxy textView = new ViewProxy();
    private final ViewProxy arrowDown = new ViewProxy();
    private final ViewProxy arrowUp = new ViewProxy();

    /**
     * The constructor.
     *
     * @param context Android context
     */
    public SynonymRowView(final Context context) {
        super(context);
        safe(() -> init(null));
    }

    /**
     * The constructor.
     *
     * @param context Android context
     * @param attrs attribute set
     */
    public SynonymRowView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        safe(() -> init(attrs));
    }

    private @Nullable SynonymRowView findRowByIndex(final int otherIndex) {
        final @Nullable ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return null;
        }
        final ViewGroup group = (ViewGroup) parent;
        for (int i=0; i<group.getChildCount(); i++) {
            final @Nullable View view = group.getChildAt(i);
            if (view instanceof SynonymRowView && ((SynonymRowView) view).index == otherIndex) {
                return (SynonymRowView) view;
            }
        }
        return null;
    }

    /**
     * Initialize the view.
     */
    private void init(final @Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.synonym, this);
        setOrientation(HORIZONTAL);

        textView.setDelegate(this, R.id.text);
        arrowDown.setDelegate(this, R.id.arrowDown);
        arrowUp.setDelegate(this, R.id.arrowUp);

        if (attrs != null) {
            final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SynonymRowView, 0, 0);
            try {
                index = a.getInt(R.styleable.SynonymRowView_index, 0);
                if (index == 0) {
                    arrowUp.setVisibility(INVISIBLE);
                }
                if (index == 9) {
                    arrowDown.setVisibility(INVISIBLE);
                }
            } finally {
                a.recycle();
            }
        }

        arrowDown.setOnClickListener(v -> {
            if (index < 9) {
                final @Nullable SynonymRowView nextRow = findRowByIndex(index + 1);
                if (nextRow != null) {
                    final String tmp = getText();
                    setText(nextRow.getText());
                    nextRow.setText(tmp);
                }
            }
        });

        arrowUp.setOnClickListener(v -> {
            if (index > 0) {
                final @Nullable SynonymRowView prevRow = findRowByIndex(index - 1);
                if (prevRow != null) {
                    final String tmp = getText();
                    setText(prevRow.getText());
                    prevRow.setText(tmp);
                }
            }
        });
    }

    /**
     * The text for this instance. Delegated to the contained EditText.
     *
     * @param text the synonym text
     */
    public void setText(final @Nullable CharSequence text) {
        textView.setText(text);
    }

    /**
     * The text for this instance. Delegated to the contained EditText.
     *
     * @return the synonym text
     */
    public String getText() {
        return textView.getText();
    }
}
