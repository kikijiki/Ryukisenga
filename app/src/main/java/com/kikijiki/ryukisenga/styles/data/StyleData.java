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
import android.graphics.Color;
import android.graphics.Paint;

import com.kikijiki.ryukisenga.content.XMLFormat.StyleFillStyle;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleEntry;

import java.io.Serializable;

public class StyleData implements Serializable {
    public static final int DEFAULT_GRADIENT_COLOR_0 = Color.rgb(0, 76, 178);
    public static final int DEFAULT_GRADIENT_COLOR_1 = Color.rgb(1, 0, 40);
    public static final String DEFAULT_TILE = "default/tile0.png";
    public final static String DEFAULT_TILE_LOCATION_PREFIX = "default/";
    public final static String CUSTOM_TILE_LOCATION_PREFIX = "custom/";
    public static final int MAX_TILE_SIZE = 256;
    private static final long serialVersionUID = 1030018030683066927L;
    public StyleEntry info = new StyleEntry();

    public PaintData line = new PaintData();
    public PaintData trail = new PaintData();
    public PaintData background = new PaintData();

    public Style computeStyle(Context c, float vpX, float vpY) {
        Style ret = new Style();

        ret.line = line.computePaint(c, vpX, vpY, false);

        if (trail.style == StyleFillStyle.asLine) {
            ret.trail = ret.line;
        } else {
            ret.trail = trail.computePaint(c, vpX, vpY, false);
        }

        ret.background = background.computePaint(c, vpX, vpY, true);

        return ret;
    }

    public Paint computeLinePaint(Context c, float vpX, float vpY) {
        return line.computePaint(c, vpX, vpY, false);
    }

    public Paint computeTrailPaint(Context c, float vpX, float vpY) {
        if (trail.style == StyleFillStyle.asLine) {
            return computeLinePaint(c, vpX, vpY);
        } else {
            return line.computePaint(c, vpX, vpY, false);
        }
    }

    public Paint computeBackgroundPaint(Context c, float vpX, float vpY) {
        return background.computePaint(c, vpX, vpY, true);
    }

    public Paint computeLinePaintPreview(Context c, float vpX, float vpY) {
        return line.computePaint(c, vpX, vpY, true);
    }

    public Paint computeTrailPaintPreview(Context c, float vpX, float vpY) {
        if (trail.style == StyleFillStyle.asLine) {
            return computeLinePaintPreview(c, vpX, vpY);
        } else {
            return trail.computePaint(c, vpX, vpY, true);
        }
    }

    public Paint computeBackgroundPaintPreview(Context c, float vpX, float vpY) {
        return background.computePaint(c, vpX, vpY, true);
    }
}
