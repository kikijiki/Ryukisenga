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

package com.kikijiki.ryukisenga.playlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.gallery.ArtistsIndexActivity;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry.Location;
import com.kikijiki.ryukisenga.styles.StyleManager;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleManagerData;
import com.kikijiki.ryukisenga.styles.StylePickerActivity;
import com.kikijiki.ryukisenga.ui.TouchInterceptor;
import com.kikijiki.ryukisenga.ui.TouchInterceptor.DragListener;
import com.kikijiki.ryukisenga.ui.TouchInterceptor.DropListener;
import com.kikijiki.ryukisenga.ui.TouchInterceptor.RemoveListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends Activity {
    protected static final int ADD_ITEM_RETURN_CODE = 1;
    protected static final int EDIT_STYLE_RETURN_CODE = 2;
    protected static final String SELECTED_POSITION_EXTRA = "position";

    private PlaylistAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playlist);

        _adapter = new PlaylistAdapter(this, getLayoutInflater());

        View footer = getLayoutInflater().inflate(R.layout.footer_button, null);
        Button edit_button = (Button) footer.findViewById(R.id.footer_button);

        edit_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent artistsIndexIntent = new Intent(PlaylistActivity.this, ArtistsIndexActivity.class);
                startActivityForResult(artistsIndexIntent, ADD_ITEM_RETURN_CODE);
            }
        });

        edit_button.setText(R.string.playlist_add_remove);

        loadPlaylist(_adapter);

        TouchInterceptor list = (TouchInterceptor) findViewById(R.id.playlist_list);
        list.addFooterView(footer);
        list.setFooterDividersEnabled(true);
        list.setAdapter(_adapter);
        list.setDropListener(_adapter);
        list.setDragListener(_adapter);
        list.setRemoveListener(_adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent stylePickerIntent = new Intent(PlaylistActivity.this, StylePickerActivity.class);

                PlaylistEntry entry = ((PlaylistAdapter.ViewHolder) view.getTag()).entry;
                stylePickerIntent.putExtra(SELECTED_POSITION_EXTRA, position);
                stylePickerIntent.putExtra(StylePickerActivity.SELECTED_ITEM_EXTRA, entry);

                startActivityForResult(stylePickerIntent, EDIT_STYLE_RETURN_CODE);
            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap open_raw = BitmapFactory.decodeResource(getResources(), R.drawable.trash_open, options);
        Bitmap closed_raw = BitmapFactory.decodeResource(getResources(), R.drawable.trash_closed, options);

        LevelListDrawable trash = new LevelListDrawable();

        int size = getResources().getDimensionPixelSize(R.dimen.normal_height);
        Bitmap open = Bitmap.createScaledBitmap(open_raw, size, size, false);
        Bitmap closed = Bitmap.createScaledBitmap(closed_raw, size, size, false);
        open_raw.recycle();
        closed_raw.recycle();

        trash.addLevel(0, 0, new BitmapDrawable(getResources(), closed));
        trash.addLevel(1, 1, new BitmapDrawable(getResources(), open));

        ImageView img = (ImageView) findViewById(R.id.playlist_trash);
        img.setImageDrawable(trash);

        list.setTrashcan(img);

        final CheckBox shuffle = (CheckBox) findViewById(R.id.playlist_shuffle);
        boolean shuffling_enabled = PlaylistManager.getShuffle(this);

        shuffle.setChecked(shuffling_enabled);
        shuffle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PlaylistManager.setShuffle(PlaylistActivity.this, isChecked);
            }
        });
    }

    private void loadPlaylist(PlaylistAdapter adapter) {
        adapter.clear();
        adapter.loadStyles(this);

        List<PlaylistEntry> pl = PlaylistManager.loadPlaylist(this, false);

        for (PlaylistEntry e : pl) {
            adapter.add(e);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ADD_ITEM_RETURN_CODE:
                loadPlaylist(_adapter);
                break;

            case EDIT_STYLE_RETURN_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String new_style = data.getExtras().getString(StylePickerActivity.SELECTED_STYLE_EXTRA);
                    int position = data.getExtras().getInt(SELECTED_POSITION_EXTRA);

                    if (new_style != null && position >= 0) {
                        PlaylistEntry cur = (PlaylistEntry) _adapter.getItem(position);
                        PlaylistManager.changeStyle(PlaylistActivity.this, cur, new_style);

                        loadPlaylist(_adapter);
                    }
                }
                break;
        }
    }

    private static class PlaylistAdapter extends BaseAdapter implements DropListener, DragListener, RemoveListener {
        private final Bitmap _droidIcon;
        private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        private final int cacheSize = maxMemory / 8;
        private WeakReference<LayoutInflater> _inflater;
        private StyleManagerData _data;
        private WeakReference<Context> _c;
        private ArrayList<PlaylistEntry> _pl = new ArrayList<PlaylistEntry>();
        private LruCache<String, Bitmap> _iconsCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        public PlaylistAdapter(Context c, LayoutInflater inflater) {
            _inflater = new WeakReference<LayoutInflater>(inflater);

            _c = new WeakReference<Context>(c);
            loadStyles(c);

            _droidIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.robocard);
        }

        public void loadStyles(Context c) {
            _data = StyleManager.LoadData(c);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View ret = null;
            final PlaylistEntry cur = (PlaylistEntry) getItem(position);
            ViewHolder vh;

            if (convertView == null) {
                ret = _inflater.get().inflate(R.layout.playlist_entry, null);

                vh = new ViewHolder();
                vh.title = (TextView) ret.findViewById(R.id.playlist_item_title);
                vh.icon = (ImageView) ret.findViewById(R.id.playlist_item_icon);
                vh.style = (TextView) ret.findViewById(R.id.playlist_item_style);

                ret.setTag(vh);
            } else {
                ret = convertView;
                vh = (ViewHolder) ret.getTag();
            }

            vh.entry = cur;

            vh.title.setText(cur.title);

            if (cur.location == Location.apk) {
                Bitmap b = null;

                if ((b = (Bitmap) _iconsCache.get(cur.path)) == null) {
                    b = BitmapFactory.decodeStream(Assets.apkOpenVectorIconFromPath(_c.get(), cur.path));
                    _iconsCache.put(cur.path, b); //TODO: null pointer in case we delete assets present in the playlist
                }

                vh.icon.setImageBitmap(b);
            } else {
                vh.icon.setImageBitmap(_droidIcon);
            }

            String style_desc = _c.get().getString(R.string.playlist_entry_style_desc);


            vh.style.setText(style_desc + StyleManager.GetStyleName(_data, cur.style));

            return ret;
        }

        @Override
        public int getCount() {
            return _pl.size();
        }

        @Override
        public Object getItem(int position) {
            return _pl.get(position);
        }

        public void add(PlaylistEntry entry) {
            _pl.add(entry);
            this.notifyDataSetChanged();
        }

        public void clear() {
            _pl.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void drag(int from, int to) {

        }

        @Override
        public void drop(int from, int to) {
            if (from >= 0 && from < _pl.size() && to <= _pl.size() && to >= 0) {
                PlaylistManager.drop(_c.get(), from, to);

                PlaylistEntry tmp = _pl.get(from);
                _pl.remove(from);
                _pl.add(Math.min(to, _pl.size()), tmp);
                notifyDataSetChanged();
            }
        }

        @Override
        public void remove(int which) {
            PlaylistEntry entry = _pl.remove(which);
            PlaylistManager.remove(_c.get(), entry);
            notifyDataSetChanged();
        }

        public static class ViewHolder {
            TextView title;
            ImageView icon;
            TextView style;
            PlaylistEntry entry;
        }
    }
}
