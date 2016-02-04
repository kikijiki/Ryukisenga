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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.Environment;
import android.widget.ImageView;

import com.kikijiki.ryukisenga.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TilePreviewCache {
    private ExecutorService _executor = Executors.newSingleThreadExecutor();
    private Set<String> _pending = Collections.synchronizedSet(new HashSet<String>());
    private Context _c;

    public TilePreviewCache(Context context) {
        _c = context;
    }

    private String mangle(String path) {
        File tile = new File(path);

        String filename = path.replace(File.separator, "_").replace(".", "_");
        filename = "tile_preview_" + filename + tile.lastModified() + tile.length();

        return filename;
    }

    private void saveToSdCache(Bitmap bmp, String svgPath) {
        File sdroot = Environment.getExternalStorageDirectory();
        File dir = new File(sdroot, Assets.SD_CACHE_PATH);
        String mangled_name = mangle(svgPath);
        File path = new File(dir, mangled_name);

        try {
            dir.mkdirs();

            FileOutputStream f = new FileOutputStream(path);
            bmp.compress(CompressFormat.PNG, 100, f);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }
    }

    private Bitmap openSdCache(String path) {
        File sdroot = Environment.getExternalStorageDirectory();
        File dir = new File(sdroot, Assets.SD_CACHE_PATH);
        File cached = new File(dir, mangle(path));

        try {
            if (cached.exists() && cached.isFile() && cached.canRead()) {
                FileInputStream is = new FileInputStream(cached);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                is.close();
                return bmp;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return null;
    }

    public void clear() {
        if (_executor != null) {
            _executor.shutdownNow();
            _executor = null;
        }
    }

    public Future<?> loadImageView(ImageView view, String tile) {
        Bitmap pic = openSdCache(tile);

        if (pic != null) //Is cached in the sd
        {
            view.setImageBitmap(pic);
        } else //Needs loading
        {
            //Drawable old = view.getDrawable();

            view.setImageResource(R.drawable.loading_preview_large);

            if (!_pending.contains(tile)) {
                _pending.add(tile);
                return _executor.submit(new AsyncCachedPreviewLoader(tile, view));
            }
        }

        return null;
    }

    public interface CacheListener {
        void cached(String path, Bitmap pic);
    }

    private class AsyncCachedPreviewLoader implements Runnable {
        private String _tile;
        private WeakReference<ImageView> _view;

        public AsyncCachedPreviewLoader(String tile, ImageView view) {
            _tile = tile;
            _view = new WeakReference<ImageView>(view);
        }

        @Override
        public void run() {
            final Bitmap fromSd = openSdCache(_tile);

            if (fromSd == null) {
                if (_view.get() != null) {
                    int width = _c.getResources().getDimensionPixelSize(R.dimen.app_width);
                    int height = _c.getResources().getDimensionPixelSize(R.dimen.tile_preview_height);

                    Bitmap bmp = BitmapFactory.decodeStream(Assets.apkOpenTile(_c, _tile));
                    final Bitmap tiled = renderTilePreview(bmp, width, height);
                    bmp.recycle();

                    saveToSdCache(tiled, _tile);

                    _view.get().post(new Runnable() {
                        @Override
                        public void run() {
                            if (tiled != null) {
                                _view.get().setImageBitmap(tiled);
                            } else {
                                _view.get().setImageResource(R.drawable.default_preview_large);
                            }
                        }
                    });
                }
            } else {
                if (_view.get() != null) {
                    _view.get().post(new Runnable() {
                        @Override
                        public void run() {
                            _view.get().setImageBitmap(fromSd);
                        }
                    });
                }
            }

            _pending.remove(_tile);
        }

        private Bitmap renderTilePreview(Bitmap b, int width, int height) {
            Bitmap buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(buffer);

            Paint p = new Paint();
            p.setShader(new BitmapShader(b, TileMode.REPEAT, TileMode.REPEAT));
            c.drawRect(0, 0, width, height, p);

            return buffer;
        }
    }
}
