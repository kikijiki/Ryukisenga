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

package com.kikijiki.ryukisenga.content;

import android.annotation.SuppressLint;
import android.graphics.Matrix;

import com.kikijiki.ryukisenga.Uti;
import com.kikijiki.ryukisenga.content.SVGUti.SvgTag;
import com.kikijiki.ryukisenga.drawing.Sen;
import com.kikijiki.ryukisenga.drawing.Sequence.Settings;
import com.kikijiki.ryukisenga.drawing.vec;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.Stack;

public class SvgContentHandler extends DefaultHandler {
    private final static float PI2 = (float) (Math.PI * 2);
    private final int CIRCLE_ELLIPSE_SEGMENTS_MIN = 4;
    private WeakReference<ParserInterface> _thread;

    private Stack<Matrix> _transform = new Stack<Matrix>();
    private boolean _abort = false;
    private Random _rnd = new Random();
    private Settings _settings;

    private CubicBezierFlattener _flattener;

    public SvgContentHandler(ParserInterface thread, Settings settings) {
        _thread = new WeakReference<ParserInterface>(thread);

        Matrix root = new Matrix();
        if (!(settings.viewbox.x < 0 || settings.viewbox.y < 0)) {
            root.setTranslate(-settings.viewbox.x, -settings.viewbox.y);
        }

        _transform.push(root);
        _settings = settings;
        _flattener = new CubicBezierConverterFloat(_settings.invScale);
    }

    private Matrix getCurrentTransform(Attributes attributes, boolean push) {
        String matrix = SVGUti.getTransform(attributes);

        if (matrix == null) {
            if (push) {
                _transform.push(_transform.peek());
            }

            return _transform.peek();
        } else {
            Matrix cur = parseTransform(matrix);
            cur.preConcat(_transform.peek());

            if (push) {
                _transform.push(cur);
            }

            return cur;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        checkAbort();

        SvgTag tag = SvgTag.ignore;

        try {
            tag = SvgTag.valueOf(qName.toLowerCase());
        } catch (Exception e) {
            //e.printStackTrace();
        }

        switch (tag) {
            case svg:
                break;

            case g:
                getCurrentTransform(attributes, true);
                break;

            case path: {
                PathReader reader = new PathReader(SVGUti.getPathData(attributes));
                PathData pathData = new PathData();
                PointBuffer pointBuffer = new PointBuffer();

                Matrix matrix = getCurrentTransform(attributes, false);

                while (true) {
                    reader.skipSpaces();

                    if (reader.hasNext()) {
                        char c = reader.peekNext();

                        if (c >= 'A' && c <= 'z') {
                            pathData.nextCurve = reader.getNext();
                        }

                        pointBuffer = parseCurve(reader, pathData, pointBuffer, matrix);
                    } else {
                        break;
                    }
                }

                if (!pointBuffer.isEmpty())
                    appendElement(pointBuffer);
            }
            break;

            case rect: {
                float x = SVGUti.getRectX(attributes);
                float y = SVGUti.getRectY(attributes);
                float w = SVGUti.getRectWidth(attributes);
                float h = SVGUti.getRectHeight(attributes);

                float xx = x + w;
                float yy = y + h;

                Matrix m = getCurrentTransform(attributes, false);

                float[] pt = {
                        x, y,
                        xx, y,
                        xx, yy,
                        x, yy,
                        x, y};

                m.mapPoints(pt);

                PointBuffer buffer = new PointBuffer();
                buffer.append(pt[0], pt[1]);
                buffer.append(pt[2], pt[3]);
                buffer.append(pt[4], pt[5]);
                buffer.append(pt[6], pt[7]);

                appendElement(buffer);
            }
            break;
            case line: {
                float x1 = SVGUti.getLineX1(attributes);
                float y1 = SVGUti.getLineY1(attributes);
                float x2 = SVGUti.getLineX2(attributes);
                float y2 = SVGUti.getLineY2(attributes);

                Matrix m = getCurrentTransform(attributes, false);

                float[] pt = {x1, y1, x2, y2};
                m.mapPoints(pt);

                PointBuffer buffer = new PointBuffer();
                buffer.append(pt[0], pt[1]);
                buffer.append(pt[2], pt[3]);

                appendElement(buffer);
            }
            break;

            case polyline: {
                appendElement(computePolyline(SVGUti.getPolylinePoints(attributes), false, getCurrentTransform(attributes, false)));
            }
            break;
            case polygon: {
                appendElement(computePolyline(SVGUti.getPolygonPoints(attributes), true, getCurrentTransform(attributes, false)));
            }
            break;

            case circle: {
                vec cnt = SVGUti.getCircleCnt(attributes);
                float r = SVGUti.getCircleRadius(attributes);

                appendElement(computeEllipse(r, r, cnt.x, cnt.y, getCurrentTransform(attributes, false)));
            }
            break;

            case ellipse: {
                vec cnt = SVGUti.getEllipseCnt(attributes);
                vec r = SVGUti.getEllipseRadius(attributes);

                appendElement(computeEllipse(r.x, r.y, cnt.x, cnt.y, getCurrentTransform(attributes, false)));
            }
            break;

            case ignore:
                break;
        }
    }

    @SuppressWarnings("unused")
    private PointBuffer parseCurve(PathReader reader, PathData pathData, PointBuffer pointBuffer, Matrix m) throws SAXException {
        pathData.previousCurve = pathData.nextCurve;

        switch (pathData.nextCurve) {
            case 'm': {
                if (!pointBuffer.isEmpty())
                    appendElement(pointBuffer);

                pointBuffer = new PointBuffer();

                pathData.startX = pathData.previousX + reader.parseFloat();
                pathData.startY = pathData.previousY + reader.parseFloat();

                pathData.previousX = pathData.startX;
                pathData.previousY = pathData.startY;

                pointBuffer.append(pathData.startX, pathData.startY, m);

                pathData.nextCurve = 'l';
            }
            break;

            case 'M': {
                if (!pointBuffer.isEmpty())
                    appendElement(pointBuffer);

                pointBuffer = new PointBuffer();

                pathData.startX = reader.parseFloat();
                pathData.startY = reader.parseFloat();

                pathData.previousX = pathData.startX;
                pathData.previousY = pathData.startY;

                pointBuffer.append(pathData.startX, pathData.startY, m);

                pathData.nextCurve = 'L';
            }
            break;

            case 'z':
            case 'Z': {
                pathData.previousX = pathData.startX;
                pathData.previousY = pathData.startY;

                pointBuffer.append(pathData.startX, pathData.startY, m);
            }
            break;

            case 'h': {
                float x = pathData.previousX + reader.parseFloat();

                pathData.previousX = x;

                pointBuffer.append(x, pathData.previousY, m);
            }
            break;

            case 'H': {
                float x = reader.parseFloat();

                pathData.previousX = x;

                pointBuffer.append(x, pathData.previousY, m);
            }
            break;

            case 'v': {
                float y = pathData.previousY + reader.parseFloat();

                pathData.previousY = y;

                pointBuffer.append(pathData.previousX, y, m);
            }
            break;

            case 'V': {
                float y = reader.parseFloat();

                pathData.previousY = y;

                pointBuffer.append(pathData.previousX, y, m);
            }
            break;

            case 'l': {
                float x = pathData.previousX + reader.parseFloat();
                float y = pathData.previousY + reader.parseFloat();

                pathData.previousX = x;
                pathData.previousY = y;

                pointBuffer.append(x, y, m);
            }
            break;

            case 'L': {
                float x = reader.parseFloat();
                float y = reader.parseFloat();

                pathData.previousX = x;
                pathData.previousY = y;

                pointBuffer.append(x, y, m);
            }
            break;

            case 'q': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;
                ctp[2] = ctp[0] + reader.parseFloat();
                ctp[3] = ctp[1] + reader.parseFloat();
                ctp[4] = ctp[0] + reader.parseFloat();
                ctp[5] = ctp[1] + reader.parseFloat();

                pathData.previousX = ctp[4];
                pathData.previousY = ctp[5];

                computeQuadratic(ctp, pointBuffer);
            }
            break;
            case 'Q': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;
                ctp[2] = reader.parseFloat();
                ctp[3] = reader.parseFloat();
                ctp[4] = reader.parseFloat();
                ctp[5] = reader.parseFloat();

                pathData.previousX = ctp[4];
                pathData.previousY = ctp[5];

                computeQuadratic(ctp, pointBuffer);
            }
            break;

            case 't':
                break;
            case 'T':
                break;

            case 'c': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;
                ctp[2] = ctp[0] + reader.parseFloat();
                ctp[3] = ctp[1] + reader.parseFloat();
                ctp[4] = ctp[0] + reader.parseFloat();
                ctp[5] = ctp[1] + reader.parseFloat();
                ctp[6] = ctp[0] + reader.parseFloat();
                ctp[7] = ctp[1] + reader.parseFloat();

                pathData.previousX = ctp[6];
                pathData.previousY = ctp[7];
                pathData.previousCtpX = ctp[4];
                pathData.previousCtpY = ctp[5];

                computeCubic(ctp, pointBuffer);
            }
            break;
            case 'C': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;
                ctp[2] = reader.parseFloat();
                ctp[3] = reader.parseFloat();
                ctp[4] = reader.parseFloat();
                ctp[5] = reader.parseFloat();
                ctp[6] = reader.parseFloat();
                ctp[7] = reader.parseFloat();

                pathData.previousX = ctp[6];
                pathData.previousY = ctp[7];
                pathData.previousCtpX = ctp[4];
                pathData.previousCtpY = ctp[5];

                computeCubic(ctp, pointBuffer);
            }
            break;

            case 's': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;

                if (pathData.previousCurve == 's' || pathData.previousCurve == 'S' || pathData.previousCurve == 'c' || pathData.previousCurve == 'C') {
                    ctp[2] = pathData.previousCtpX;
                    ctp[3] = pathData.previousCtpY;
                } else {
                    ctp[2] = ctp[0];
                    ctp[3] = ctp[1];
                }

                ctp[4] = ctp[0] + reader.parseFloat();
                ctp[5] = ctp[1] + reader.parseFloat();
                ctp[6] = ctp[0] + reader.parseFloat();
                ctp[7] = ctp[1] + reader.parseFloat();

                pathData.previousX = ctp[6];
                pathData.previousY = ctp[7];
                pathData.previousCtpX = ctp[4];
                pathData.previousCtpY = ctp[5];

                computeCubic(ctp, pointBuffer);
            }
            break;

            case 'S': {
                float[] ctp = {.0f, .0f, .0f, .0f, .0f, .0f, .0f, .0f};

                ctp[0] = pathData.previousX;
                ctp[1] = pathData.previousY;

                if (pathData.previousCurve == 's' || pathData.previousCurve == 'S' || pathData.previousCurve == 'c' || pathData.previousCurve == 'C') {
                    ctp[2] = pathData.previousCtpX;
                    ctp[3] = pathData.previousCtpY;
                } else {
                    ctp[2] = ctp[0];
                    ctp[3] = ctp[1];
                }

                ctp[4] = reader.parseFloat();
                ctp[5] = reader.parseFloat();
                ctp[6] = reader.parseFloat();
                ctp[7] = reader.parseFloat();

                pathData.previousX = ctp[6];
                pathData.previousY = ctp[7];
                pathData.previousCtpX = ctp[4];
                pathData.previousCtpY = ctp[5];

                computeCubic(ctp, pointBuffer);
            }
            break;

            case 'a': {
                float x0 = pathData.previousX;
                float y0 = pathData.previousY;

                float rx = reader.parseFloat();
                float ry = reader.parseFloat();
                float angle = reader.parseFloat();
                boolean largeArcFlag = (int) reader.parseFloat() == 1;
                boolean sweepFlag = (int) reader.parseFloat() == 1;
                float x = x0 + reader.parseFloat();
                float y = y0 + reader.parseFloat();

                pathData.previousX = x;
                pathData.previousY = y;

                pointBuffer.append(x, y); //Arcs not implemented
            }
            break;

            case 'A': {
                float x0 = pathData.previousX;
                float y0 = pathData.previousY;

                float rx = reader.parseFloat();
                float ry = reader.parseFloat();
                float angle = reader.parseFloat();
                boolean largeArcFlag = (int) reader.parseFloat() == 1;
                boolean sweepFlag = (int) reader.parseFloat() == 1;
                float x = reader.parseFloat();
                float y = reader.parseFloat();

                pathData.previousX = x;
                pathData.previousY = y;

                pointBuffer.append(x, y); //Arcs not implemented
            }
            break;

            default:
                reader.getNext();
                break;
        }

        return pointBuffer;
    }

    private void computeQuadratic(float[] ctp, PointBuffer ptbuf) {
        final float c = 2.0f / 3.0f;

        ctp[6] = ctp[4];
        ctp[7] = ctp[5];
        ctp[4] = ctp[4] + c * (ctp[2] - ctp[4]);
        ctp[5] = ctp[5] + c * (ctp[3] - ctp[5]);
        ctp[2] = ctp[0] + c * (ctp[2] - ctp[0]);
        ctp[3] = ctp[1] + c * (ctp[3] - ctp[1]);

        computeCubic(ctp, ptbuf);
    }

    private void computeCubic(float[] ctp, PointBuffer ptbuf) {
        _transform.peek().mapPoints(ctp);
        _flattener.convert(false, ctp, ptbuf);
    }

    PointBuffer computePolyline(String points, boolean close, Matrix m) {
        PointBuffer buf = new PointBuffer();

        PathReader reader = new PathReader(points);

        while (reader.hasNext()) {
            char c = reader.peekNext();

            switch (c) {
                case ' ':
                case ',':
                    reader.currentPosition++;
                    break;

                default:
                    buf.append(reader.parseFloat(), reader.parseFloat(), m);
                    break;
            }
        }

        if (close) {
            buf.close();
        }

        return buf;
    }

    private PointBuffer computeEllipse(float rx, float ry, float cntx, float cnty, Matrix m) {
        PointBuffer buf = new PointBuffer();

        int div = (int) Math.max(CIRCLE_ELLIPSE_SEGMENTS_MIN, (rx + rx + ry + ry) * _settings.scale * .25f);

        float da = PI2 / (float) div;
        float a = da * _rnd.nextInt(div);

        for (int i = 0; i < div; i++) {
            float x = (float) (cntx + rx * Math.cos(a));
            float y = (float) (cnty + ry * Math.sin(a));

            buf.append(x, y, m);

            a += da;
        }

        buf.close();

        return buf;
    }

    private void appendElement(PointBuffer data) throws SAXException {
        checkAbort();

        if (data == null || data.pt.size() == 0 || data.len <= Float.MIN_VALUE)
            return;

        ParserInterface p = _thread.get();

        if (p != null) {
            p.append(new Sen(data, _settings));
        } else {
            _abort = true;
            _thread.clear();
            throw new SAXException("Thread expired.");
        }
    }

    private Matrix parseTransform(String mat) {
        int index_first = mat.indexOf(SVGUti.SVG_TRANSFORM_MATRIX_START_BRACKET) + 1;
        int index_last = mat.lastIndexOf(SVGUti.SVG_TRANSFORM_MATRIX_END_BRACKET);

        String[] coeff = mat.substring(index_first, index_last).split(SVGUti.SVG_TRANSFORM_MATRIX_COEFF_REGEX);

        Matrix ret = new Matrix();

        if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_MATRIX)) {
            float[] fcoeff = new float[]{
                    1.0f, .0f, .0f,
                    .0f, 1.0f, .0f,
                    .0f, .0f, 1.0f};

            //ref: http://www.w3.org/TR/SVG/coords.html#TransformMatrixDefined
            SVGUti.SafeParse(coeff, 0, fcoeff, 0); //a
            SVGUti.SafeParse(coeff, 1, fcoeff, 3); //c
            SVGUti.SafeParse(coeff, 2, fcoeff, 1); //e
            SVGUti.SafeParse(coeff, 3, fcoeff, 4); //b
            SVGUti.SafeParse(coeff, 4, fcoeff, 2); //d
            SVGUti.SafeParse(coeff, 5, fcoeff, 5); //f

            ret.setValues(fcoeff);
        } else if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_TRANSLATE)) {
            float tx = SVGUti.SafeParse(coeff, 0);
            float ty = SVGUti.SafeParse(coeff, 1);

            ret.setTranslate(tx, ty);
        } else if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_SCALE)) {
            float sx = SVGUti.SafeParse(coeff, 0);
            float sy = SVGUti.SafeParse(coeff, 1);

            ret.setScale(sx, sy);
        } else if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_ROTATE)) {
            float a = SVGUti.SafeParse(coeff, 0);
            float cx = SVGUti.SafeParse(coeff, 1);
            float cy = SVGUti.SafeParse(coeff, 2);

            ret.setRotate(a, cx, cy);
        } else if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_SKEWX)) {
            float sk = SVGUti.SafeParse(coeff, 0);

            ret.setSkew(sk, .0f);
        } else if (mat.startsWith(SVGUti.SVG_TRANSFORM_MATRIX_TYPE_SKEWY)) {
            float sk = SVGUti.SafeParse(coeff, 0);

            ret.setSkew(.0f, sk);
        }

        return ret;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("g")) {
            _transform.pop();
        }
    }

    public void abort() {
        _abort = true;
    }

    private void checkAbort() throws SAXException {
        if (_abort || Thread.interrupted())
            throw new SAXException("Aborted by the loading thread.");
    }

    private class PathData {
        public float previousX = .0f;
        public float previousY = .0f;

        public float previousCtpX = .0f;
        public float previousCtpY = .0f;

        public float startX = .0f;
        public float startY = .0f;

        public char previousCurve = 0;
        public char nextCurve = 0;
    }

    private class PathReader {
        private String inputString;
        private int currentPosition = 0;
        private int length = 0;

        public PathReader(String string) {
            inputString = string;
            length = inputString.length();

            currentPosition = 0;
        }

        public boolean hasNext() {
            return currentPosition < length;
        }

        public char getNext() {
            return inputString.charAt(currentPosition++);
        }

        public char peekNext() {
            return inputString.charAt(currentPosition);
        }

        private void skipCharacter() {
            currentPosition++;
        }

        private void skipSpaces() {
            while (hasNext()) {
                char c = peekNext();

                switch (c) {
                    case ' ':
                    case ',':
                    case '\n':
                    case '\t':
                    case '\r':
                        break;

                    default:
                        return;
                }

                skipCharacter();
            }
        }

        public float parseFloat() {
            boolean exp = false;

            skipSpaces();

            int j = currentPosition;

            if (inputString.charAt(j) == '-') {
                getNext();
            }

            while (hasNext()) {
                char c = getNext();

                switch (c) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '+':
                    case '.':
                        exp = false;
                        break;

                    case '-':
                        if (exp) {
                            exp = false;
                        } else {
                            currentPosition--;
                            return Uti.safeParseFloat(inputString.substring(j, currentPosition));
                        }
                        break;

                    case 'e':
                    case 'E':
                        exp = true;
                        break;

                    default:
                        currentPosition--;
                        return Uti.safeParseFloat(inputString.substring(j, currentPosition));
                }
            }

            return Uti.safeParseFloat(inputString.substring(j, currentPosition));
        }
    }
}