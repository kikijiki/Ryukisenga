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

package com.kikijiki.ryukisenga.drawing;

import android.graphics.Canvas;
import android.graphics.Path;

import com.kikijiki.ryukisenga.content.PointBuffer;
import com.kikijiki.ryukisenga.drawing.Sequence.Settings;

import java.util.Random;

public class Sen {
    private static final float TRAIL_MARGIN = 20.0f;
    private static final float TRAIL_TANGENT_COEFF = 200.0f;
    private static final float TRAIL_EXIT_SPEED_COEFF = .25f;
    private static final float TRAIL_EXIT_SPEED_PUSH = 100.0f;

    private static final float PI2 = (float) (Math.PI * 2.0D);

    private float _t = .0f;
    private LineStatus _status = LineStatus.inactive;

    private PointBuffer _data;
    private int _pt_prog;
    private int _dl_prog;

    private Path _path = new Path();

    private PointBuffer _trail;
    private float[] _trail_buffer;

    private float[] _trail_progress = {.0f, .0f};
    private float _len = .0f;

    private Settings _settings;

    public Sen(PointBuffer data, Settings settings) {
        _settings = settings;
        _data = data;

        _path.incReserve(_data.pt.size() / 2);

        _t = .0f;

        _trail_progress[0] = .0f;
        _trail_progress[1] = .0f;

        _path.moveTo(_data.pt.get(0), _data.pt.get(1));
        _pt_prog = 2;
        _status = LineStatus.inactive;

        if (_settings.useTrail) {
            computeTrail();
        }
    }

    public void computeTrail() {
        float[] ctp = new float[8];

        ctp[6] = _data.pt.get(0);
        ctp[7] = _data.pt.get(1);

        Random r = new Random();

        float inv_len = 1.0f / _data.dl.get(0);
        float mag = r.nextFloat() * TRAIL_TANGENT_COEFF;

        float dx = Math.min(1.0f, (_data.pt.get(2) - _data.pt.get(0)) * inv_len);
        float dy = Math.min(1.0f, (_data.pt.get(3) - _data.pt.get(1)) * inv_len);

        if (Float.isNaN(dx) || Float.isInfinite(dx))
            dx = .5f;

        if (Float.isNaN(dy) || Float.isInfinite(dy))
            dy = .5f;

        ctp[4] = ctp[6] - dx * mag;
        ctp[5] = ctp[7] - dy * mag;

        int side = r.nextInt(4);

        vec topleft = _settings.untransformedTopLeftCorner;
        vec bottomright = _settings.untransformedBottomRightCorner;

        switch (side) {
            //Up
            case 0:
                ctp[0] = lerp(r.nextFloat(), topleft.x, bottomright.x);
                ctp[1] = topleft.y - TRAIL_MARGIN;
                break;
            //Down
            case 1:
                ctp[0] = lerp(r.nextFloat(), topleft.x, bottomright.x);
                ctp[1] = bottomright.y + TRAIL_MARGIN;
                break;
            //Left
            case 2:
                ctp[0] = topleft.x - TRAIL_MARGIN;
                ctp[1] = lerp(r.nextFloat(), topleft.y, bottomright.y);
                break;
            //Right
            case 3:
                ctp[0] = bottomright.x + TRAIL_MARGIN;
                ctp[1] = lerp(r.nextFloat(), topleft.y, bottomright.y);
                break;
        }

        float a = r.nextFloat() * PI2;

        float size = _settings.sequenceSize.x + _settings.sequenceSize.y;

        ctp[2] = ctp[0] + size * TRAIL_EXIT_SPEED_COEFF * (float) Math.cos(a);
        ctp[3] = ctp[1] + size * TRAIL_EXIT_SPEED_COEFF * (float) Math.sin(a);

        computeTrailCubic(ctp);
    }

    public void computeTrail(float x, float y) {
        float[] ctp = new float[8];

        ctp[6] = _data.pt.get(0);
        ctp[7] = _data.pt.get(1);

        Random r = new Random();

        float inv_len = 1.0f / _data.dl.get(0);
        float mag = r.nextFloat() * TRAIL_TANGENT_COEFF;

        float dx = Math.min(1.0f, (_data.pt.get(2) - _data.pt.get(0)) * inv_len);
        float dy = Math.min(1.0f, (_data.pt.get(3) - _data.pt.get(1)) * inv_len);

        if (Float.isNaN(dx) || Float.isInfinite(dx))
            dx = .5f;

        if (Float.isNaN(dy) || Float.isInfinite(dy))
            dy = .5f;

        ctp[4] = ctp[6] - dx * mag;
        ctp[5] = ctp[7] - dy * mag;

        float a = r.nextFloat() * PI2;

        ctp[2] = ctp[0] + TRAIL_EXIT_SPEED_PUSH * (float) Math.cos(a);
        ctp[3] = ctp[1] + TRAIL_EXIT_SPEED_PUSH * (float) Math.sin(a);

        ctp[0] = x;
        ctp[1] = y;

        computeTrailCubic(ctp);
    }

    private void computeTrailCubic(float[] ctp) {
        _trail = new PointBuffer();
        _settings.flattener.convert(true, ctp, _trail);

        _trail_buffer = new float[4 + (_trail.pt.size() - 4) * 2];

        _trail_buffer[0] = _trail.pt.get(0);
        _trail_buffer[1] = _trail.pt.get(1);

        int tid = 2;
        int i;
        for (i = 2; i < _trail.pt.size() - 2; i += 2) {
            float x = _trail.pt.get(i);
            float y = _trail.pt.get(i + 1);

            _trail_buffer[tid++] = x;
            _trail_buffer[tid++] = y;
            _trail_buffer[tid++] = x;
            _trail_buffer[tid++] = y;
        }

        _trail_buffer[tid++] = _trail.pt.get(i++);
        _trail_buffer[tid++] = _trail.pt.get(i++);
    }

    private float lerp(float a, float x1, float x2) {
        return x1 + a * (x2 - x1);
    }

    public void update(float sec) {
        switch (_status) {
            case trail_entering:
                updateTrail(sec);
                break;
            case trail_finishing:
                updateTrail(sec);
                updateMainPath(sec);
                break;
            case trail_translating:
                updateTrail(sec);
                break;
            case continuing:
                updateTrail(sec);
                updateMainPath(sec);
                break;
            case no_trail:
                updateMainPath(sec);
                break;
            case inactive:
                break;
            case line_ended:
                endLine();
                break;
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void updateTrail(float sec) {
        float trailSpeed = _settings.trailSpeed;
        float trailProgress = trailSpeed * sec;
        float lineResidue = _data.len - _len - _t;

        switch (_status) {
            case trail_entering: {
                _trail_progress[1] += trailProgress;

                //The trail reached the starting point, from now stay still (as if flowing in).
                if (_trail_progress[1] >= _trail.len) {
                    _status = LineStatus.continuing;
                    _trail_progress[1] = _trail.len;
                    return;
                }

                //The line is shorter than the trail: start translating the trail.
                if (_trail_progress[1] > _data.len) {
                    _status = LineStatus.trail_translating;
                    _trail_progress[0] = .0f;
                    _trail_progress[1] = _data.len;
                    return;
                }

            }
            break;

            case continuing: {
                //The remaining portion of path is the same size as the trail: from now on, the trail will start shortening.
                if (lineResidue < _trail.len) {
                    _status = LineStatus.trail_finishing;
                    _trail_progress[0] = _trail.len - lineResidue;
                    _trail_progress[1] = _trail.len;
                    return;
                }
            }
            break;

            case trail_translating: {
                _trail_progress[0] += trailProgress;
                _trail_progress[1] += trailProgress;

                if (_trail_progress[1] > _trail.len) {
                    _status = LineStatus.trail_finishing;
                    _trail_progress[1] = _trail.len;
                    _trail_progress[0] = _trail.len - lineResidue;
                    return;
                }
            }
            break;

            case trail_finishing: {
                _trail_progress[0] = _trail.len - lineResidue;
            }
            break;
        }
    }

    public void updateMainPath(float sec) {
        _t += _settings.lineSpeed * sec;

        while (true) {
            float len = _data.dl.get(_dl_prog);

            if (_t > len) {
                int i = _pt_prog;
                _path.lineTo(_data.pt.get(i), _data.pt.get(i + 1));

                _len += len;
                _t -= len;

                _dl_prog++;
                _pt_prog += 2;

                if (_dl_prog >= _data.dl.size()) {
                    _dl_prog--;
                    _pt_prog -= 2;

                    stop();
                    endLine();
                    return;
                }
            } else {
                break;
            }
        }
    }

    public void draw(Canvas c) {
        //c.save();

        switch (_status) {
            case trail_entering:
                drawTrail(c, -2.0f, _trail_progress[1]);
                break;
            case continuing:
                drawLine(c);
                drawTrail(c, -2.0f, -2.0f);
                break;
            case trail_translating:
                drawTrail(c, _trail_progress[0], _trail_progress[1]);
                break;
            case trail_finishing:
                drawLine(c);
                drawTrail(c, _trail_progress[0], -2.0f);
                break;
            case inactive:
            case line_ended:
                drawLine(c);
                break;
            case no_trail:
                drawLine(c);
                break;
        }

        //c.restore();
    }

    private void drawLine(Canvas c) {
        c.drawPath(_path, _settings.style.line);

        float startX = _data.pt.get(_pt_prog - 2);
        float startY = _data.pt.get(_pt_prog - 1);
        float stopX = _data.pt.get(_pt_prog);
        float stopY = _data.pt.get(_pt_prog + 1);

        float a = _t / _data.dl.get(_dl_prog);
        a = Float.isNaN(a) ? 1.0f : a;

        c.drawLine(startX, startY, lerp(a, startX, stopX), lerp(a, startY, stopY), _settings.style.line);
    }

    private void drawTrail(Canvas c, float fromLength, float toLength) {
        int start = 0;
        int end = 0;

        float dl = 0;

        int j = 0;

        if (fromLength > -1.0f && fromLength > Float.MIN_VALUE) {
            for (int i = 0; i < _trail.dl.size(); i++) {
                dl = _trail.dl.get(i);

                if (fromLength > dl) {
                    fromLength -= dl;
                    toLength -= dl;

                    start += 2;
                    end += 2;
                    j++;
                } else {
                    break;
                }
            }

            float x0 = _trail.pt.get(start);
            float y0 = _trail.pt.get(start + 1);
            float x1 = _trail.pt.get(start + 2);
            float y1 = _trail.pt.get(start + 3);

            float a1 = fromLength / dl;
            float fromX = lerp(a1, x0, x1);
            float fromY = lerp(a1, y0, y1);

            if (toLength < dl && toLength > .0f) {
                float a2 = toLength / dl;
                float toX = lerp(a2, x0, x1);
                float toY = lerp(a2, y0, y1);

                c.drawLine(fromX, fromY, toX, toY, _settings.style.trail);
                start += 2;

                return;
            } else {
                c.drawLine(fromX, fromY, x1, y1, _settings.style.trail);

                toLength -= dl;
                j++;
                start += 2;
                end += 2;
            }
        }

        if (toLength > -1.0f) {
            for (; j < _trail.dl.size(); j++) {
                dl = _trail.dl.get(j);

                if (dl < toLength) {
                    toLength -= dl;
                    end += 2;
                } else {
                    break;
                }
            }

            float a = toLength / dl;

            float fromX = _trail.pt.get(end);
            float fromY = _trail.pt.get(end + 1);
            float toX = lerp(a, fromX, _trail.pt.get(end + 2));
            float toY = lerp(a, fromY, _trail.pt.get(end + 3));

            c.drawLine(fromX, fromY, toX, toY, _settings.style.trail);
        } else {
            end = _trail.pt.size() - 2;
        }

        int count = (end - start) * 2;
        c.drawLines(_trail_buffer, start * 2, count, _settings.style.trail);
    }

    public void drawImmediate(Canvas c) {
        c.save();
        c.drawPath(_path, _settings.style.line);
        c.restore();
    }

    public void start() {
        if (_status != LineStatus.line_ended) {
            _status = _settings.useTrail ? LineStatus.trail_entering : LineStatus.no_trail;
        }
    }

    public void stop() {
        _status = LineStatus.inactive;
    }

    public boolean isActive() {
        return _status != LineStatus.line_ended;
    }

    public float getLength() {
        return _data.len;
    }

    public void endLine() {
        _status = LineStatus.line_ended;
    }

    private enum LineStatus {
        trail_entering,
        trail_finishing,
        trail_translating,
        continuing,
        line_ended,
        inactive,
        no_trail,
    }
}