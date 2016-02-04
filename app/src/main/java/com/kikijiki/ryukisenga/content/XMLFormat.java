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

package com.kikijiki.ryukisenga.content;

import android.graphics.Color;

import com.kikijiki.ryukisenga.Uti;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLFormat {
    public final static String STYLE_NAME_ATTRIBUTE = "name";
    public final static String STYLE_STROKE_WIDTH = "width";
    public final static String STYLE_FILL_STYLE_ATTRIBUTE = "style";
    public final static String STYLE_FILL_COLOR_ATTRIBUTE = "color";
    public final static String STYLE_FILL_TILE_ATTRIBUTE = "tile";
    public final static String STYLE_FILL_SCALE_ATTRIBUTE = "scale";
    public final static String STYLE_GRADIENT_ANGLE = "angle";
    public final static String STYLE_FILL_AS_LINE = "asLine";
    public final static String STYLE_STOP_OFFSET_ATTRIBUTE = "offset";
    public final static String STYLE_STOP_COLOR_ATTRIBUTE = "color";

    public static String styleGetName(Attributes a) {
        return a.getValue(STYLE_NAME_ATTRIBUTE);
    }

    public static StyleFillStyle styleGetFillStyle(Attributes a) {
        return StyleFillStyle.valueOf(a.getValue(STYLE_FILL_STYLE_ATTRIBUTE));
    }

    public static float styleGetStrokeWidth(Attributes a) {
        return Uti.safeParseFloat(a.getValue(STYLE_STROKE_WIDTH));
    }

    public static float styleGetGradientAngle(Attributes a) {
        return Uti.safeParseFloat(a.getValue(STYLE_GRADIENT_ANGLE));
    }

    public static int styleGetFillColor(Attributes a) {
        return Color.parseColor(a.getValue(STYLE_FILL_COLOR_ATTRIBUTE));
    }

    public static String styleGetFillTile(Attributes a) {
        return a.getValue(STYLE_FILL_TILE_ATTRIBUTE);
    }

    public static Float styleGetFillScale(Attributes a) {
        return Uti.safeParseNullableFloat(a.getValue(STYLE_FILL_SCALE_ATTRIBUTE));
    }

    public static Float styleGetStopOffset(Attributes a) {
        String offset = a.getValue(STYLE_STOP_OFFSET_ATTRIBUTE);

        if (offset == null) {
            return null;
        } else {
            return Uti.safeParseFloat(a.getValue(STYLE_STOP_OFFSET_ATTRIBUTE));
        }
    }

    public static int styleGetStopColor(Attributes a) {
        return Color.parseColor(a.getValue(STYLE_STOP_COLOR_ATTRIBUTE));
    }

    private static String colorToString(int color) {
        return "#" + Integer.toHexString(color);
    }

    public static void writeFillColorAttribute(XmlSerializer s, int color) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_FILL_COLOR_ATTRIBUTE, colorToString(color));
    }

    public static void writeFillStyleAttribute(XmlSerializer s, StyleFillStyle style) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_FILL_STYLE_ATTRIBUTE, style.toString());
    }

    public static void writeFillTile(XmlSerializer s, String tile) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_FILL_TILE_ATTRIBUTE, tile);
    }

    public static void writeFillScale(XmlSerializer s, float scale) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_FILL_SCALE_ATTRIBUTE, Float.toString(scale));
    }

    public static void writeFillGradientAttributes(XmlSerializer s, float a) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_GRADIENT_ANGLE, Float.toString(a));
    }

    public static void writeFillGradientStopAttributes(XmlSerializer s, float offset, int color) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_STOP_OFFSET_ATTRIBUTE, Float.toString(offset));
        s.attribute(null, STYLE_STOP_COLOR_ATTRIBUTE, colorToString(color));
    }

    public static void writeFillUniformGradientStopAttributes(XmlSerializer s, int color) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_STOP_COLOR_ATTRIBUTE, colorToString(color));
    }

    public static void writeStrokeWidthAttribute(XmlSerializer s, float width) throws IllegalArgumentException, IllegalStateException, IOException {
        s.attribute(null, STYLE_STROKE_WIDTH, Float.toString(width));
    }

    public enum VectorTag {
        image,
        title,
        style,
    }

    public enum ArtistTag {
        artist,
        name,
        info,
        website,
        email,
        twitter,
    }

    public enum StyleTag {
        style,
        name,
        line,
        trail,
        background,
        stop,
    }

    public enum StyleFillStyle {
        solid,
        gradient,
        tile,
        scale,
        asLine //for the trail
    }
}