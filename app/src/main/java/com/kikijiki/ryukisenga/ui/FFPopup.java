/*
 * Copyright 2014 Matteo Bernacchia <dev@kikijiki.com>
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;

import com.kikijiki.ryukisenga.drawing.vec;

import java.util.Random;

public class FFPopup {
    private Paint _popup_paint;
    private Shader _outer_shader;

    private RectF _outer_rect;
    private float[] _coord;

    private int _border_color = Color.LTGRAY;
    private float _border_corner = 4.0f;
    private float _border_width = 2.0f;

    private String _message;
    private Paint _text_paint;

    private float _padding;
    private float _offset;

    private float _size;
    private vec _viewport;
    private vec _dir;
    private vec _pos = new vec();
    private Matrix _transform;
    private int _bounces = 0;
    private int _bounce_max = 4;
    private Random _random = new Random();
    private float _speed;

    public FFPopup(Context c, int message, float size, float speed, vec viewport) {
        _message = c.getString(message);
        _size = size;

        _popup_paint = new Paint();
        _text_paint = new Paint();

        _text_paint.setTextSize(_size);

        _viewport = viewport;

        _speed = speed;
        _dir = new vec(_random, speed);

        float width = _text_paint.measureText(_message);
        float height = _size;

        _padding = height * .5f;
        _border_corner = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, c.getResources().getDisplayMetrics()); //height * .1f;
        _border_width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, c.getResources().getDisplayMetrics());
        _offset = _size * 0.125f;

        _outer_rect = new RectF(
                0,
                0,
                width + _padding * 2 + _offset,
                height + _padding * 2 + _offset);

        _coord = new float[]{
                _padding,
                _padding + _size,
                _padding + _offset,
                _padding + _offset + _size};

        _outer_shader = new LinearGradient(0, 0, 0, height, Color.parseColor("#004cb2"), Color.parseColor("#010028"), Shader.TileMode.CLAMP);
        _transform = new Matrix();
    }

    public void update(float sec) {
        _pos.x += _dir.x * sec;
        _pos.y += _dir.y * sec;

        if (_pos.x < .0f) {
            _dir.x = -_dir.x;
            _pos.x = .0f;
            _bounces++;
        } else if (_pos.x > _viewport.x - _outer_rect.width()) {
            _dir.x = -_dir.x;
            _pos.x = _viewport.x - _outer_rect.width();
            _bounces++;
        }

        if (_pos.y < .0f) {
            _dir.y = -_dir.y;
            _pos.y = .0f;
            _bounces++;
        } else if (_pos.y > _viewport.y - _outer_rect.height()) {
            _dir.y = -_dir.y;
            _pos.y = _viewport.y - _outer_rect.height();
            _bounces++;
        }

        if (_bounces >= _bounce_max) {
            _dir = new vec(_random, _speed * (1.0f + (.5f - _random.nextFloat())));
            _bounces = 0;
            _bounce_max = _random.nextInt(10);
        }

        _transform.setTranslate(_pos.x, _pos.y);
    }

    public void draw(Canvas c) {
        c.save();
        c.setMatrix(_transform);

        _popup_paint.setShader(_outer_shader);
        _popup_paint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(_outer_rect, _border_corner, _border_corner, _popup_paint);

        _popup_paint.setStyle(Paint.Style.STROKE);
        _popup_paint.setStrokeWidth(_border_width);
        _popup_paint.setShader(null);
        _popup_paint.setColor(_border_color);
        c.drawRoundRect(_outer_rect, _border_corner, _border_corner, _popup_paint);

        _text_paint.setColor(Color.BLACK);
        c.drawText(_message, _coord[2], _coord[1], _text_paint);
        c.drawText(_message, _coord[0], _coord[3], _text_paint);
        c.drawText(_message, _coord[2], _coord[3], _text_paint);

        _text_paint.setColor(Color.WHITE);
        c.drawText(_message, _coord[0], _coord[1], _text_paint);

        c.restore();
    }
}
