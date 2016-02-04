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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.styles.data.StyleData;

public class TilePickerActivity extends Activity {
    private final static int TILE_GALLERY_RESULT = 1;
    private static final String TILE_ARRAY_BUNDLE_KEY = "tile_array";

    private String[] _tile_array;
    private TilePickerAdapter _adapter;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.tile_picker);

        ListView list = (ListView) findViewById(R.id.tile_picker_tile_list);

        if (state != null && state.containsKey(TILE_ARRAY_BUNDLE_KEY)) {
            _tile_array = state.getStringArray(TILE_ARRAY_BUNDLE_KEY);

            if (_tile_array.length == 0) {
                _tile_array = Assets.apkListTiles(this);
            }
        } else {
            _tile_array = Assets.apkListTiles(this);
        }

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = getIntent();
                i.putExtra(StyleEditorActivity.TILE_EXTRA, StyleData.DEFAULT_TILE_LOCATION_PREFIX + view.getTag().toString());
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        });

        Button open_gallery = new Button(this);
        open_gallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.tile_picker_open_gallery)), TILE_GALLERY_RESULT);
            }
        });

        open_gallery.setText(R.string.tile_picker_open_gallery);

        list.addFooterView(open_gallery);

        _adapter = new TilePickerAdapter(this); //Don't use array in the constructor to avoid caching problems
        list.setAdapter(_adapter);

        for (String tile : _tile_array) {
            _adapter.add(tile);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == TILE_GALLERY_RESULT) {
                Uri selectedImageUri = data.getData();

                String path = getPath(selectedImageUri);

                if (path == null) {
                    path = selectedImageUri.getPath();
                }

                Intent i = getIntent();
                i.putExtra(StyleEditorActivity.TILE_EXTRA, StyleData.CUSTOM_TILE_LOCATION_PREFIX + path);
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null && _tile_array == null && _tile_array.length > 0) {
            outState.putStringArray(TILE_ARRAY_BUNDLE_KEY, _tile_array);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (_adapter != null) {
            _adapter.free();
        }
    }


}