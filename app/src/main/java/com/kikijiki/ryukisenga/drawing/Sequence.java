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

package com.kikijiki.ryukisenga.drawing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.kikijiki.ryukisenga.Uti;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.content.CubicBezierConverter;
import com.kikijiki.ryukisenga.content.CubicBezierFlattener;
import com.kikijiki.ryukisenga.content.ParserInterface;
import com.kikijiki.ryukisenga.content.SVGUti;
import com.kikijiki.ryukisenga.content.SequenceContentHandler;
import com.kikijiki.ryukisenga.content.SvgContentHandler;
import com.kikijiki.ryukisenga.content.SvgInfoContentHandler;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.preferences.WallPreferences;
import com.kikijiki.ryukisenga.styles.StyleManager;
import com.kikijiki.ryukisenga.styles.data.Style;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sequence {
    private final static int GROUP_SIZE_LIMIT = 64;

    private BlockingQueue<Sen> _queue;
    private ConcurrentLinkedQueue<Sen> _sen_active = new ConcurrentLinkedQueue<Sen>();
    private List<Sen> _sen_bbQueue = new ArrayList<Sen>();
    private List<Sen> _sen_ended = new ArrayList<Sen>();

    private float _scroll = .0f;
    private float _scroll_factor = .5f;
    private vec _scroll_offset = new vec();

    private Matrix[] _transform = new Matrix[]
            {
                    new Matrix(), //[0] Rendering of the lines to the accumulation texture
                    //Transform: scaling
                    new Matrix(), //[1] Rendering of the active lines to the backbuffer
                    //Transform: scaling + translation(cntx + scroll, status_bar + cnty)
                    new Matrix(), //[2] Rendering of the accumulation texture to the backbuffer
                    //Transform: translation(cntx + scroll, status_bar)
            };

    private boolean _use_backbuffer = true;

    private SequenceStatus _status = SequenceStatus.buffering;

    private DisplayMode _display_mode = DisplayMode.fitHeight;
    private ScrollMode _scroll_mode = ScrollMode.enabled;

    private float _group_length = .0f;

    private Bitmap _buffer;
    private Canvas _bbc;

    private boolean _parsing_complete = false;

    private Settings _settings = new Settings();

    private Sequence(Context c, PlaylistEntry entry, BlockingQueue<Sen> queue, vec scrSize, boolean useBackbuffer) {
        _settings.viewport = scrSize.clone();
        _settings.viewport.y -= _settings.verticalOffset;

        _queue = queue;

        loadPreferences(c);
        parseSVGAsset(c, entry, null);

        if (_use_backbuffer = useBackbuffer) {
            try {
                int bmp_width;
                int bmp_height;

                bmp_width = (int) (_settings.sequenceSize.x * _settings.scale);
                bmp_height = (int) (_settings.sequenceSize.y * _settings.scale);
                //bmp_height = (int)(_settings.viewport.y);

                if (bmp_width <= 0 || bmp_height <= 0) {
                    setStatus(SequenceStatus.inactive);
                    return;
                }

                _buffer = Bitmap.createBitmap(bmp_width, bmp_height, Bitmap.Config.ARGB_8888);
                _bbc = new Canvas(_buffer);
                _bbc.drawColor(Color.TRANSPARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (entry.style != null) {
            setStyle(StyleManager.LoadStyle(c, entry.style, scrSize));
        }
    }

    /*
     * To use with the preview
     */
    private Sequence(Context c, PlaylistEntry entry, vec scrSize, Style style) {
        _settings.verticalOffset = 0;

        _settings.viewport = scrSize.clone();

        _display_mode = DisplayMode.fit;
        _scroll_mode = ScrollMode.disabled;
        _scroll_factor = .0f;
        _group_length = .0f;

        computeStaticData();

        parseSVGAsset(c, entry, style);

        _use_backbuffer = false;
    }

    public static Sequence makeSequence(Context c, PlaylistEntry entry, BlockingQueue<Sen> queue, vec scrSize, boolean useBackbuffer) {
        return new Sequence(c, entry, queue, scrSize, useBackbuffer);
    }

    public static Sequence makePreviewSequence(Context c, PlaylistEntry entry, vec scrSize, Style style) {
        return new Sequence(c, entry, scrSize, style);
    }

    //Intersection between segments
    static boolean Intersects(vec a1, vec a2, vec b1, vec b2, vec intersection) {
        vec b = a2.sub(a1);
        vec d = b2.sub(b1);
        float bDotDPerp = b.x * d.y - b.y * d.x;

        if (!(bDotDPerp > Float.MIN_VALUE))
            return false;

        vec c = b1.sub(a1);

        float t = (c.x * d.y - c.y * d.x) / bDotDPerp;
        if (t < 0 || t > 1)
            return false;

        float u = (c.x * b.y - c.y * b.x) / bDotDPerp;
        if (u < 0 || u > 1)
            return false;

        intersection.x = a1.x + t * b.x;
        intersection.y = a1.y + t * b.y;

        return true;
    }

    public void loadPreferences(Context c) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);

        _display_mode = DisplayMode.valueOf(p.getString(WallPreferences.Keys.SEQUENCE_DISPLAY_MODE, DisplayMode.fitHeight.toString()));
        _scroll_mode = ScrollMode.valueOf(p.getString(WallPreferences.Keys.SEQUENCE_SCROLL_MODE, ScrollMode.enabled.toString()));
        _group_length = Uti.safeParseFloat(p.getString(WallPreferences.Keys.SEQUENCE_GROUP_LENGTH, "500"), 500.0f);

        _settings.lineSpeed = Uti.safeParseFloat(p.getString(WallPreferences.Keys.SEQUENCE_LINE_SPEED, "200"), 200.0f);
        _settings.trailSpeed = Uti.safeParseFloat(p.getString(WallPreferences.Keys.SEQUENCE_TRAIL_SPEED, "300"), 300.0f);
        _settings.useTrail = p.getString(WallPreferences.Keys.SEQUENCE_USE_TRAIL, "use_trail").equalsIgnoreCase("use_trail");
    }

    public void update(float sec) {
        switch (_status) {
            case buffering:
                if (_queue.size() >= SequenceScheduler.BUFFER_QUEUE_SIZE || _parsing_complete) {
                    setStatus(SequenceStatus.active);
                }
                break;
            case active:
                updateMainSequence(sec);
                break;
            case inactive:
                break;
        }
    }

    private void updateMainSequence(float sec) {
        if (_queue.size() == 0 && _sen_active.size() == 0 && _parsing_complete) {
            setStatus(SequenceStatus.inactive);
            return;
        }

        Iterator<Sen> it = _sen_active.iterator();

        while (it.hasNext()) {
            Sen s = it.next();

            s.update(sec);

            if (!s.isActive()) {
                it.remove();
                _sen_ended.add(s);
            }
        }

        updateLineLists();
    }

    public void draw(Canvas c, float offset) {
        //round to avoid discrepancy between active line pixels
        //and accumulation texture pixel coordinates.
        int dx = (int) (-offset * _scroll + _scroll_offset.x);
        int dy = (int) (_settings.verticalOffset + _scroll_offset.y);

        _transform[1].setScale(_settings.scale, _settings.scale);
        _transform[1].postTranslate(dx, dy);

        _transform[2].setTranslate(dx, dy);

        drawBackground(c, dx, dy);

        if (_use_backbuffer) {
            processBackbufferQueue();
            drawBackBuffer(c);
        } else {
            drawInactiveSequence(c);
        }

        drawMainSequence(c);
    }

    private void drawBackground(Canvas c, float dx, float dy) {
        c.setMatrix(_transform[2]);

        c.drawRect(-dx, -dy, _settings.viewport.x - dx, -dy + _settings.viewport.y, _settings.style.background);
    }

    private void drawInactiveSequence(Canvas c) {
        c.setMatrix(_transform[1]);

        for (Sen s : _sen_ended) {
            s.draw(c);
        }
    }

    private void processBackbufferQueue() {
        _bbc.setMatrix(_transform[0]);

        for (Sen s : _sen_bbQueue) {
            s.drawImmediate(_bbc);
        }

        _sen_bbQueue.clear();
    }

    private void drawBackBuffer(Canvas c) {
        c.setMatrix(_transform[2]);
        c.drawBitmap(_buffer, 0, 0, null);
    }

    private void drawMainSequence(Canvas c) {
        c.setMatrix(_transform[1]);

        for (Sen s : _sen_active) {
            s.draw(c);
        }
    }

    public void free() {
        if (_buffer != null) {
            _buffer.recycle();
            _buffer = null;
        }
    }

    public void parseSVGInfo(InputStream is) {
        try {
            SVGUti.parseIgnoreNamespace(is, new SvgInfoContentHandler(_settings.sequenceSize, _settings.viewbox));
        } catch (Exception e) {
            //e.printStackTrace();
        }

        setupView();
    }

    public void setGroupLength(float value) {
        _group_length = value;
    }

    public void setStyle(Style style) {
        _settings.style = style;
        _settings.style.adjustScaling(_settings.invScale);
    }

    private void setupView() {
        switch (_display_mode) {
            case fit: {
                vec size = _settings.sequenceSize;

                float rx = _settings.viewport.x / size.x;
                float ry = _settings.viewport.y / size.y;

                _settings.scale = Math.min(rx, ry);

                _transform[0].setScale(_settings.scale, _settings.scale);

                _scroll_offset.x = (_settings.viewport.x - (size.x * _settings.scale)) * .5f;
                _scroll_offset.y = (_settings.viewport.y - (size.y * _settings.scale)) * .5f;

                _scroll = 0;
            }
            break;
            case fitHeight: {
                _settings.scale = _settings.viewport.y / _settings.sequenceSize.y;

                _transform[0].setScale(_settings.scale, _settings.scale);

                _scroll_offset.x = (_settings.viewport.x - _settings.sequenceSize.x * _settings.scale) * .5f;

                if (_scroll_mode == ScrollMode.disabled) {
                    _scroll = 0;
                } else {
                    if (_scroll_mode == ScrollMode.forced) {
                        _scroll = _settings.sequenceSize.x * _settings.scale * _scroll_factor;
                    } else if (_settings.viewport.x < _settings.sequenceSize.x * _settings.scale) {
                        _scroll = Math.abs(_scroll_offset.x) * 2.0f;
                    }
                }
            }
            break;

            case fitWidth: {
                float w = _settings.viewport.x * (1.0f + _scroll_factor);
                _settings.scale = w / _settings.sequenceSize.x;

                _transform[0].setScale(_settings.scale, _settings.scale);

                _scroll_offset.x = _settings.viewport.x * _scroll_factor * -.5f;
                _scroll_offset.y = (_settings.viewport.y - (_settings.sequenceSize.y * _settings.scale)) * .5f;

                if (_scroll_mode == ScrollMode.disabled) {
                    _scroll = 0;
                } else {
                    if (_scroll_mode == ScrollMode.forced) {
                        _scroll = _settings.sequenceSize.x * _settings.scale * _scroll_factor;
                    } else if (_settings.viewport.x < _settings.sequenceSize.x * _settings.scale) {
                        _scroll = Math.abs(_scroll_offset.x) * 2.0f;
                    }
                }
            }
            break;

            case noResize: {
                _settings.scale = 1.0f;

                _transform[0].reset();

                _scroll_offset.x = (_settings.viewport.x - _settings.sequenceSize.x) / 2;

                if (_scroll_mode == ScrollMode.disabled) {
                    _scroll = 0;
                } else {
                    if (_scroll_mode == ScrollMode.forced) {
                        _scroll = _settings.sequenceSize.x * _scroll_factor;
                    } else if (_settings.viewport.x < _settings.sequenceSize.x * _settings.scale) {
                        _scroll = Math.abs(_scroll_offset.x) * 2.0f;
                    }
                }
            }
            break;
        }

        _settings.invScale = 1.0f / _settings.scale;

        if (_settings.style != null) {
            _settings.style.adjustScaling(_settings.invScale);
        }

        _settings.lineSpeed *= _settings.invScale;
        _settings.trailSpeed *= _settings.invScale;
        _settings.flattener = new CubicBezierConverter(_settings.invScale);
        _group_length *= _settings.invScale;
    }

    private void parseSVGAsset(Context c, PlaylistEntry entry, Style style) {
        String[] data = new String[]{null};

        try {
            if (entry.location == PlaylistEntry.Location.apk) {
                InputStream is = Assets.openVectorInfo(c, entry);
                SequenceContentHandler sch = new SequenceContentHandler(data);
                Xml.parse(is, Encoding.UTF_8, sch);
                is.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            InputStream is = Assets.openVectorData(c, entry);
            parseSVGInfo(is);
            is.close();

            computeStaticData();

            if (style == null) {

                style = StyleManager.LoadStyle(c, data[0], _settings.viewport);
            }

            setStyle(style);

            if (_queue == null) {
                parseImmediate(c, entry);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void parseImmediate(Context c, PlaylistEntry entry) {
        InputStream is = Assets.openVectorData(c, entry);

        _queue = new LinkedBlockingQueue<Sen>();

        try {
            SVGUti.parseIgnoreNamespace(is, new SvgContentHandler(new ParserInterface() {
                @Override
                public void append(Sen sen) {
                    _queue.add(sen);
                }
            }, _settings));
        } catch (Exception e) {
            //e.printStackTrace();
            setStatus(SequenceStatus.inactive);
        }

        _parsing_complete = true;
    }

    private void updateLineLists() {
        if (_use_backbuffer) {
            _sen_bbQueue.addAll(_sen_ended);
            _sen_ended.clear();
        }

        if (_sen_active.size() == 0) {
            nextLine();
        }
    }

    private void nextLine() {
        float l = .0f;
        int size = 0;

        while (true) {
            Sen s = _queue.poll();

            if (s == null)
                return;

            l += s.getLength();
            s.start();
            _sen_active.add(s);
            size++;

            if (l > _group_length || size + _sen_active.size() > GROUP_SIZE_LIMIT) {
                break;
            }
        }
    }

    public void pushLine(final vec touch, final float offset) {
        if (_status != SequenceStatus.active)
            return;

        //if(_sen_active.size() < GROUP_SIZE_LIMIT)
        {
            Sen s = _queue.poll();

            if (s == null)
                return;

            //AsyncTask<Sen, Void, Sen> task = new AsyncTask<Sen, Void, Sen>()
            //{
            //	@Override
            //	protected Sen doInBackground(Sen... params)
            //	{
            //		Sen s = params[0];
            if (_settings.useTrail) {
                s.computeTrail((touch.x + offset * _scroll - _scroll_offset.x) * _settings.invScale, (touch.y - _scroll_offset.y) * _settings.invScale);
            }
            //		return s;
            //	}

            //	@Override
            //	protected void onPostExecute(Sen result)
            //	{
            //		Sen s = result;
            _sen_active.add(s);
            s.start();
            //	}
            //};

            //task.execute(s);
        }
    }

    public void parsingComplete() {
        _parsing_complete = true;
    }

    public boolean isActive() {
        return _status == SequenceStatus.active;
    }

    public boolean isBuffering() {
        return _status == SequenceStatus.buffering;
    }

    public String toString() {
        return "seq status:" + _status.toString();
    }

    public Matrix getTransform() {
        return this._transform[1];
    }

    private Matrix getInverseTransform(float offset) {
        float dx = -offset * _scroll + _scroll_offset.x;

        Matrix tr = new Matrix();
        tr.setScale(_settings.scale, _settings.scale);
        tr.postTranslate(dx, _settings.verticalOffset + _scroll_offset.y);

        Matrix ret = new Matrix();
        tr.invert(ret);

        return ret;
    }

    private void computeStaticData() {
        Matrix tl = getInverseTransform(-0.5f);
        Matrix br = getInverseTransform(0.5f);

        _settings.untransformedTopLeftCorner.transform(tl);

        _settings.untransformedBottomRightCorner = _settings.viewport.clone();
        _settings.untransformedBottomRightCorner.transform(br);
    }

    public void setDisplayMode(DisplayMode mode) {
        _display_mode = mode;
        setupView();
        computeStaticData();
    }

    public Settings getSettings() {
        return _settings;
    }

    public BlockingQueue<Sen> getQueue() {
        return _queue;
    }

    private void setStatus(SequenceStatus status) {
        _status = status;
    }

    public void setAlpha(int alpha) {
        _settings.style.line.setAlpha(alpha);
        _settings.style.trail.setAlpha(alpha);
        _settings.style.background.setAlpha(alpha);
    }

    public enum SequenceStatus {
        buffering,
        active,
        inactive,
    }

    private enum DisplayMode {
        fit,
        fitWidth,
        fitHeight,
        noResize
    }

    private enum ScrollMode {
        disabled,
        enabled,
        forced,
    }

    public class Settings {
        public boolean useTrail = true;
        public vec sequenceSize = new vec();
        public vec viewbox = new vec();
        public float invScale = 1.0f;
        public vec untransformedTopLeftCorner = new vec();
        public vec untransformedBottomRightCorner = new vec();
        public float trailSpeed = 200.0f;
        public float lineSpeed = 100.0f;
        public Style style = new Style();
        public float scale = 1.0f;
        public vec viewport = new vec();
        public float verticalOffset = .0f;
        public CubicBezierFlattener flattener;
    }
}