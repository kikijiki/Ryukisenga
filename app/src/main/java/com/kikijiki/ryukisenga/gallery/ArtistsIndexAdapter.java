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
import android.util.Xml;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.gallery.ArtistsIndexFragment.ArtistsIndexEntry;

import java.io.IOException;
import java.io.InputStream;

public class ArtistsIndexAdapter extends ArrayAdapter<String> {
    public ArtistsIndexAdapter(Context context) {
        super(context, R.layout.artists_index_entry, R.id.artists_index_artist_name);

        this.add("user");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = (LinearLayout) (LayoutInflater.from(getContext()).inflate(R.layout.artists_index_entry, null));
        }

        TextView text_name = (TextView) convertView.findViewById(R.id.artists_index_artist_name);

        TextView text_count = (TextView) convertView.findViewById(R.id.artists_index_artist_count);
        ImageView iv = (ImageView) convertView.findViewById(R.id.artists_index_artist_image);

        if (position == 0) {
            //user
            text_name.setText(getContext().getString(R.string.artists_index_user_name));
            iv.setImageResource(R.drawable.robocard);
            text_count.setText("");
        } else {
            ArtistsIndexEntry entry = new ArtistsIndexEntry();
            entry.dir = getItem(position);

            try {
                ArtistsIndexContentHandler aich = new ArtistsIndexContentHandler(entry);
                Xml.parse(Assets.apkOpenArtistInfo(getContext(), entry.dir), Encoding.UTF_8, aich);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            text_name.setText(entry.name);

            try {
                InputStream is = getContext().getAssets().open("gallery/artists/" + entry.dir + "/icon.png");
                Bitmap icon = BitmapFactory.decodeStream(is);
                iv.setImageBitmap(icon);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int count = Assets.apkLlistVectorDirectories(getContext(), entry.dir).length;

            text_count.setText(getContext().getString(R.string.artists_index_artist_count) + ": " + count);

            convertView.setTag(entry.dir);
        }

        return convertView;
    }
}
