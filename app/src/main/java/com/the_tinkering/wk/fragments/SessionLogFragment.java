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

package com.the_tinkering.wk.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.adapter.sessionlog.SessionLogAdapter;
import com.the_tinkering.wk.db.model.Subject;
import com.the_tinkering.wk.model.Session;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.ThemeUtil;

import javax.annotation.Nullable;

/**
 * Fragment to show a simple search result.
 */
public final class SessionLogFragment extends AbstractFragment {
    private final Session session = Session.getInstance();
    private final SessionLogAdapter adapter = session.getAdapter();

    private final ViewProxy resultView = new ViewProxy();
    private final ViewProxy tutorialText = new ViewProxy();
    private final ViewProxy tutorialDismiss = new ViewProxy();

    /**
     * The constructor.
     */
    public SessionLogFragment() {
        super(R.layout.fragment_session_log);
    }

    /**
     * Create a new instance with arguments set.
     *
     * @return the fragment
     */
    public static SessionLogFragment newInstance() {
        return new SessionLogFragment();
    }

    @Override
    protected void onCreateLocal() {
        //
    }

    @Override
    protected void onResumeLocal() {
        if (GlobalSettings.Tutorials.getSessionLogDismissed() || GlobalSettings.AdvancedOther.getFullSessionLog()) {
            tutorialText.setParentVisibility(false);
        }
        else {
            tutorialText.setText("By default, this session log shows only limited information to avoid giving away"
                    + " answers or showing inappropriate hints, and some items are deliberately not clickable."
                    + " If you want to see the full session log, you can"
                    + " enable it in Settings -> Advanced -> Other.");
            tutorialText.setParentVisibility(true);
            tutorialDismiss.setOnClickListener(v -> {
                GlobalSettings.Tutorials.setSessionLogDismissed(true);
                tutorialText.setParentVisibility(false);
            });
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreatedLocal(final View view, final @Nullable Bundle savedInstanceState) {
        resultView.setDelegate(view, R.id.resultView);
        tutorialText.setDelegate(view, R.id.tutorialText);
        tutorialDismiss.setDelegate(view, R.id.tutorialDismiss);

        final DisplayMetrics metrics = view.getContext().getResources().getDisplayMetrics();
        int spans = (int) ((metrics.widthPixels / metrics.density + 10) / 90);
        if (spans < 1) {
            spans = 1;
        }
        final int numSpans = spans;
        final GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(), spans);
        final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                return adapter.getItemSpanSize(position, numSpans);
            }
        };
        spanSizeLookup.setSpanGroupIndexCacheEnabled(true);
        spanSizeLookup.setSpanIndexCacheEnabled(true);
        layoutManager.setSpanSizeLookup(spanSizeLookup);
        resultView.setLayoutManager(layoutManager);
        resultView.setAdapter(adapter);
        resultView.setHasFixedSize(false);

        adapter.setActment(this);
        adapter.initialize();
    }

    @Override
    public String getToolbarTitle() {
        return "Session log";
    }

    @Override
    public int getToolbarBackgroundColor() {
        return ThemeUtil.getColor(R.attr.toolbarColorBackground);
    }

    @Override
    public void enableInteraction() {
        interactionEnabled = true;
    }

    @Override
    public void disableInteraction() {
        interactionEnabled = false;
    }

    @Override
    public @Nullable Subject getCurrentSubject() {
        return null;
    }

    @Override
    public void showOrHideSoftInput() {
        hideSoftInput();
    }

    @Override
    public void updateViews() {
        //
    }
}
