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

package com.kikijiki.ryukisenga.styles.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;

import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.content.XMLFormat.StyleFillStyle;

import java.io.InputStream;
import java.io.Serializable;

public class PaintData implements Serializable {
    private static final long serialVersionUID = 2167838558255849429L;

    public StyleFillStyle style = StyleFillStyle.solid;

    public float width = 1.0f;
    public int color = Color.BLACK;

    public Gradient gradient = new Gradient();

    public String tile = StyleData.DEFAULT_TILE;
    public Float scale = null;

    public static String getTilePath(String tilePath) {
        int start = tilePath.indexOf("/") + 1;
        start = Math.max(start, 0);
        start = Math.min(start, tilePath.length());

        return tilePath.substring(start, tilePath.length());
    }

    @SuppressWarnings("incomplete-switch")
    Paint computePaint(Context c, float vpX, float vpY, boolean isBackground) {
        Paint ret = new Paint();

        ret.setStyle(isBackground ? Paint.Style.FILL : Paint.Style.STROKE);
        ret.setStrokeWidth(width);
        ret.setStrokeCap(Paint.Cap.ROUND);
        ret.setAntiAlias(true);

        switch (style) {
            case solid: {
                ret.setColor(color);
            }
            break;

            case gradient: {
                Shader shader = gradient.computeShader(vpX, vpY);
                if (shader != null) {
                    ret.setShader(shader);
                }
            }
            break;

            case tile: {
                Bitmap b = null;

                if (isTileDefault()) {
                    b = BitmapFactory.decodeStream(Assets.apkOpenTile(c, getTilePath(tile)));
                } else {
                    InputStream is = Assets.sdOpenTile(c, getTilePath(tile));

                    if (is != null) {
                        b = BitmapFactory.decodeStream(is);
                    }
                }

                if (b != null) {
                    Shader shader = new BitmapShader(b, TileMode.REPEAT, TileMode.REPEAT);

                    if (scale != null) {
                        Matrix m = new Matrix();
                        m.setScale(scale, scale);
                        shader.setLocalMatrix(m);
                    }

                    if (shader != null) {
                        ret.setShader(shader);
                    }
                }
            }
            break;
        }

        return ret;
    }

    public PaintData clone() {
        PaintData ret = new PaintData();

        ret.color = color;
        ret.style = style;
        ret.tile = tile;
        ret.width = width;
        ret.gradient = gradient.clone();

        return ret;
    }

    public void setTile(String tile, boolean custom) {
        if (custom) {
            this.tile = StyleData.CUSTOM_TILE_LOCATION_PREFIX + tile;
        } else {
            this.tile = StyleData.DEFAULT_TILE_LOCATION_PREFIX + tile;
        }
    }

    public boolean isTileDefault() {
        return tile.startsWith(StyleData.DEFAULT_TILE_LOCATION_PREFIX);
    }
}