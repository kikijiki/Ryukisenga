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

package com.kikijiki.ryukisenga.gallery;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.gallery.ArtistPageFragment.GalleryEntry;
import com.kikijiki.ryukisenga.playlist.PlaylistManager;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserGalleryFragment extends Fragment {
    private static final String VECTOR_LIST_BUNDLE_KEY = "list";

    private ArrayList<GalleryEntry> _vector_list;
    private ListView _gallery;
    UserGalleryAdapter _adapter;
    Button _scanButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View ret = inflater.inflate(R.layout.user_gallery, null);
        _adapter = new UserGalleryAdapter(getActivity());
        _gallery = (ListView) ret.findViewById(R.id.user_gallery_gallery);
        _gallery.setAdapter(_adapter);
        _scanButton = (Button) ret.findViewById(R.id.user_gallery_rescan);
        _scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        return ret;
    }

    private void scan()
    {
        _vector_list = new ArrayList<GalleryEntry>();
        _adapter.clear();

        AsyncTask<Void, Void, Void> scanTask = new AsyncTask<Void, Void, Void>() {
            private ProgressDialog _progressDialog = new ProgressDialog(getActivity());

            @Override
            protected void onPreExecute() {
                _progressDialog.setTitle("Loading");
                _progressDialog.setMessage(getActivity().getString(R.string.user_gallery_scan_started));
                _progressDialog.show();
                _scanButton.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                final ArrayList<String> files = Assets.sdScanForVectors(getActivity());

                List<PlaylistEntry> pl = PlaylistManager.loadPlaylist(getActivity(), false);

                for (String file : files) {
                    final GalleryEntry e = new GalleryEntry("user", file, Location.sd);
                    e.data.title = file.substring(file.lastIndexOf(File.separator) + 1, file.lastIndexOf(Assets.VECTOR_FILE_EXTENSION));
                    e.selected = pl.contains(e.data);

                    _vector_list.add(e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                for (GalleryEntry e : _vector_list) {
                    _adapter.add(e);
                }

                _progressDialog.dismiss();
                _scanButton.setEnabled(true);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            scanTask.execute();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int pad = getResources().getDimensionPixelSize(R.dimen.list_padding);

        if (savedInstanceState != null && savedInstanceState.containsKey(VECTOR_LIST_BUNDLE_KEY)) {
            _vector_list = (ArrayList<GalleryEntry>) savedInstanceState.getSerializable(VECTOR_LIST_BUNDLE_KEY);

            for (GalleryEntry ge : _vector_list)
                _adapter.add(ge);
        } else {
            scan();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(VECTOR_LIST_BUNDLE_KEY, _vector_list);
    }

    public class UserGalleryAdapter extends ArrayAdapter<GalleryEntry> {
        public UserGalleryAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View cv, ViewGroup parent) {
            if (cv == null) {
                cv = LayoutInflater.from(getContext()).inflate(R.layout.user_gallery_entry, null);
            }

            final GalleryEntry cur = getItem(position);

            CheckBox checkbox = (CheckBox) cv.findViewById(R.id.gallery_entry_checkbox);
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(cur.selected);
            checkbox.setText(cur.data.title);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cur.selected = isChecked;

                    if (cur.selected) {
                        PlaylistManager.add(getActivity(), cur.data);
                    } else {
                        PlaylistManager.remove(getActivity(), cur.data);
                    }
                }
            });

            cv.setTag(checkbox);
            return cv;
        }
    }
}
