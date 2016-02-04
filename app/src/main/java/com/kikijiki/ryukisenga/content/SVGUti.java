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

import com.kikijiki.ryukisenga.Uti;
import com.kikijiki.ryukisenga.drawing.vec;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class SVGUti {
    public final static Map<String, Float> Units = new HashMap<String, Float>();

    static {
        Units.put("px", 1.0f);
        Units.put("pt", 1.25f);
        Units.put("pc", 15.0f);
        Units.put("mm", 3.543307f);
        Units.put("cm", 35.43307f);
        Units.put("in", 90.0f);
    }
    public final static String SVG_WIDTH = "width";

    //SVG
    public final static String SVG_HEIGHT = "height";
    //Transform
    public final static String SVG_TRANSFORM_MATRIX_START_BRACKET = "(";
    public final static String SVG_TRANSFORM_MATRIX_END_BRACKET = ")";
    public final static String SVG_TRANSFORM_MATRIX_COEFF_REGEX = "[\\s,;]+";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_MATRIX = "matrix";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_TRANSLATE = "translate";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_SCALE = "scale";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_ROTATE = "rotate";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_SKEWX = "skewX";
    public final static String SVG_TRANSFORM_MATRIX_TYPE_SKEWY = "skewY";
    //Group
    public final static String SVG_GROUP_TRANSFORM_ATTRIBUTE = "transform";
    //Path
    public final static String SVG_PATH_DATA_ATTRIBUTE = "d";
    //Rect
    public final static String SVG_RECT_X_ATTRIBUTE = "x";
    public final static String SVG_RECT_Y_ATTRIBUTE = "y";
    public final static String SVG_RECT_WIDTH_ATTRIBUTE = "width";

    ;
    public final static String SVG_RECT_HEIGHT_ATTRIBUTE = "height";
    //Line
    public final static String SVG_LINE_X1_ATTRIBUTE = "x1";

    ;
    public final static String SVG_LINE_X2_ATTRIBUTE = "x2";
    public final static String SVG_LINE_Y1_ATTRIBUTE = "y1";
    public final static String SVG_LINE_Y2_ATTRIBUTE = "y2";
    //Polyline
    public final static String SVG_POLYLINE_PINTS_ATTRIBUTE = "points";
    //Polygon
    public final static String SVG_POLYGON_PINTS_ATTRIBUTE = "points";

    ;
    //Circle
    public final static String SVG_CIRCLE_CNTX_ATTRIBUTE = "cx";

    ;
    public final static String SVG_CIRCLE_CNTY_ATTRIBUTE = "cy";

    ;
    public final static String SVG_CIRCLE_RADIUS_ATTRIBUTE = "r";

    ;
    //Ellipse
    public final static String SVG_ELLIPSE_CNTX_ATTRIBUTE = "cx";

    ;
    public final static String SVG_ELLIPSE_CNTY_ATTRIBUTE = "cy";

    ;
    public final static String SVG_ELLIPSE_RADIUSX_ATTRIBUTE = "rx";
    public final static String SVG_ELLIPSE_RADIUSY_ATTRIBUTE = "ry";

    public static vec getSize(Attributes attributes) {
        return new vec(SafeParseUnit(attributes, SVG_WIDTH), SafeParseUnit(attributes, SVG_HEIGHT));
    }

    public static float[] getViewBox(Attributes attributes) {
        String data = attributes.getValue("viewBox");

        if (data != null) {
            String[] coord = data.split("\\s*(,|\\s)\\s*");

            if (coord.length == 4) {
                float[] ret = new float[]{.0f, .0f, .0f, .0f};

                ret[0] = Uti.safeParseFloat(coord[0]);
                ret[1] = Uti.safeParseFloat(coord[1]);
                ret[2] = Uti.safeParseFloat(coord[2]);
                ret[3] = Uti.safeParseFloat(coord[3]);

                return ret;
            } else {
                return null;
            }
        }

        return null;
    }

    public static String getTransform(Attributes a) {
        return a.getValue(SVG_GROUP_TRANSFORM_ATTRIBUTE);
    }

    ;

    public static String getPathData(Attributes a) {
        return a.getValue(SVG_PATH_DATA_ATTRIBUTE);
    }

    ;

    public static float getRectX(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_X_ATTRIBUTE);
    }

    ;

    public static float getRectY(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_Y_ATTRIBUTE);
    }

    ;

    public static vec getRectTopLeft(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_X_ATTRIBUTE, SVG_RECT_Y_ATTRIBUTE);
    }

    ;

    public static float getRectWidth(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_WIDTH_ATTRIBUTE);
    }

    ;

    public static float getRectHeight(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_HEIGHT_ATTRIBUTE);
    }

    public static vec getRectSize(Attributes a) {
        return SVGUti.SafeParse(a, SVG_RECT_WIDTH_ATTRIBUTE, SVG_RECT_HEIGHT_ATTRIBUTE);
    }

    ;

    public static float getLineX1(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_X1_ATTRIBUTE);
    }

    public static float getLineY1(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_Y1_ATTRIBUTE);
    }

    ;

    public static vec getLineStart(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_X1_ATTRIBUTE, SVG_LINE_Y1_ATTRIBUTE);
    }

    public static float getLineX2(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_X2_ATTRIBUTE);
    }

    public static float getLineY2(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_Y2_ATTRIBUTE);
    }

    public static vec getLineEnd(Attributes a) {
        return SVGUti.SafeParse(a, SVG_LINE_X2_ATTRIBUTE, SVG_LINE_Y2_ATTRIBUTE);
    }

    ;

    public static String getPolylinePoints(Attributes a) {
        return a.getValue(SVG_POLYLINE_PINTS_ATTRIBUTE);
    }

    ;

    public static String getPolygonPoints(Attributes a) {
        return a.getValue(SVG_POLYGON_PINTS_ATTRIBUTE);
    }

    ;

    public static float getCircleCntX(Attributes a) {
        return SVGUti.SafeParse(a, SVG_CIRCLE_CNTX_ATTRIBUTE);
    }

    ;

    public static float getCircleCntY(Attributes a) {
        return SVGUti.SafeParse(a, SVG_CIRCLE_CNTY_ATTRIBUTE);
    }

    public static vec getCircleCnt(Attributes a) {
        return SVGUti.SafeParse(a, SVG_CIRCLE_CNTX_ATTRIBUTE, SVG_CIRCLE_CNTY_ATTRIBUTE);
    }

    public static float getCircleRadius(Attributes a) {
        return SVGUti.SafeParse(a, SVG_CIRCLE_RADIUS_ATTRIBUTE);
    }

    public static float getEllipseCntX(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_CNTX_ATTRIBUTE);
    }

    public static float getEllipseCntY(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_CNTY_ATTRIBUTE);
    }

    ;

    public static vec getEllipseCnt(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_CNTX_ATTRIBUTE, SVG_ELLIPSE_CNTY_ATTRIBUTE);
    }

    ;

    public static float getEllipseRadiusX(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_RADIUSX_ATTRIBUTE);
    }

    ;

    public static float getEllipseRadiusY(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_RADIUSY_ATTRIBUTE);
    }

    ;

    public static vec getEllipseRadius(Attributes a) {
        return SVGUti.SafeParse(a, SVG_ELLIPSE_RADIUSX_ATTRIBUTE, SVG_ELLIPSE_RADIUSY_ATTRIBUTE);
    }

    ;

    public static float SafeParse(String[] src, int srci) {
        float ret = .0f;

        if (srci < src.length && src[srci] != null) {
            try {
                ret = Float.parseFloat(src[srci]);
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }
        }

        return ret;
    }

    ;

    private static vec SafeParse(Attributes a, String a1, String a2) {
        return new vec(SafeParse(a, a1), SafeParse(a, a2));
    }

    public static float SafeParseUnit(Attributes a, String id) {
        float ret = .0f;

        String value = a.getValue(id);

        if (value.length() <= 2 || !SVGUti.Units.containsKey(value.substring(value.length() - 2, value.length()))) {
            try {
                ret = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }
        } else {
            String unit = value.substring(value.length() - 2);
            float mod = 1.0f;

            if (SVGUti.Units.containsKey(unit)) {
                mod = SVGUti.Units.get(unit);
            }

            float v = .0f;

            try {
                v = Float.parseFloat((String) value.subSequence(0, value.length() - 2));
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }

            ret = v * mod;

        }

        return ret;
    }

    public static void SafeParse(String[] src, int srci, float[] dst, int dsti) {
        if (srci < src.length && src[srci] != null) {
            float v = .0f;

            try {
                v = Float.parseFloat(src[srci]);
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            } finally {
                dst[dsti] = v;
            }
        } else {
            dst[dsti] = .0f;
        }
    }

    public static float SafeParse(Attributes a, String id) {
        String v = a.getValue(id);
        float ret = .0f;

        if (v != null) {
            try {
                ret = Float.parseFloat(v);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public static void parseIgnoreNamespace(InputStream is, ContentHandler ch) throws SAXException, ParserConfigurationException, IOException {
        XMLReader r;

        if (is == null) {
            return;
        }

        InputSource source = new InputSource(is);

        r = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        r.setFeature("http://xml.org/sax/features/namespaces", false);
        r.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        r.setContentHandler(ch);
        r.parse(source);

        is.close();
    }

    public enum SvgTag {
        path,
        rect,
        polyline,
        polygon,
        line,
        circle,
        ellipse,
        g,
        ignore,
        svg,
    }
}
