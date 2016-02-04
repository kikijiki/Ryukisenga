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

package com.kikijiki.ryukisenga.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class GraduatedSeekbar extends HorizontalScrollView {
    private static final int DIVIDERS = 8;
    private ImageView _grad;
    private Paint _line_paint = new Paint();

    private float _k = .0f;
    private float _inv_k;

    private float _v = .0f;
    private boolean _initialized = false;

    private GraduatedSeekbarListener _l;
    private int _width;
    private int _height;

    private Drawable _background;

    private boolean _select = false;

    public GraduatedSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public GraduatedSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraduatedSeekbar(Context context) {
        super(context);
        init();
    }

    private void init() {
        _line_paint.setStrokeWidth(2);
        _line_paint.setAlpha(127);
        _line_paint.setColor(Color.RED);

        setHorizontalFadingEdgeEnabled(false);
        setVerticalFadingEdgeEnabled(false);

        _grad = new ImageView(getContext());

        addView(_grad);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        _initialized = false;

        super.onLayout(changed, l, t, r, b);

        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);

        _width = r - l;
        _height = b - t;

        prepareBackground(_width, _height);

        _grad.layout(0, 0, _width * 4, _height);

        _k = _width * 3; //_grad.getWidth() - getWidth()
        _inv_k = 1.0f / _k;

        _initialized = true;

        setValue(_v);
    }

    @SuppressWarnings("deprecation")
    private void prepareBackground(int w, int h) {
        float margin = (float) h * .1f;
        float dl = h - margin;
        float h2 = h * .5f;

        _background = new ColorDrawable(Color.DKGRAY);

        Bitmap bm = Bitmap.createBitmap(h, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bm);

        Paint p = new Paint();
        p.setStrokeWidth(4);
        p.setAntiAlias(true);
        p.setColor(Color.rgb(200, 200, 200));
        p.setAlpha(200);
        p.setStyle(Style.STROKE);

        Path path = new Path();
        path.moveTo(dl, h2 + 4);
        path.lineTo(dl, dl);
        path.lineTo(margin, dl);
        path.close();

        c.drawPath(path, p);

        BitmapDrawable back = new BitmapDrawable(bm);
        back.setTileModeXY(TileMode.REPEAT, TileMode.MIRROR);

        _grad.setBackgroundDrawable(back);

        LayoutParams params = new LayoutParams(w * 4, h);
        _grad.setLayoutParams(params);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas c) {
        //super.onDraw(c);

        float l = getScrollX();
        float w = getWidth();
        //float r = l + w;
        float h = getHeight();
        float h2 = h * .5f;

        float x = l + w * getValue();

        _line_paint.setColor(Color.WHITE);
        _line_paint.setStrokeWidth(2.0f);
        _line_paint.setAlpha(100);

        _background.setBounds((int) l, 0, (int) w + (int) l, (int) h);
        _background.draw(c);

        for (int i = 0; i <= DIVIDERS; i++) {
            float g = l + w / (float) DIVIDERS * (float) i;

            c.drawLine(g, 0, g, h2, _line_paint);
        }

        _line_paint.setColor(Color.RED);
        _line_paint.setStrokeWidth(2.0f);
        _line_paint.setAlpha(200);
        c.drawLine(x, 0, x, h2, _line_paint);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (_initialized) {
            _v = Math.max(.0f, Math.min(1.0f, 1.0f - l * _inv_k));

            if (_l != null) {
                _l.onScroll(_v);
            }
        }
    }

    public float getValue() {
        return _v;
    }

    public void setValue(float v) {
        scrollTo((int) ((1.0f - v) * _k), 0);
        _v = v;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getY() >= _height / 2.0f) {
            return super.onTouchEvent(ev);
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            _select = true;
        } else if (ev.getAction() == MotionEvent.ACTION_UP && _select) {
            _select = false;

            float x = ev.getX();
            int rounded = (int) (x / _width * (float) (DIVIDERS - 1)) + 1;

            setValue((float) rounded / (float) DIVIDERS);

            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            _select = false;
        }

        return super.onTouchEvent(ev);
    }

    public void setOnScrollListener(GraduatedSeekbarListener l) {
        _l = l;
    }

    public void setInitialValue(float v) {
        _v = v;
    }

    public interface GraduatedSeekbarListener {
        void onScroll(float value);
    }
}
