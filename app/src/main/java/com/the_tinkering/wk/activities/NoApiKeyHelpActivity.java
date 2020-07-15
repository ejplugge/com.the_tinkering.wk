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

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.NO_API_KEY_HELP_DOCUMENT;
import static com.the_tinkering.wk.util.ObjectSupport.safe;

/**
 * A simple activity only used as a helper to get the user to supply an API key.
 *
 * <p>
 *     As long as no valid API key is present, other activities force this one to
 *     be launched.
 * </p>
 */
public final class NoApiKeyHelpActivity extends AbstractActivity {
    private final ViewProxy saveButton = new ViewProxy();
    private final ViewProxy apiKey = new ViewProxy();

    /**
     * The constructor.
     */
    public NoApiKeyHelpActivity() {
        super(R.layout.activity_no_api_key_help, R.menu.no_api_key_help_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        saveButton.setDelegate(this, R.id.saveButton);
        apiKey.setDelegate(this, R.id.apiKey);

        final ViewProxy document = new ViewProxy(this, R.id.document);
        document.setTextHtml(NO_API_KEY_HELP_DOCUMENT);
        document.setLinkMovementMethod();

        apiKey.setOnEditorActionListener((v, actionId, event) -> safe(false, () -> {
            if (event == null && actionId == EditorInfo.IME_ACTION_DONE) {
                saveApiKey(v);
                return true;
            }
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                saveApiKey(v);
                return true;
            }
            return false;
        }));
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
        saveButton.enableInteraction();
    }

    @Override
    protected void disableInteractionLocal() {
        saveButton.disableInteraction();
    }

    /**
     * Handler for the save button. Save the API key that was entered.
     *
     * @param view the button
     */
    public void saveApiKey(@SuppressWarnings("unused") final View view) {
        safe(() -> {
            if (!interactionEnabled) {
                return;
            }
            disableInteraction();
            GlobalSettings.Api.setApiKey(apiKey.getText());
            goToMainActivity();
        });
    }
}
