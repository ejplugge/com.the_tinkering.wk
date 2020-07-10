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

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.the_tinkering.wk.GlobalSettings;
import com.the_tinkering.wk.R;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.Logger;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_1;
import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_2;
import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_3;
import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_4;
import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_5;
import static com.the_tinkering.wk.Constants.KEYBOARD_HELP_DOCUMENT_INTRO;

/**
 * Simple activity that shows some keyboard-related help.
 */
public final class KeyboardHelpActivity extends AbstractActivity {
    private static final Logger LOGGER = Logger.get(KeyboardHelpActivity.class);

    /**
     * The constructor.
     */
    public KeyboardHelpActivity() {
        super(R.layout.activity_keyboard_help, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        final ViewProxy documentIntro = new ViewProxy(this, R.id.document_intro);
        documentIntro.setTextHtml(KEYBOARD_HELP_DOCUMENT_INTRO);
        final ViewProxy document1 = new ViewProxy(this, R.id.document_1);
        document1.setTextHtml(KEYBOARD_HELP_DOCUMENT_1);
        final ViewProxy document2 = new ViewProxy(this, R.id.document_2);
        document2.setTextHtml(KEYBOARD_HELP_DOCUMENT_2);
        final ViewProxy document3 = new ViewProxy(this, R.id.document_3);
        document3.setTextHtml(KEYBOARD_HELP_DOCUMENT_3);
        final ViewProxy document4 = new ViewProxy(this, R.id.document_4);
        document4.setTextHtml(KEYBOARD_HELP_DOCUMENT_4);
        final ViewProxy document5 = new ViewProxy(this, R.id.document_5);
        document5.setTextHtml(KEYBOARD_HELP_DOCUMENT_5);
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

    /**
     * Handler for the 'keyboard settings' buttons.
     *
     * @param view the button
     */
    public void goToKeyboardSettings(@SuppressWarnings("unused") final View view) {
        try {
            goToPreferencesActivity("keyboard_settings");
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the first 'do it' button.
     *
     * @param view the button
     */
    public void doIt1(@SuppressWarnings("unused") final View view) {
        try {
            GlobalSettings.Keyboard.setForceVisiblePasswordMeaning(true);
            GlobalSettings.Keyboard.setForceVisiblePasswordReading(true);
            GlobalSettings.Keyboard.setEnableAutoCorrectMeaning(false);
            GlobalSettings.Keyboard.setEnableAutoCorrectReading(false);
            GlobalSettings.Keyboard.setEnableNoLearning(false);
            new AlertDialog.Builder(this)
                    .setTitle("Keyboard settings updated")
                    .setMessage("Your settings have been updated.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            //
                        }
                    }).create().show();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the second 'do it' button.
     *
     * @param view the button
     */
    public void doIt2(@SuppressWarnings("unused") final View view) {
        try {
            GlobalSettings.Keyboard.setForceVisiblePasswordMeaning(true);
            GlobalSettings.Keyboard.setForceVisiblePasswordReading(true);
            GlobalSettings.Keyboard.setEnableAutoCorrectMeaning(false);
            GlobalSettings.Keyboard.setEnableAutoCorrectReading(false);
            GlobalSettings.Keyboard.setEnableNoLearning(true);
            new AlertDialog.Builder(this)
                    .setTitle("Keyboard settings updated")
                    .setMessage("Your settings have been updated.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            //
                        }
                    }).create().show();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the third 'do it' button.
     *
     * @param view the button
     */
    public void doIt3(@SuppressWarnings("unused") final View view) {
        try {
            GlobalSettings.Keyboard.setForceVisiblePasswordMeaning(false);
            GlobalSettings.Keyboard.setForceVisiblePasswordReading(false);
            new AlertDialog.Builder(this)
                    .setTitle("Keyboard settings updated")
                    .setMessage("Your settings have been updated.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            //
                        }
                    }).create().show();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Handler for the third 'do it' button.
     *
     * @param view the button
     */
    public void doIt4(@SuppressWarnings("unused") final View view) {
        try {
            GlobalSettings.Keyboard.setForceAsciiMeaning(false);
            new AlertDialog.Builder(this)
                    .setTitle("Keyboard settings updated")
                    .setMessage("Your settings have been updated.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            //
                        }
                    }).create().show();
        } catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }
}
