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

package com.the_tinkering.wk.activities;

import android.os.Bundle;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.DIGRAPH_HELP_DOCUMENT_1;
import static com.the_tinkering.wk.Constants.DIGRAPH_HELP_DOCUMENT_2;

/**
 * Simple activity that only shows a big TextView. Shows the app's 'about' information.
 */
public final class DigraphHelpActivity extends AbstractActivity {
    /**
     * The constructor.
     */
    public DigraphHelpActivity() {
        super(R.layout.activity_digraph_help, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        final ViewProxy document1 = new ViewProxy(this, R.id.document1);
        final ViewProxy document2 = new ViewProxy(this, R.id.document2);

        document1.setTextHtml(DIGRAPH_HELP_DOCUMENT_1);
        document1.setLinkMovementMethod();

        document2.setTextHtml(DIGRAPH_HELP_DOCUMENT_2);
        document2.setLinkMovementMethod();
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
}
