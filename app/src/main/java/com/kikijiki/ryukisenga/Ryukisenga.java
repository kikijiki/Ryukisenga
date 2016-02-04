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

package com.kikijiki.ryukisenga;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;
import com.kikijiki.kabegamilib.Kabegami;
import com.kikijiki.ryukisenga.drawing.SequenceScheduler;
import com.kikijiki.ryukisenga.drawing.vec;
import com.kikijiki.ryukisenga.preferences.WallPreferences;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Ryukisenga extends Kabegami {
    public Ryukisenga() {
        setFactory(new Factory());
    }

    private static class Touch {
        public Timer timer = new Timer();
        public vec position = new vec();
    }

    private class Factory implements IKabegamiFactory {
        @Override
        public IKabegami newInstance() {
            return new RyukisengaWallpaper();
        }
    }

    public class RyukisengaWallpaper implements IKabegami {
        public static final long SHOOTING_INTERVAL = 100;

        private KabegamiEngine _e;
        private SequenceScheduler _ss;

        private Map<Integer, Touch> _touch;
        private boolean _nextOnTouch;

        @Override
        public boolean init(KabegamiEngine e) {
            _e = e;

            _touch = new ConcurrentHashMap<Integer, Touch>();

            _ss = new SequenceScheduler(Ryukisenga.this, new vec(_e.getScreenWidth(), _e.getScreenHeight()));
            readPreferences();

            CrittercismConfig config = new CrittercismConfig();
            config.setLogcatReportingEnabled(true);
            Crittercism.initialize(getApplicationContext(), "524b7e03d0d8f706ee000002");

            return true;
        }

        private void readPreferences() {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Ryukisenga.this);

            float transition = Uti.safeParseFloat((p.getString(WallPreferences.Keys.SEQUENCE_TRANSITION_LENGTH, "10")));
            _nextOnTouch = transition < -.5f;

            _ss.setTransitionLength(transition);
        }

        @Override
        public void update(long milli, float sec) {
            if (_ss != null) {
                _ss.update(sec);
            }
        }

        @Override
        public void draw(Canvas c, float offset) {
            if (_ss != null) {
                _ss.draw(c, offset);
            }
        }

        @Override
        public void drawLoading(Canvas c, KabegamiEngine e) {
            Paint p = new Paint();
            p.setAlpha(255);
            p.setColorFilter(null);
            p.setColor(Color.WHITE);
            p.setTextSize(20);
            c.drawColor(Color.BLACK);
            c.drawText(Ryukisenga.this.getString(R.string.loading_message), 100, 100, p);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
            _e.reset();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (_ss == null) {
                return;
            }

            int index = event.getActionIndex();
            int id = event.getPointerId(index);
            int count = event.getPointerCount();

            if (!_ss.isActive())
                return;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    if (_nextOnTouch) {
                        if (_ss.stopWaiting()) {
                            return;
                        }
                    }

                    if (id >= count)
                        break;

                    Touch t = new Touch();
                    t.position = new vec(event.getX(index), event.getY(index));
                    t.timer.scheduleAtFixedRate(new TimeredTouchTask(t.position, id), 0, SHOOTING_INTERVAL);

                    Touch old = _touch.put(id, t);

                    if (old != null) {
                        old.timer.cancel();
                        old.timer = null;
                    }
                }
                break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    removeTouch(id);
                }
                break;

                case MotionEvent.ACTION_MOVE: {
                    for (Integer i : _touch.keySet()) {
                        vec t = _touch.get(i).position;

                        t.x = event.getX(index);
                        t.y = event.getY(index);
                    }
                }
                break;
            }
        }

        private void removeTouch(int id) {
            Touch t = _touch.get(id);

            if (t != null) {
                if (t.timer != null) {
                    t.timer.cancel();
                    t.timer = null;
                }

                _touch.remove(id);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }

        @Override
        public boolean waitForDebugger() {
            return false;
        }

        @Override
        public void onDestroy() {
            if (_ss != null)
                _ss.onDestroy();
        }

        public Context getContext() {
            return Ryukisenga.this;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            //Kabegamilib already handles this
        }

        @Override
        public void free() {
            if (_ss != null)
                _ss.onDestroy();

            if (_touch != null)
                _touch.clear();
        }

        private class TimeredTouchTask extends TimerTask {
            private vec _t;
            private int _id;

            public TimeredTouchTask(vec t, int id) {
                _t = t;
                _id = id;
            }

            @Override
            public void run() {
                if (_ss != null && _ss.isActive()) {
                    _ss.getActiveSequence().pushLine(_t, _e.getScreenOffset());
                } else {
                    Touch t = _touch.get(_id);

                    if (t != null)
                        _touch.remove(_id);

                    cancel();
                }
            }
        }
    }
}
