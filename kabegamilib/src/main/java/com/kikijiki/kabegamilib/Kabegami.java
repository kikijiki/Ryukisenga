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

package com.kikijiki.kabegamilib;

import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.service.wallpaper.WallpaperService;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class Kabegami extends WallpaperService
{
    public interface IKabegami
    {
        public boolean init(KabegamiEngine e);
        public void free();

        public void update(long milli, float sec);
        public void draw(Canvas c, float offset);
        public void drawLoading(Canvas c, KabegamiEngine e);

        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1);
        public void onTouchEvent(MotionEvent event);
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset);
        public void onVisibilityChanged(boolean visible);

        public void onDestroy();

        public boolean waitForDebugger();
    }

    public interface IKabegamiFactory
    {
        public IKabegami newInstance();
    }

    private IKabegamiFactory _factory;
    public void setFactory(IKabegamiFactory f){_factory = f;}

    @Override
    public Engine onCreateEngine()
    {
        return new KabegamiEngine();
    }

    public class KabegamiEngine extends Engine implements OnSharedPreferenceChangeListener
    {
        private static final long MAX_ELAPSED_TIME = 500;
        private static final int DEBUGGER_TIMEOUT = 5000;
        protected static final long PAUSE_SLEEP_TIME = 200;

        private boolean _isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));

        private SharedPreferences _pref;
        public SharedPreferences getPreferences() {return _pref;}

        private IKabegami _k;

        private boolean _visible = true;
        private boolean _need_init = true;
        private boolean _pause = false;

        private long _time;

        private int _screen_width;
        public int getScreenWidth(){return _screen_width;}

        private int _screen_height;
        public int getScreenHeight(){return _screen_height;}

        private float _screen_offset;
        public float getScreenOffset()
        {
            return isPreview() ? .0f :_screen_offset;
        }

        private int _fps = 30;
        private int _next_frame = 1000 / _fps;
        public void setFps(int FPS)
        {
            _fps = FPS;
            _next_frame = 1000 / _fps;
        }

        private Canvas _c = null;
        private Paint _log_paint = new Paint();
        private int _log_color = Color.CYAN;
        private float _log_size = 20.0f;

        private final Handler _handler = new Handler();
        private final Runnable _drawRunner = new Runnable()
        {
            @Override
            public void run()
            {
                if(_need_init)
                {
                    _k.free();

                    Canvas c = null;
                    SurfaceHolder holder = getSurfaceHolder();

                    c = holder.lockCanvas();

                    if(c != null)
                    {
                        _k.drawLoading(c, KabegamiEngine.this);
                        holder.unlockCanvasAndPost(c);
                        c = null;
                    }

                    _need_init = !_k.init(KabegamiEngine.this);
                    _handler.removeCallbacks(_drawRunner);
                    _handler.postDelayed(_drawRunner, 0);
                    return;
                }

                if(_pause)
                {
                    _handler.removeCallbacks(_drawRunner);
                    _time = System.currentTimeMillis();
                    _handler.postDelayed(_drawRunner, PAUSE_SLEEP_TIME);
                    return;
                }

                if (_visible)
                {
                    long now = System.currentTimeMillis();
                    long milli = Math.min(MAX_ELAPSED_TIME, (now - _time));
                    float ela = (float)milli * .001f;
                    _time = now;
                    _k.update(milli, ela);

                    SurfaceHolder holder = null;
                    boolean locked = false;

                    try
                    {
                        holder = getSurfaceHolder();
                        _c = holder.lockCanvas();
                        locked = true;

                        if(_c != null)
                        {
                            _k.draw(_c, getScreenOffset());
//							holder.unlockCanvasAndPost(_c);
//							_c = null;
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        if(locked && holder != null && _c != null)
                        {
                            holder.unlockCanvasAndPost(_c);
                            _c = null;
                        }
                        else
                        {
                            try
                            {
                                Thread.sleep(500);
                            } catch (InterruptedException e){e.printStackTrace();}
                        }
                    }

                    _handler.removeCallbacks(_drawRunner);

                    long frameTime = System.currentTimeMillis() - now;
                    int next = (int) Math.max(0, _next_frame - frameTime);
                    _handler.postDelayed(_drawRunner, next);
                }
            }
        };

        private class WaitDebuggerTask extends AsyncTask<Void, Integer, Boolean>
        {
            @Override
            protected Boolean doInBackground(Void... arg0)
            {
                android.os.Debug.waitForDebugger();
                return true;
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                super.onProgressUpdate(values);
            }
        }

        public KabegamiEngine()
        {
            //System.gc();
            _need_init = true;

            _pref = PreferenceManager.getDefaultSharedPreferences(Kabegami.this);
            _pref.registerOnSharedPreferenceChangeListener(this);

            _k = _factory.newInstance();

            if(_isDebuggable)
            {
                WaitDebuggerTask waitDebugger = new WaitDebuggerTask();
                waitDebugger.execute();

                if(_k.waitForDebugger())
                {
                    try
                    {
                        waitDebugger.get(DEBUGGER_TIMEOUT, TimeUnit.MILLISECONDS);
                    }
                    catch (Exception e)
                    {
                        //e.printStackTrace();
                    }
                }
            }

            _handler.post(_drawRunner);
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            exit();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder)
        {
            super.onSurfaceDestroyed(holder);

            _visible = false;
            _k.onVisibilityChanged(false);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            _screen_width = width;
            _screen_height = height;

            _need_init = true;

            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            _visible = visible;

            if (_visible)
            {
                _time = System.currentTimeMillis();
                _handler.post(_drawRunner);
            }

            if(_k != null)
                _k.onVisibilityChanged(_visible);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
        {
            _screen_offset = xOffset - .5f;

            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }

        @Override
        public void onTouchEvent(MotionEvent event)
        {
            _k.onTouchEvent(event);
            super.onTouchEvent(event);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            _k.onSharedPreferenceChanged(arg0, arg1);
        }

        public void setupLog(int color, float size)
        {
            _log_color = color;
            _log_size = size;
        }

        public void cLog(String text, float x, float y)
        {
            if(_c != null)
            {
                _c.save();
                _c.setMatrix(null);
                _log_paint.setColor(_log_color);
                _log_paint.setTextSize(_log_size);
                _c.drawText(text, x, y, _log_paint);
                _c.restore();
            }
        }

        private void exit()
        {
            _k.onDestroy();

            _handler.removeCallbacks(_drawRunner);
            //Kabegami.this.stopSelf();
        }

        public void pause(boolean pause)
        {
            //TODO untested
            _pause = pause;
        }

        public void reset()
        {
            _need_init = true;
        }

        boolean isDebugMode()
        {
            return 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
        }
    }
}
