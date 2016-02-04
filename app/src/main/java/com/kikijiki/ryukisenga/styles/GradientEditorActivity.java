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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kikijiki.ryukisenga.ui.ColorPickerDialog;
import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.styles.data.Gradient;
import com.kikijiki.ryukisenga.styles.data.GradientStop;
import com.kikijiki.ryukisenga.ui.GraduatedSeekbar;
import com.kikijiki.ryukisenga.ui.GraduatedSeekbar.GraduatedSeekbarListener;

import java.io.Serializable;
import java.util.Random;

public class GradientEditorActivity extends Activity {
    private static final int COLOR_PICKER_RESULT_CODE = 1;

    private GradientAdapter _adapter;
    private CheckBox _uniform;

    private int _start = Color.BLACK;
    private int _end = Color.WHITE;

    private ImageView _preview;
    private Bitmap _preview_bitmap;
    private Paint _preview_paint_flat = new Paint();
    private Paint _preview_paint = new Paint();
    private int _flat_height = 24;
    private Paint _line_paint = new Paint();
    private Canvas _preview_canvas;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.gradient_editor);

        _flat_height = getResources().getDimensionPixelSize(R.dimen.flat_preview_height);
        _line_paint.setStrokeWidth(1.0f);

        _adapter = new GradientAdapter();

        Gradient gradient = null;

        if (state != null) {
            _start = state.getInt("start");
            _end = state.getInt("end");

            Gradient saved_gradient = (Gradient) state.getSerializable("gradient");

            if (saved_gradient != null) {
                gradient = saved_gradient;
            } else {
                if (getIntent().getExtras() != null) {
                    gradient = (Gradient) getIntent().getExtras().getSerializable(StyleEditorActivity.GRADIENT_EXTRA);

                    if (gradient != null) {
                        _end = gradient.stop.remove(gradient.stop.size() - 1).color;
                        _start = gradient.stop.remove(0).color;
                    }
                }
            }
        } else {
            if (getIntent().getExtras() != null) {
                gradient = (Gradient) getIntent().getExtras().getSerializable(StyleEditorActivity.GRADIENT_EXTRA);

                if (gradient != null) {
                    _end = gradient.stop.remove(gradient.stop.size() - 1).color;
                    _start = gradient.stop.remove(0).color;
                }
            }
        }

        _adapter.setGradient(gradient);

        final Button save = (Button) findViewById(R.id.gradient_editor_save_and_exit);
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });

        final ImageView start = (ImageView) findViewById(R.id.gradient_editor_stop_start);
        //start.setBackgroundColor(_start);
        start.setImageDrawable(new ColorDrawable(_start));
        start.setOnClickListener(new OnClickListener() {
            @Override

            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(GradientEditorActivity.this, _start, new ColorPickerDialog.ColorPickerListener() {
                    @Override
                    public void onCancel(ColorPickerDialog dialog) {
                    }

                    @Override
                    public void onOk(ColorPickerDialog dialog, int color) {
                        _start = color;
                        //start.setBackgroundColor(_start);
                        start.setImageDrawable(new ColorDrawable(_start));
                        updateGradient();
                    }
                });
                picker.show();
            }
        });

        final ImageView end = (ImageView) findViewById(R.id.gradient_editor_stop_end);
        //end.setBackgroundColor(_end);
        end.setImageDrawable(new ColorDrawable(_end));
        end.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(GradientEditorActivity.this, _end, new ColorPickerDialog.ColorPickerListener() {
                    @Override
                    public void onCancel(ColorPickerDialog dialog) {
                    }

                    @Override
                    public void onOk(ColorPickerDialog dialog, int color) {
                        _end = color;
                        end.setImageDrawable(new ColorDrawable(_end));
                        updateGradient();
                    }
                });
                picker.show();
            }
        });

        _uniform = (CheckBox) findViewById(R.id.gradient_editor_uniform);
        _uniform.setChecked(_adapter.getGradient().uniform);
        _uniform.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _adapter.getGradient().uniform = isChecked;
                updateGradient();
            }
        });

        Button add_stop = new Button(this);
        add_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float off = .0f;

                Random r = new Random();
                int col = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));

                if (_adapter.getCount() > 0) {
                    for (int i = 0; i < _adapter.getCount(); i++) {
                        off = Math.max(off, _adapter.getItem(i).offset);
                    }

                    off = 1.0f - (1.0f - off) * .5f;
                } else {
                    off = .5f;
                }


                _adapter.add(new GradientStop(off, col));
                updateGradient();
            }
        });

        add_stop.setText(R.string.gradient_editor_add_stop);

        GraduatedSeekbar angle_seek = (GraduatedSeekbar) findViewById(R.id.gradient_editor_gradient_angle_seekbar);
        angle_seek.setInitialValue((_adapter.getGradient().angle / (float) Math.PI + 1.0f) * .5f);
        angle_seek.setOnScrollListener(new GraduatedSeekbarListener() {
            @Override
            public void onScroll(float value) {
                _adapter.getGradient().angle = (2.0f * value - 1.0f) * (float) Math.PI;
                updateGradient();
            }
        });

        _preview = (ImageView) findViewById(R.id.gradient_editor_preview);

        ListView list = (ListView) findViewById(R.id.gradient_editor_stop_list);
        list.addFooterView(add_stop);
        list.setAdapter(_adapter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            _preview_bitmap = Bitmap.createBitmap(_preview.getWidth(), _preview.getHeight(), Bitmap.Config.ARGB_8888);
            _preview.setImageBitmap(_preview_bitmap);
            _preview_canvas = new Canvas(_preview_bitmap);
            updateGradient();
        }
    }

    private void updateGradient() {
        if (_preview == null || _preview_bitmap == null || _uniform == null)
            return;

        float w = _preview.getWidth();
        float h = _preview.getHeight() - _flat_height;

        _preview_paint.setDither(true);
        _preview_paint_flat.setDither(true);

        Gradient g = _adapter.getGradient();
        Shader[] shader = g.computePreviewShaders(w, h, _start, _end);

        _preview_paint.setShader(shader[0]);

        _preview_paint_flat.setShader(shader[1]);

        _preview_canvas.drawPaint(_preview_paint_flat);
        _preview_canvas.drawRect(0, 0, w, h, _preview_paint);

        _line_paint.setColor(Color.BLACK);
        _preview_canvas.drawLine(0, h, w, h, _line_paint);

        _line_paint.setColor(Color.WHITE);
        _preview_canvas.drawLine(0, h + 1, w, h + 1, _line_paint);

        _preview.invalidate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("start", _start);
        outState.putInt("end", _end);
        outState.putSerializable("gradient", _adapter.getGradient());
    }

    private void saveAndExit() {
        Gradient ret = _adapter.getGradient();
        ret.addExtremes(_start, _end);

        Intent i = getIntent();
        i.putExtra(StyleEditorActivity.GRADIENT_EXTRA, ret);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class GradientAdapter extends BaseAdapter {
        private Gradient _g = new Gradient();

        public void add(GradientStop stop) {
            _g.stop.add(stop);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(GradientEditorActivity.this).inflate(R.layout.gradient_editor_stop, null);
            }

            final GradientStop cur = _g.stop.get(position);

            final GraduatedSeekbar wheel = (GraduatedSeekbar) convertView.findViewById(R.id.gradient_editor_stop_wheel);
            wheel.setInitialValue(cur.offset);
            wheel.setOnScrollListener(new GraduatedSeekbarListener() {
                @Override
                public void onScroll(float value) {
                    cur.offset = value;
                    updateGradient();
                }
            });

            final ImageView stop_color = (ImageView) convertView.findViewById(R.id.gradient_editor_stop_color);
            stop_color.setBackgroundColor(cur.color);
            stop_color.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog picker = new ColorPickerDialog(GradientEditorActivity.this, cur.color, new ColorPickerDialog.ColorPickerListener() {
                        @Override
                        public void onCancel(ColorPickerDialog dialog) {
                        }

                        @Override
                        public void onOk(ColorPickerDialog dialog, int color) {
                            cur.color = color;
                            stop_color.setBackgroundColor(cur.color);
                            updateGradient();
                        }
                    });
                    picker.show();
                }
            });

            TextView delete = (TextView) convertView.findViewById(R.id.gradient_editor_stop_delete);
            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    _g.stop.remove(cur);
                    notifyDataSetChanged();
                    updateGradient();
                }
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return _g.stop.size();
        }

        @Override
        public GradientStop getItem(int position) {
            return _g.stop.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Gradient getGradient() {
            return _g;
        }

        public void setGradient(Serializable serializable) {
            if (serializable != null) {
                _g = (Gradient) serializable;
                notifyDataSetChanged();
            }
        }
    }
}