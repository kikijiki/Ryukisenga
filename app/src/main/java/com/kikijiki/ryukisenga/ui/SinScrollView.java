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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class SinScrollView extends HorizontalScrollView {
    private static final int INTERVAL = 20;
    private static final int SPEED = 1;
    private static final int WAIT = 2000;
    private int _wait = WAIT;
    private Timer _t = null;
    private boolean _moving = false;
    private int _dir = 1;
    private int _oldx = -1;

    private boolean _scrollable = true;

    public SinScrollView(Context context) {
        super(context);
        init();

    }

    public SinScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SinScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.setHorizontalScrollBarEnabled(false);
        this.setHorizontalFadingEdgeEnabled(false);
        this.setClickable(false);
        this.setFocusable(false);
    }

    private void startTimer() {
        _t = new Timer();

        _t.scheduleAtFixedRate(new TimerTask() {
                                   private WeakReference<SinScrollView> s = new WeakReference<SinScrollView>(SinScrollView.this);

                                   @Override
                                   public void run() {
                                       if (s == null || s.get() == null) {
                                           this.cancel();
                                           return;
                                       }

                                       if (!_moving) {
                                           _wait -= INTERVAL;

                                           if (_wait <= 0) {
                                               _moving = true;
                                               _wait = 0;
                                           }
                                       } else {
                                           if (_wait > 0) {
                                               _wait -= INTERVAL;
                                               return;
                                           }

                                           SinScrollView.this.post(new Runnable() {
                                               @Override
                                               public void run() {
                                                   scrollBy(SPEED * _dir, 0);

                                                   int x = SinScrollView.this.getScrollX();

                                                   if (x == _oldx) {
                                                       _dir *= -1;
                                                       _wait = WAIT;
                                                   }

                                                   _oldx = x;
                                               }
                                           });
                                       }
                                   }
                               },

                0, INTERVAL);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        freeTimer();
        if (visibility == View.VISIBLE) {
            startTimer();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        freeTimer();
        if (visibility == View.VISIBLE) {
            startTimer();
        }
    }

    private void freeTimer() {
        if (_t != null) {
            _t.cancel();
            _t.purge();
            _t = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (_t != null)
            return;

        View child = this.getChildAt(0);

        if (child == null)
            return;

        int dx = child.getWidth() - getWidth();

        if (dx > 0) {
            startTimer();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (_scrollable) {
            _moving = false;
            _wait = WAIT;
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }

    public void setScrollable(boolean scrollable) {
        _scrollable = scrollable;
    }


}
