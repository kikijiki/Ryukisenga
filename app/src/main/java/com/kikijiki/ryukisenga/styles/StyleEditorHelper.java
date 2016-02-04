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
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kikijiki.ryukisenga.ui.ColorPickerDialog;
import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.content.XMLFormat.StyleFillStyle;
import com.kikijiki.ryukisenga.drawing.Sequence;
import com.kikijiki.ryukisenga.drawing.Sequence.Settings;
import com.kikijiki.ryukisenga.drawing.vec;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.styles.data.Gradient;
import com.kikijiki.ryukisenga.styles.data.PaintData;
import com.kikijiki.ryukisenga.styles.data.StyleData;

import java.io.Serializable;

public class StyleEditorHelper implements Serializable {
    public static final String[] SPINNER_WIDTH_CHOICES;
    public static final float[] SPINNER_WIDTH_VALUES;
    public static final String[] SPINNER_SCALE_CHOICES;
    public static final float[] SPINNER_SCALE_VALUES;
    public static final int SPINNER_SCALE_NO_SCALING;
    private static final long serialVersionUID = -5949699548781985724L;
    private static final double FPS = 60;
    private static final long FRAME_LENGTH_MS = (long) (1000.0D / FPS);
    private static final float FRAME_LENGTH_S = (float) (1.0D / FPS);
    public StyleData data = null;
    public transient StylePreview style;
    public transient PaintPreview line;
    public transient PaintPreview trail;
    public transient PaintPreview background;
    private boolean _ready = false;
    static {
        SPINNER_WIDTH_CHOICES = new String[]
                {"1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px"};
        SPINNER_WIDTH_VALUES = new float[]
                {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        SPINNER_SCALE_CHOICES = new String[]
                {"5%", "10%", "25%", "33%", "50%", "75%", "100%", "125%", "150%", "200%", "300%"};
        SPINNER_SCALE_VALUES = new float[]
                {.05f, .1f, .25f, .33f, .5f, .75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f};

        SPINNER_SCALE_NO_SCALING = 6;
    }
    private transient Activity _host;

    private void updatePaints() {
        line.setPreviewBorder(data.line);
        line.drawPaintPreview(data.computeLinePaintPreview(_host, line._size.x, line._size.y));

        trail.setPreviewBorder(data.trail);
        trail.drawPaintPreview(data.computeTrailPaintPreview(_host, trail._size.x, trail._size.y));

        background.setPreviewBorder(data.background);
        background.drawPaintPreview(data.computeBackgroundPaintPreview(_host, background._size.x, background._size.y));

        style.onDataChanged();
    }

    private void bind(Activity host) {
        _host = host;

        style = new StylePreview(R.id.style_editor_style_preview);
        line = new PaintPreview(R.id.style_editor_line_preview);
        trail = new PaintPreview(R.id.style_editor_trail_preview);
        background = new PaintPreview(R.id.style_editor_background_preview);

        style.prepare();
        line.prepare();
        trail.prepare();
        background.prepare();

        bindSpinners();
    }

    private void bindSpinners() {
        final Spinner line_width_spinner = (Spinner) _host.findViewById(R.id.style_editor_line_width);
        ArrayAdapter<String> line_width_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item, SPINNER_WIDTH_CHOICES);

        line_width_spinner.setAdapter(line_width_adapter);

        final Spinner trail_width_spinner = (Spinner) _host.findViewById(R.id.style_editor_trail_width);
        ArrayAdapter<String> trail_width_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item, SPINNER_WIDTH_CHOICES);
        trail_width_spinner.setAdapter(trail_width_adapter);

        final Spinner line_fill = (Spinner) _host.findViewById(R.id.style_editor_line_fill);
        ArrayAdapter<String> line_fill_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item,
                new String[]
                        {
                                _host.getString(R.string.style_editor_fill_type_solid),
                                _host.getString(R.string.style_editor_fill_type_gradient),
                        });
        line_fill.setAdapter(line_fill_adapter);

        final Spinner trail_fill = (Spinner) _host.findViewById(R.id.style_editor_trail_fill);
        ArrayAdapter<String> trail_fill_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item,
                new String[]
                        {
                                _host.getString(R.string.style_editor_fill_type_solid),
                                _host.getString(R.string.style_editor_fill_type_gradient),
                        });
        trail_fill.setAdapter(trail_fill_adapter);

        final Spinner background_scale = (Spinner) _host.findViewById(R.id.style_editor_background_scale);
        ArrayAdapter<String> background_scale_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item, SPINNER_SCALE_CHOICES);
        background_scale.setAdapter(background_scale_adapter);

        final Spinner background_fill = (Spinner) _host.findViewById(R.id.style_editor_background_fill);
        ArrayAdapter<String> background_fill_adapter = new ArrayAdapter<String>(_host, android.R.layout.simple_spinner_dropdown_item,
                new String[]
                        {
                                _host.getString(R.string.style_editor_fill_type_solid),
                                _host.getString(R.string.style_editor_fill_type_gradient),
                                _host.getString(R.string.style_editor_fill_type_tile),
                        });
        background_fill.setAdapter(background_fill_adapter);
    }

    private void bindEvents() {
        final ImageView line_preview = (ImageView) _host.findViewById(R.id.style_editor_line_preview);
        line_preview.post(new Runnable() {
            @Override
            public void run() {
                line_preview.setOnClickListener(new OnClickListener() {
                    @SuppressWarnings("incomplete-switch")
                    @Override
                    public void onClick(View v) {
                        switch (data.line.style) {
                            case solid: {
                                ColorPickerDialog picker = new ColorPickerDialog(_host, data.line.color, new ColorPickerDialog.ColorPickerListener() {
                                    @Override
                                    public void onCancel(ColorPickerDialog dialog) {
                                    }

                                    @Override
                                    public void onOk(ColorPickerDialog dialog, int color) {
                                        data.line.color = color;
                                        commitChanges();
                                    }
                                });
                                picker.show();
                            }
                            break;

                            case gradient: {
                                Intent i = new Intent(_host, GradientEditorActivity.class);
                                i.putExtra(StyleEditorActivity.GRADIENT_EXTRA, data.line.gradient);
                                i.putExtra(StyleEditorActivity.CALLER_ID_EXTRA, StyleEditorActivity.CALLER_ID_LINE);
                                _host.startActivityForResult(i, StyleEditorActivity.GRADIENT_EDITOR_REQUEST_CODE);
                            }
                            break;
                        }
                    }
                });
            }
        });

        final ImageView trail_preview = (ImageView) _host.findViewById(R.id.style_editor_trail_preview);
        trail_preview.post(new Runnable() {
            @Override
            public void run() {
                trail_preview.setOnClickListener(new OnClickListener() {
                    @SuppressWarnings("incomplete-switch")
                    @Override
                    public void onClick(View v) {
                        switch (data.trail.style) {
                            case solid: {
                                ColorPickerDialog picker = new ColorPickerDialog(_host, data.trail.color, new ColorPickerDialog.ColorPickerListener() {
                                    @Override
                                    public void onCancel(ColorPickerDialog dialog) {
                                    }

                                    @Override
                                    public void onOk(ColorPickerDialog dialog, int color) {
                                        data.trail.color = color;
                                        commitChanges();
                                    }
                                });
                                picker.show();
                            }
                            break;

                            case gradient: {
                                Intent i = new Intent(_host, GradientEditorActivity.class);
                                i.putExtra(StyleEditorActivity.GRADIENT_EXTRA, data.trail.gradient);
                                i.putExtra(StyleEditorActivity.CALLER_ID_EXTRA, StyleEditorActivity.CALLER_ID_TRAIL);
                                _host.startActivityForResult(i, StyleEditorActivity.GRADIENT_EDITOR_REQUEST_CODE);
                            }
                            break;
                        }
                    }
                });
            }
        });

        final ImageView background_preview = (ImageView) _host.findViewById(R.id.style_editor_background_preview);
        background_preview.post(new Runnable() {
            @Override
            public void run() {
                background_preview.setOnClickListener(new OnClickListener() {
                    @SuppressWarnings("incomplete-switch")
                    @Override
                    public void onClick(View v) {
                        switch (data.background.style) {
                            case solid: {
                                ColorPickerDialog picker = new ColorPickerDialog(_host, data.background.color, new ColorPickerDialog.ColorPickerListener() {
                                    @Override
                                    public void onCancel(ColorPickerDialog dialog) {
                                    }

                                    @Override
                                    public void onOk(ColorPickerDialog dialog, int color) {
                                        data.background.color = color;
                                        background.setPreviewBorderColorInverseOf(color);
                                        commitChanges();
                                    }
                                });
                                picker.show();
                            }
                            break;

                            case gradient: {
                                Intent i = new Intent(_host, GradientEditorActivity.class);
                                i.putExtra(StyleEditorActivity.GRADIENT_EXTRA, data.background.gradient);
                                i.putExtra(StyleEditorActivity.CALLER_ID_EXTRA, StyleEditorActivity.CALLER_ID_BACKGROUND);
                                _host.startActivityForResult(i, StyleEditorActivity.GRADIENT_EDITOR_REQUEST_CODE);
                            }
                            break;

                            case tile: {
                                Intent i = new Intent(_host, TilePickerActivity.class);
                                _host.startActivityForResult(i, StyleEditorActivity.TILE_PICKER_REQUEST_CODE);
                            }
                            break;
                        }
                    }
                });
            }
        });

        final Spinner line_width_spinner = (Spinner) _host.findViewById(R.id.style_editor_line_width);
        line_width_spinner.post(new Runnable() {
            public void run() {
                line_width_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setLineWidth(SPINNER_WIDTH_VALUES[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });

        final Spinner trail_width_spinner = (Spinner) _host.findViewById(R.id.style_editor_trail_width);
        trail_width_spinner.post(new Runnable() {
            public void run() {
                trail_width_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setTrailWidth(SPINNER_WIDTH_VALUES[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });

        final CheckBox trail_check = (CheckBox) _host.findViewById(R.id.style_editor_trail_equals_line);
        trail_check.post(new Runnable() {
            @Override
            public void run() {
                trail_check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                        setTrailEqualsLine(checked);
                    }
                });
            }
        });

        final Spinner line_fill = (Spinner) _host.findViewById(R.id.style_editor_line_fill);
        line_fill.post(new Runnable() {
            @Override
            public void run() {
                line_fill.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setLineFillStyle(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });


        final Spinner trail_fill = (Spinner) _host.findViewById(R.id.style_editor_trail_fill);
        trail_fill.post(new Runnable() {
            @Override
            public void run() {
                trail_fill.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setTrailFillStyle(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });

        final Spinner background_fill = (Spinner) _host.findViewById(R.id.style_editor_background_fill);
        background_fill.post(new Runnable() {
            @Override
            public void run() {
                background_fill.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setBackgroundFillStyle(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });

        //final TextView scale_label = (TextView)_host.findViewById(R.id.style_editor_background_scale_label);
        final Spinner background_scale = (Spinner) _host.findViewById(R.id.style_editor_background_scale);
        background_scale.post(new Runnable() {
            @Override
            public void run() {
                enableScaling(data.background.style == StyleFillStyle.tile);

                background_scale.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setBackgroundScale(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });
    }

    public void save() {
        StyleManager.SaveStyleData(_host, data);
    }

    public void initialize(Activity host, StyleData data) {
        this.data = data;

        rebind(host);
    }

    public void setTrailEqualsLine(boolean checked) {
        if (checked) {
            data.trail.style = StyleFillStyle.asLine;
            //data.trail = data.line;
        } else {
            data.trail = data.line.clone();
        }

        save();
        updateInterface();
    }

    private void enableTrailEditing(boolean enable) {
        LinearLayout v;

        v = (LinearLayout) _host.findViewById(R.id.style_editor_trail_color_group);
        v.setEnabled(enable);

        for (int i = 0; i < v.getChildCount(); i++) {
            View item = v.getChildAt(i);
            enableView(item, enable);
        }

        v = (LinearLayout) _host.findViewById(R.id.style_editor_trail_width_group);
        v.setEnabled(enable);

        for (int i = 0; i < v.getChildCount(); i++) {
            View item = v.getChildAt(i);
            enableView(item, enable);
        }

        v = (LinearLayout) _host.findViewById(R.id.style_editor_trail_fill_group);
        v.setEnabled(enable);

        for (int i = 0; i < v.getChildCount(); i++) {
            View item = v.getChildAt(i);
            enableView(item, enable);
        }

        View preview = _host.findViewById(R.id.style_editor_trail_preview);
        enableView(preview, enable);
    }

    private void enableView(View v, boolean enable) {
        if (v.isEnabled() == enable)
            return;

        float from = enable ? .5f : 1.0f;
        float to = enable ? 1.0f : .5f;

        AlphaAnimation a = new AlphaAnimation(from, to);

        a.setDuration(500);
        a.setFillAfter(true);

        v.setEnabled(enable);
        v.startAnimation(a);
    }

    public void updateInterface() {
        TextView title = (TextView) _host.findViewById(R.id.style_editor_style_name);
        title.setText(data.info.name);

        CheckBox trail_check = (CheckBox) _host.findViewById(R.id.style_editor_trail_equals_line);
        trail_check.setChecked(data.trail.style == StyleFillStyle.asLine);
        enableTrailEditing(data.trail.style != StyleFillStyle.asLine);

        updateWidthSpinner(R.id.style_editor_line_width, data.line.width);
        updateWidthSpinner(R.id.style_editor_trail_width, data.trail.width);

        updateFillStyleSpinner(R.id.style_editor_line_fill, data.line.style);
        updateFillStyleSpinner(R.id.style_editor_trail_fill, data.trail.style);
        updateFillStyleSpinner(R.id.style_editor_background_fill, data.background.style);

        updateBackgroundScaleSpinner(R.id.style_editor_background_scale, data.background.scale);

        updatePaints();
    }

    private void updateBackgroundScaleSpinner(int id, Float scale) {
        Spinner s = (Spinner) _host.findViewById(id);

        if (scale == null) {
            s.setSelection(SPINNER_SCALE_NO_SCALING);
            return;
        }

        int option = 0;
        float error = Math.abs(SPINNER_SCALE_VALUES[0] - scale);

        for (int i = 1; i < SPINNER_SCALE_VALUES.length; i++) {
            float diff = Math.abs(SPINNER_SCALE_VALUES[i] - scale);

            if (diff < error) {
                error = diff;
                option = i;
            }
        }

        s.setSelection(option);
        enableView(s, data.background.style == StyleFillStyle.tile);
    }

    @SuppressWarnings("incomplete-switch")
    private void updateFillStyleSpinner(int id, StyleFillStyle fillStyle) {
        Spinner s = (Spinner) _host.findViewById(id);

        switch (fillStyle) {
            case solid:
                s.setSelection(0);
                break;
            case gradient:
                s.setSelection(1);
                break;
            case tile:
                s.setSelection(2);
                break;
        }
    }

    private void updateWidthSpinner(int id, float value) {
        Spinner s = (Spinner) _host.findViewById(id);

        int option = 0;
        float error = Math.abs(SPINNER_WIDTH_VALUES[0] - value);

        for (int i = 1; i < SPINNER_WIDTH_VALUES.length; i++) {
            float diff = Math.abs(SPINNER_WIDTH_VALUES[i] - value);
            if (diff < error) {
                error = diff;
                option = i;
            }
        }

        s.setSelection(option);
    }

    public void onDestroy() {
        style.onDestroy();
    }

    public void setName(String name) {
        data.info.name = name;
        TextView title = (TextView) _host.findViewById(R.id.style_editor_style_name);
        title.setText(data.info.name);
    }

    public void setLineWidth(float width) {
        data.line.width = width;

        commitChanges();
    }

    public void setTrailWidth(float width) {
        data.trail.width = width;

        commitChanges();
    }

    public void setLineFillStyle(int position) {
        switch (position) {
            case 0:
                data.line.style = StyleFillStyle.solid;
                break;
            case 1:
                data.line.style = StyleFillStyle.gradient;
                if (data.line.gradient.stop.isEmpty())
                    data.line.gradient.loadDefaultGradient();
                break;
        }

        commitChanges();
    }

    public void setTrailFillStyle(int position) {
        if (data.trail.style == StyleFillStyle.asLine)
            return;

        switch (position) {
            case 0:
                data.trail.style = StyleFillStyle.solid;
                break;
            case 1:
                data.trail.style = StyleFillStyle.gradient;
                if (data.trail.gradient.stop.isEmpty())
                    data.trail.gradient.loadDefaultGradient();
                break;
        }

        commitChanges();
    }

    public void setBackgroundScale(int position) {
        float scale = SPINNER_SCALE_VALUES[position];

        if (position == SPINNER_SCALE_NO_SCALING) {
            data.background.scale = null;
        } else {
            data.background.scale = scale;
        }

        commitChanges();
    }

    public void setBackgroundFillStyle(int position) {
        switch (position) {
            case 0:
                data.background.style = StyleFillStyle.solid;
                enableScaling(false);
                break;
            case 1:
                data.background.style = StyleFillStyle.gradient;
                if (data.background.gradient.stop.isEmpty())
                    data.background.gradient.loadDefaultGradient();
                enableScaling(false);
                break;
            case 2:
                data.background.style = StyleFillStyle.tile;
                enableScaling(true);
                break;
        }

        commitChanges();
    }

    private void enableScaling(boolean enable) {
        Spinner s = (Spinner) _host.findViewById(R.id.style_editor_background_scale);
        final TextView l = (TextView) _host.findViewById(R.id.style_editor_background_scale_label);

        enableView(s, enable);
        enableView(l, enable);
    }

    private void commitChanges() {
        if (_ready) {
            save();
            updateInterface();
        }
    }

    public void rebind(Activity host) {
        _host = host;

        _ready = false;
        bind(_host);
        updateInterface();
        bindEvents();
        _ready = true;
    }

    public boolean isReady() {
        return _ready;
    }

    public void setBackgroundTile(String tile) {
        data.background.tile = tile;
        data.background.scale = null;
        commitChanges();
    }

    public void setLineGradient(Gradient g) {
        data.line.gradient = g;
        line.setPreviewBorderColorInverseOf(g.stop.get(0).color);
        commitChanges();
    }

    public void setTrailGradient(Gradient g) {
        data.trail.gradient = g;
        trail.setPreviewBorderColorInverseOf(g.stop.get(0).color);
        commitChanges();
    }

    public void setBackgroundGradient(Gradient g) {
        data.background.gradient = g;
        background.setPreviewBorderColorInverseOf(g.stop.get(0).color);
        commitChanges();
    }

    private class StylePreview {
        private static final float PREVIEW_LINE_SPEED = 300;
        private static final float PREVIEW_TRAIL_SPEED = 500;

        private int _id = 0;

        private vec _size;
        private Bitmap _bitmap;
        private Canvas _c;

        private Sequence _s;

        private Handler _handler;
        public StylePreview(int id) {
            _id = id;
            _handler = new Handler();
        }        private Runnable _sequence_runnable = new Runnable() {
            @Override
            public void run() {
                if (this != _sequence_runnable)
                    return;

                ImageView view = (ImageView) _host.findViewById(R.id.style_editor_style_preview);

                if (_s == null || !_s.isActive()) {
                    newSequence();
                }

                if (data != null && _s != null && _c != null && _host != null && view != null) {
                    _s.update(FRAME_LENGTH_S);
                    _s.draw(_c, 0);

                    view.invalidate();

                    _handler.removeCallbacks(this);
                    _handler.postDelayed(_sequence_runnable, FRAME_LENGTH_MS);
                }
            }
        };

        public void prepare() {
            ImageView view = (ImageView) _host.findViewById(_id);

            if (view != null) {
                _size = new vec(view.getWidth(), view.getHeight());

                if (_bitmap != null)
                    _bitmap.recycle();

                _bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                _c = new Canvas(_bitmap);

                view.setImageBitmap(_bitmap);
                view.invalidate();

                newSequence();
                _handler.post(_sequence_runnable);
            }
        }

        private void newSequence() {
            PlaylistEntry e;

            if (_host.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                e = new PlaylistEntry("kikijiki", Assets.PREVIEW_SEQUENCE_PATH_LANDSCAPE, PlaylistEntry.Location.apk);
            } else {
                e = new PlaylistEntry("kikijiki",Assets.PREVIEW_SEQUENCE_PATH, PlaylistEntry.Location.apk);
            }

            _s = Sequence.makePreviewSequence(_host, e, _size, data.computeStyle(_host, _size.x, _size.y));
            Settings set = _s.getSettings();
            set.lineSpeed = PREVIEW_LINE_SPEED;
            set.trailSpeed = PREVIEW_TRAIL_SPEED;
        }

        public void onDataChanged() {
            _s.setStyle(data.computeStyle(_host, _size.x, _size.y));
        }

        public void onDestroy() {
            if (_handler != null && _sequence_runnable != null) {
                _handler.removeCallbacks(_sequence_runnable);
            }
        }


    }

    private class PaintPreview {
        private vec _size;
        private Bitmap _bitmap;
        private Paint _border = new Paint();

        private int _id = 0;

        public PaintPreview(int id) {
            _id = id;

            _border.setStrokeWidth(4.0f);
            _border.setColor(Color.GRAY);
            _border.setStyle(Paint.Style.STROKE);
        }

        public void prepare() {
            ImageView view = (ImageView) _host.findViewById(_id);

            if (view != null) {
                _size = new vec(view.getWidth(), view.getHeight());

                if (_bitmap != null)
                    _bitmap.recycle();

                _bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                view.setImageBitmap(_bitmap);
                view.invalidate();
            }
        }

        private void drawPaintPreviewBorder(Canvas c) {
            float half_width = _border.getStrokeWidth() * .5f;

            c.drawRect(half_width, half_width, _size.x - half_width, _size.y - half_width, _border);
        }

        public void drawPaintPreview(Paint paint) {
            Canvas c = new Canvas(_bitmap);
            c.drawColor(Color.BLACK);
            c.drawRect(0, 0, _size.x, _size.y, paint);
            drawPaintPreviewBorder(c);

            ImageView view = (ImageView) _host.findViewById(_id);
            view.invalidate();
        }

        private void setPreviewBorderColorInverseOf(int col) {
            int inv = Color.argb(
                    255,
                    255 - Color.red(col),
                    255 - Color.green(col),
                    255 - Color.blue(col));

            _border.setColor(inv);
        }

        @SuppressWarnings("incomplete-switch")
        public void setPreviewBorder(PaintData paint) {
            switch (paint.style) {
                case solid:
                    setPreviewBorderColorInverseOf(paint.color);
                    break;
                case gradient:
                    setPreviewBorderColorInverseOf(paint.gradient.stop.get(0).color);
                    break;
                case tile:
                    setPreviewBorderColorInverseOf(Color.GRAY);
                    break;
            }
        }
    }
}