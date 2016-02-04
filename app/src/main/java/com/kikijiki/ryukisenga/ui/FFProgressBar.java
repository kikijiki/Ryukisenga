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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

public class FFProgressBar {
    private Paint _paint;
    private Shader _outer_shader;
    private Shader _inner_shader;

    private RectF _outer_rect;
    private RectF _inner_rect;

    private float _bar_width;

    private int _border_color = Color.GRAY;
    private float _border_corner = 4.0f;
    private float _border_width = 2.0f;

    public FFProgressBar(float top, float left, float width, float height) {
        float padding = height * .25f;
        _border_corner = height * .1f;
        _border_width = height * .05f;

        _outer_rect = new RectF(
                top,
                left,
                top + width,
                left + height);

        _inner_rect = new RectF(
                top + padding,
                left + padding,
                top + width - padding,
                left + height - padding);

        _bar_width = width - padding * 2.0f;

        _paint = new Paint();

        _outer_shader = new LinearGradient(top, left, top, left + height, Color.parseColor("#004cb2"), Color.parseColor("#010028"), Shader.TileMode.CLAMP);
        _inner_shader = new LinearGradient(
                top + padding, left + padding, top + padding, left + height - padding,
                new int[]{Color.parseColor("#121fb7"), Color.parseColor("#545aff"), Color.parseColor("#00056e")},
                null, Shader.TileMode.CLAMP);
    }

    public void draw(Canvas c, float progress) {
        c.save();
        c.setMatrix(null);

        _inner_rect.right = _inner_rect.left + progress * _bar_width;

        _paint.setShader(_outer_shader);
        _paint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(_outer_rect, _border_corner, _border_corner, _paint);

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(_border_width);
        _paint.setShader(null);
        _paint.setColor(_border_color);
        c.drawRoundRect(_outer_rect, _border_corner, _border_corner, _paint);

        _paint.setStyle(Paint.Style.FILL);
        _paint.setShader(_inner_shader);
        c.drawRect(_inner_rect, _paint);

        c.restore();
    }
}
