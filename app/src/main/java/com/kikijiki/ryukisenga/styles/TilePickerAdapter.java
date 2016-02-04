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

package com.kikijiki.ryukisenga.styles;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.TilePreviewCache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class TilePickerAdapter extends ArrayAdapter<String> {
    private TilePreviewCache _cache;
    private Set<ImageView> _views = new HashSet<ImageView>();

    public TilePickerAdapter(Context context) {
        super(context, 0);
        _cache = new TilePreviewCache(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View ret = null;
        String tile = getItem(position);

        if (convertView == null) {
            ret = LayoutInflater.from(getContext()).inflate(R.layout.tile_picker_entry, null);
        } else {
            ret = convertView;
        }

        ImageView img = (ImageView) ret.findViewById(R.id.tile_picker_entry_image);
        _views.add(img);

        Future<?> future = (Future<?>) img.getTag();

        if (future != null) {
            try {
                future.get();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        future = _cache.loadImageView(img, tile);
        img.setTag(future);

        ret.setTag(tile);

        return ret;
    }

    public void free() {
        for (ImageView i : _views) {
            Drawable d = i.getDrawable();

            if (d instanceof BitmapDrawable) {
                ((BitmapDrawable) d).getBitmap().recycle();
            }
        }
    }
}
