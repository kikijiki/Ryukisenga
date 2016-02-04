/*
 * Copyright 2014 Matteo Bernacchia <kikijikispaccaspecchi@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kikijiki.ryukisenga.styles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.styles.data.Gradient;
import com.kikijiki.ryukisenga.styles.data.PaintData;
import com.kikijiki.ryukisenga.styles.data.StyleData;

public class StyleEditorActivity extends Activity {
    public static final String STYLE_FILENAME_EXTRA = "style_filename";
    public static final String TILE_EXTRA = "tile";
    public static final int TILE_PICKER_REQUEST_CODE = 1;
    public static final String GRADIENT_EXTRA = "gradient";
    public static final int GRADIENT_EDITOR_REQUEST_CODE = 2;
    public static final String CALLER_ID_EXTRA = "caller";
    public static final int CALLER_ID_LINE = 0;
    public static final int CALLER_ID_TRAIL = 1;
    public static final int CALLER_ID_BACKGROUND = 2;
    private static final String STYLE_STATE_KEY = "style";
    public boolean created = false;
    private StyleEditorHelper _style = null;
    private Bundle _state;
    private Bundle _extras;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.style_editor);

        _state = state;
        _extras = getIntent().getExtras();

        if (_state != null) {
            StyleEditorHelper style = (StyleEditorHelper) _state.get(STYLE_STATE_KEY);

            if (style != null)
                _style = style;
        }
    }

    private void newStyle(String name) {
        if (StyleManager.isStyleNameAvailable(this, name)) {
            _style = new StyleEditorHelper();
            _style.initialize(this, StyleManager.LoadStyleData(this, null));
            _style.setName(name);
            _style.save();
        } else {
            nameNotAvailable();
        }
    }

    private void nameNotAvailable() {
        new AlertDialog.Builder(StyleEditorActivity.this)
                .setTitle(R.string.style_editor_name_dialog_title_na)
                .setMessage(R.string.style_editor_name_dialog_message_na)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        promptName();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void loadStyle(String filename) {
        if (filename == null) {
            promptName();
        } else {
            _style = new StyleEditorHelper();
            _style.initialize(this, StyleManager.LoadStyleDataFromFile(this, filename));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && !created) {
            startup();

            created = true;
        }
    }

    private void promptName() {
        final EditText input = new EditText(this);

        new AlertDialog.Builder(StyleEditorActivity.this)
                .setTitle(R.string.style_editor_name_dialog_title)
                .setMessage(R.string.style_editor_name_dialog_message)
                .setView(input)
                .setIcon(android.R.drawable.ic_input_add)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || value.length() == 0) {
                            finish();
                            return;
                        } else {
                            newStyle(value);
                        }
                    }
                })
                .setNegativeButton(R.string.style_editor_name_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STYLE_STATE_KEY, _style);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        created = false;

        if (_style != null) {
            _style.onDestroy();
            _style = null;
        }
    }

    private void startup() {
        if (_style != null) {
            _style.rebind(this);
        } else {
            if (_state != null) {
                StyleEditorHelper style = (StyleEditorHelper) _state.get(STYLE_STATE_KEY);

                if (style != null) {
                    _style = style;
                    _style.rebind(this);
                } else {
                    String style_filename = null;

                    if (_extras != null) {
                        style_filename = _extras.getString(STYLE_FILENAME_EXTRA);
                    }

                    loadStyle(style_filename);
                }
            } else {
                String style_filename = null;

                if (_extras != null) {
                    style_filename = _extras.getString(STYLE_FILENAME_EXTRA);
                }

                loadStyle(style_filename);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

        Bundle extras = data.getExtras();

        if (extras == null)
            return;

        switch (requestCode) {
            case TILE_PICKER_REQUEST_CODE: {
                String tile = extras.getString(TILE_EXTRA);
                if (tile == null || tile.length() == 0)
                    return;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(PaintData.getTilePath(tile), options);

                if (options.outHeight > StyleData.MAX_TILE_SIZE || options.outWidth > StyleData.MAX_TILE_SIZE) {
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.style_editor_size_too_big_title)
                            .setMessage(R.string.style_editor_size_too_big_message)
                            .setPositiveButton(R.string.dialog_ok, null)
                            .show();
                } else if (options.outHeight < 0 || options.outWidth < 0) {
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.style_editor_invalid_tile_title)
                            .setMessage(R.string.style_editor_invalid_tile_message)
                            .setPositiveButton(R.string.dialog_ok, null)
                            .show();
                } else {
                    _style.setBackgroundTile(tile);
                }
            }
            break;
            case GRADIENT_EDITOR_REQUEST_CODE: {
                Gradient g = (Gradient) extras.getSerializable(GRADIENT_EXTRA);

                if (g == null)
                    return;

                switch (extras.getInt(CALLER_ID_EXTRA)) {
                    case CALLER_ID_LINE:
                        _style.setLineGradient(g);
                        break;
                    case CALLER_ID_TRAIL:
                        _style.setTrailGradient(g);
                        break;
                    case CALLER_ID_BACKGROUND:
                        _style.setBackgroundGradient(g);
                        break;
                }
            }
            break;
        }
    }
}
