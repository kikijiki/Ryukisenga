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

package com.kikijiki.ryukisenga.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.gallery.ArtistPageFragment.GalleryEntry;

public class ArtistGalleryAdapter extends ArrayAdapter<GalleryEntry> {
    private final int maxMemory =
            (int) (Runtime.getRuntime().maxMemory() / 1024);
    private final int cacheSize = maxMemory / 8;
    private LruCache<String, Bitmap> _iconsCache =
            new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            };

    public ArtistGalleryAdapter(Context context, String artist) {
        super(context, R.layout.gallery_entry, R.id.gallery_entry_title);
    }

    @Override
    public void add(GalleryEntry object) {
        super.add(object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret;

        if (convertView == null) {
            ret = LayoutInflater.from(getContext())
                    .inflate(R.layout.gallery_entry, null);
        } else {
            ret = convertView;
        }

        GalleryEntry cur = getItem(position);

        CheckBox title = (CheckBox) ret.findViewById(R.id.gallery_entry_title);
        title.setText(cur.data.title);
        title.setChecked(cur.selected);
        title.setFocusable(false);
        title.setClickable(false);

        ImageView icon = (ImageView) ret.findViewById(R.id.gallery_entry_image);

        Bitmap b = _iconsCache.get(cur.data.path);
        if (b == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                new LoadBitmapTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cur.data.path, icon);
            }
            else {
                new LoadBitmapTask().execute(cur.data.path, icon);
            }
        } else {
            icon.setImageBitmap(b);
        }

        if (cur.selected) {
            ret.setBackgroundResource(
                    R.drawable.gallery_entry_rectangle_selected);
        } else {
            ret.setBackgroundResource(
                    R.drawable.gallery_entry_rectangle_unselected);
        }

        ret.setTag(title);

        return ret;
    }

    private class LoadBitmapTask extends AsyncTask<Object, Void, Void> {
        Bitmap _bitmap;
        ImageView _target;

        @Override
        protected Void doInBackground(Object... params) {
            String path = (String) params[0];
            _target = (ImageView) params[1];

            _bitmap = BitmapFactory.decodeStream(
                    Assets.apkOpenVectorIconFromPath(getContext(), path));
            _iconsCache.put(path, _bitmap);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            _target.setImageBitmap(_bitmap);
            super.onPostExecute(result);
        }


    }
}
