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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.the_tinkering.wk.util.FontStorageUtil.flushCache;
import static com.the_tinkering.wk.util.FontStorageUtil.getNames;
import static com.the_tinkering.wk.util.FontStorageUtil.getTypefaceConfiguration;
import static com.the_tinkering.wk.util.FontStorageUtil.hasFontFile;
import static com.the_tinkering.wk.util.FontStorageUtil.importFontFile;
import static com.the_tinkering.wk.util.ObjectSupport.isEmpty;
import static com.the_tinkering.wk.util.ObjectSupport.runAsync;
import static com.the_tinkering.wk.util.ObjectSupport.safe;
import static com.the_tinkering.wk.util.ObjectSupport.safeNullable;
import static java.util.Objects.requireNonNull;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.the_tinkering.wk.R;
import com.the_tinkering.wk.WkApplication;
import com.the_tinkering.wk.model.TypefaceConfiguration;
import com.the_tinkering.wk.proxy.ViewProxy;
import com.the_tinkering.wk.util.FontStorageUtil;
import com.the_tinkering.wk.views.FontImportRowView;

import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * An activity for importing fonts for quiz questions.
 */
public final class FontImportActivity extends AbstractActivity {
    private final ViewProxy fontTable = new ViewProxy();
    private final ViewProxy importFontButton = new ViewProxy();

    private @Nullable ActivityResultLauncher<Intent> activityResultLauncher = null;

    /**
     * The constructor.
     */
    public FontImportActivity() {
        super(R.layout.activity_font_import, R.menu.generic_options_menu);
    }

    @Override
    protected void onCreateLocal(final @Nullable Bundle savedInstanceState) {
        fontTable.setDelegate(this, R.id.fontTable);
        importFontButton.setDelegate(this, R.id.importFontButton);

        importFontButton.setOnClickListener(v -> importFont());

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        final Uri uri = result.getData().getData();
                        final @Nullable String fileName = resolveFileName(uri);
                        if (fileName == null) {
                            return;
                        }
                        if (hasFontFile(fileName)) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Overwrite file?")
                                    .setMessage(String.format("A file named '%s' already exists. Do you want to overwrite it?", fileName))
                                    .setIcon(R.drawable.ic_baseline_warning_24px)
                                    .setNegativeButton("No", (dialog, which) -> {})
                                    .setPositiveButton("Yes", (dialog, which) -> safe(() -> importFile(uri, fileName))).create().show();
                        }
                        else {
                            importFile(uri, fileName);
                        }
                    }
                });
    }

    @Override
    protected void onResumeLocal() {
        updateFileList();
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

    private void updateFileList() {
        fontTable.removeAllViews();
        for (final String name: getNames()) {
            final FontImportRowView row = new FontImportRowView(this);
            row.setName(name);
            final TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams(0, 0);
            rowLayoutParams.setMargins(dp2px(2), dp2px(2), 0, 0);
            rowLayoutParams.width = WRAP_CONTENT;
            rowLayoutParams.height = WRAP_CONTENT;
            fontTable.addView(row, rowLayoutParams);
        }
    }

    private @Nullable String resolveFileName(final Uri uri) {
        return safeNullable(() -> {
            @Nullable String fileName = uri.getPath();
            try (final @Nullable Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    final int displayNameIndex = cursor.getColumnIndex("_display_name");
                    final @Nullable String displayName = displayNameIndex == -1 ? null : cursor.getString(displayNameIndex);
                    if (displayName != null) {
                        fileName = displayName;
                    }
                }
            }

            if (fileName != null) {
                final int p = fileName.lastIndexOf('/');
                if (p >= 0) {
                    fileName = fileName.substring(p+1);
                }
            }

            if (isEmpty(fileName)) {
                fileName = null;
            }

            return fileName;
        });
    }

    private void importFile(final Uri uri, final String fileName) {
        runAsync(this, () -> {
            try (final @Nullable InputStream is = WkApplication.getInstance().getContentResolver().openInputStream(uri)) {
                if (is != null) {
                    importFontFile(is, fileName);
                }
            }
            return null;
        }, result -> {
            flushCache(fileName);
            updateFileList();
            Toast.makeText(this, "File imported", Toast.LENGTH_LONG).show();
        });
    }

    @TargetApi(19)
    private void importFontPost19() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (activityResultLauncher != null) {
            activityResultLauncher.launch(intent);
        }
    }

    private void importFontPre19() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (activityResultLauncher != null) {
            activityResultLauncher.launch(Intent.createChooser(intent, "Select a TTF font file to import"));
        }
    }

    /**
     * Handler for the import button. Pop up the chooser.
     */
    private void importFont() {
        safe(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                importFontPost19();
            }
            else {
                importFontPre19();
            }
        });
    }

    /**
     * Handler for the show sample button. Load the font and pop up a dialog with a text sample.
     */
    public void showSample(final @Nullable FontImportRowView row) {
        safe(() -> {
            if (row != null && row.getName() != null) {
                try {
                    final TypefaceConfiguration typefaceConfiguration = requireNonNull(getTypefaceConfiguration(row.getName()));

                    final TextView textView = new TextView(this, null, R.attr.WK_TextView_Normal);
                    textView.setTextSize(36);
                    textView.setText("日本語の書体");
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(0, dp2px(16), 0, dp2px(16));
                    textView.setTypeface(typefaceConfiguration.getTypeface());

                    new AlertDialog.Builder(this)
                            .setTitle("Font sample")
                            .setView(textView)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {}).create().show();
                }
                catch (final Exception e) {
                    new AlertDialog.Builder(this)
                            .setTitle("Unable to load font")
                            .setMessage("There was an error attempting to load this font. You will not be able to use it in quizzes.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {}).create().show();
                }
            }
        });
    }

    /**
     * Handler for the delete button. Ask for confirmation and remove the file.
     */
    public void deleteFont(final @Nullable FontImportRowView row) {
        safe(() -> {
            if (row != null && row.getName() != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete file?")
                        .setMessage(String.format("Are you sure you want to delete '%s'", row.getName()))
                        .setIcon(R.drawable.ic_baseline_warning_24px)
                        .setNegativeButton("No", (dialog, which) -> {})
                        .setPositiveButton("Yes", (dialog, which) -> safe(() -> {
                            FontStorageUtil.deleteFont(row.getName());
                            flushCache(row.getName());
                            updateFileList();
                        })).create().show();
            }
        });
    }
}
