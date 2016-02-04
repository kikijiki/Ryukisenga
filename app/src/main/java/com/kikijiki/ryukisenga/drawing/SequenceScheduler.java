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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.TypedValue;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.content.ParserInterface;
import com.kikijiki.ryukisenga.content.SVGUti;
import com.kikijiki.ryukisenga.content.SvgContentHandler;
import com.kikijiki.ryukisenga.playlist.PlaylistManager;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.ui.FFPopup;
import com.kikijiki.ryukisenga.ui.FFProgressBar;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SequenceScheduler {
    final static int MAX_QUEUE_SIZE = 64 * 100;
    final static int BUFFER_QUEUE_SIZE = 32 * 100;
    private static final float FADE_TIME = 1.0f;
    private static int _thread_count = 0;
    private Context _c;
    private Sequence _active = null;
    private vec _viewport;
    private Iterator<PlaylistEntry> _playlist_iterator;
    private Thread _worker;
    private FFProgressBar _pb;
    private FFPopup _popup;
    private FFPopup _popup2;
    private float _wait_time = .0f;
    private float _wait = .0f;
    private SchedulerStatus _status = SchedulerStatus.sequence;
    private Bitmap _transition = null;
    private float _offset;
    private PlaylistEntry _prev;

    public SequenceScheduler(Context c, vec viewport) {
        _c = c;
        _viewport = viewport;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, c.getResources().getDisplayMetrics());
        _pb = new FFProgressBar(_viewport.x * .25f, _viewport.y * .5f - px * .5f, _viewport.x * .5f, px);
        _popup = new FFPopup(c, R.string.playlist_empty, 24, 80, _viewport);
        _popup2 = new FFPopup(c, R.string.playlist_empty2, 24, 80, _viewport);
    }

    private void updatePlaylist() {
        List<PlaylistEntry> pl = PlaylistManager.loadPlaylist(_c, true);

        if (pl.isEmpty()) {
            _status = SchedulerStatus.empty;
        } else {
            if (pl.size() > 1) {
                while (pl.get(0).equals(_prev)) {
                    Collections.shuffle(pl);
                }
            }

            _playlist_iterator = pl.iterator();
        }
    }

    public void nextSequence() {
        int width = (int) _viewport.x;
        int height = (int) _viewport.y;

        if (width <= 0 || height <= 0) {
            return;
        }

        if (_active != null) {
            Sequence old = _active;

            if (_transition != null)
                _transition.recycle();

            _transition = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(_transition);
            old.draw(c, _offset);

            old.free();
            old = null;

            setStatus(SchedulerStatus.wait);

            if (_wait_time > -1.0f) {
                _wait = _wait_time;
            } else {
                _wait = -10.0f;
            }
        } else {
            _wait = -10.0f;
        }

        if (_playlist_iterator == null || !_playlist_iterator.hasNext()) {
            updatePlaylist();
        }

        if (_playlist_iterator != null && _playlist_iterator.hasNext()) {
            PlaylistEntry next = _playlist_iterator.next();
            InputStream is = Assets.openVectorData(_c, next);

            BlockingQueue<Sen> queue = new LinkedBlockingQueue<Sen>(MAX_QUEUE_SIZE);
            _active = Sequence.makeSequence(_c, next, queue, _viewport, true);
            LoadingRunnable parser = new LoadingRunnable(queue, is, _active);

            stopWorker();
            startWorker(parser);

            _prev = next;
        }
    }

    private void startWorker(Runnable r) {
        _worker = new Thread(r);
        _worker.setName("RyukiSenga parser " + _thread_count);
        _worker.setPriority(Thread.MIN_PRIORITY);
        //_worker.setDaemon(true);
        _worker.start();
    }

    private void stopWorker() {
        if (_worker != null) {
            Thread old = _worker;
            old.interrupt();
            old = null;
        }
    }

    public void update(float sec) {
        switch (_status) {
            case sequence:
                if (_active != null && (_active.isActive() || _active.isBuffering())) {
                    _active.update(sec);
                } else {
                    nextSequence();
                }
                break;
            case fade:
                if (_wait < -FADE_TIME) {
                    if (_active != null)
                        _active.setAlpha(255);

                    if (_transition != null)
                        _transition.recycle();

                    _active.update(.0f);

                    _wait = -10.0f;
                    setStatus(SchedulerStatus.sequence);
                } else {
                    _wait -= sec;
                }
                break;
            case wait:
                if (_wait_time >= .0f) {
                    _wait -= sec;

                    if (_wait < .0f) {
                        _wait = .0f;
                        setStatus(SchedulerStatus.fade);
                    }
                } else {
                    _wait = -10.0f;
                }
                break;

            case empty:
                _popup.update(sec);
                _popup2.update(sec);
                break;
        }
    }

    public void draw(Canvas c, float offset) {
        _offset = offset;

        switch (_status) {
            case sequence:
                if (_active != null) {
                    if (_active.isActive()) {
                        _active.draw(c, offset);
                    } else if (_active.isBuffering()) {
                        _active.draw(c, offset);

                        int size = _active.getQueue().size();
                        if (size < BUFFER_QUEUE_SIZE) {
                            float progress = (float) size / (float) BUFFER_QUEUE_SIZE;
                            _pb.draw(c, progress);
                        }
                    }
                }
                break;
            case fade:
                if (_transition != null)
                    c.drawBitmap(_transition, 0, 0, null);

                int alpha = Math.min(255, (int) (256.0f * (-_wait / FADE_TIME)));

                _active.setAlpha(alpha);
                _active.draw(c, offset);
                break;
            case wait:
                if (_transition != null)
                    c.drawBitmap(_transition, 0, 0, null);
                break;
            case empty:
                c.drawColor(Color.argb(60, 0, 0, 0));
                _popup.draw(c);
                _popup2.draw(c);
                break;
        }
    }

    public boolean isActive() {
        return _active != null;
    }

    public Sequence getActiveSequence() {
        return _active;
    }

    public void onDestroy() {
        stopWorker();

        if (isActive()) {
            _active.free();
            _active = null;
        }

        if (_transition != null)
            _transition.recycle();
    }

    public void setTransitionLength(float transition) {
        _wait_time = transition;
    }

    public boolean stopWaiting() {
        if (_status == SchedulerStatus.wait) {
            _wait = -10.0f;
            setStatus(SchedulerStatus.sequence);
            return true;
        } else {
            return false;
        }
    }

    private void setStatus(SchedulerStatus status) {
        _status = status;
    }

    private enum SchedulerStatus {
        sequence,
        wait,
        fade, empty,
    }

    public class LoadingRunnable implements Runnable, ParserInterface {
        private InputStream _is;
        private SvgContentHandler _ch;
        private BlockingQueue<Sen> _queue;

        private int _id;
        private WeakReference<Sequence> _s;

        public LoadingRunnable(BlockingQueue<Sen> queue, InputStream is, Sequence s) {
            _queue = queue;
            _is = is;
            _s = new WeakReference<Sequence>(s);
        }

        @Override
        public void run() {
            _id = ++_thread_count;
            doSequence();
        }

        private void doSequence() {
            try {
                //TODO cache previously parsed data?
                SVGUti.parseIgnoreNamespace(_is, _ch = new SvgContentHandler(this, _s.get().getSettings()));
            } catch (Exception e) {
                e.printStackTrace();
                //stopWorker();
                Thread.currentThread().interrupt();
            } finally {
                if (_s.get() != null)
                    _s.get().parsingComplete();
            }
        }

        private boolean isInvalid() {
            return Thread.currentThread() != _worker || _id != _thread_count;
        }

        private void stop() {
            _ch.abort();
            //stopWorker();
            _queue.clear();
            _queue = null;
            Thread.currentThread().interrupt();
        }

        public void append(Sen sen) {
            if (isInvalid()) {
                stop();
                return;
            }

            if (sen == null)
                return;

            try {
                //Put uses an unsafe "park" method that prevents interrupting.
                while (!_queue.offer(sen, 100, TimeUnit.MILLISECONDS)) {
                    if (isInvalid()) {
                        stop();
                    }
                }
            } catch (InterruptedException e) {
                stop();
                return;
            }
        }
    }
}
