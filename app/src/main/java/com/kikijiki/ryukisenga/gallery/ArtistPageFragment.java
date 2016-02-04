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
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.playlist.PlaylistManager;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;

import com.bluejamesbond.text.DocumentView;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ArtistPageFragment extends Fragment {
    private String _artistName;
    private ArtistEntry _artist;

    public static ArtistPageFragment newInstance(int index, String artist) {
        ArtistPageFragment f = new ArtistPageFragment();

        Bundle args = new Bundle();
        args.putInt(ArtistsIndexFragment.SELECTION_BUNDLE_KEY, index);
        args.putString(ArtistsIndexFragment.ARTIST_BUNDLE_KEY, artist);
        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt(ArtistsIndexFragment.SELECTION_BUNDLE_KEY, 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        int index = getArguments().getInt(ArtistsIndexFragment.SELECTION_BUNDLE_KEY);
        _artistName = getArguments().getString(ArtistsIndexFragment.ARTIST_BUNDLE_KEY);

        if (index == 0)
            return null;

        final View ret = inflater.inflate(R.layout.artist_page, null);

        GridView gallery = (GridView) ret.findViewById(R.id.artist_page_gallery_grid);
        final ArtistGalleryAdapter adapter = new ArtistGalleryAdapter(getActivity(), _artistName);
        gallery.setAdapter(adapter);

        return ret;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inflateArtist(view, LayoutInflater.from(this.getActivity()));
    }

    private void inflateArtist(View root, LayoutInflater inflater) {
        GridView gallery = (GridView) root.findViewById(R.id.artist_page_gallery_grid);
        ImageView icon = (ImageView) root.findViewById(R.id.artist_page_artist_icon);

        gallery.setSelector(android.R.color.transparent);

        try {
            icon.setImageBitmap(BitmapFactory.decodeStream(Assets.apkOpenArtistIcon(getActivity(), _artistName)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        _artist = new ArtistEntry();

        //Parse artist info
        try {
            ArtistPageContentHandler agch = new ArtistPageContentHandler(_artist);
            InputStream is = Assets.apkOpenArtistInfo(getActivity(), _artistName);
            Xml.parse(is, Encoding.UTF_8, agch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TextView artist_name = (TextView) root.findViewById(R.id.artist_page_artist_name);
        artist_name.setText(_artist.name);

        TextView artist_twitter = (TextView) root.findViewById(R.id.artist_page_artist_twitter_right);
        TextView artist_twitter_label = (TextView) root.findViewById(R.id.artist_page_artist_twitter_left);

        if (_artist.twitter != null && _artist.twitter.length() > 0) {
            artist_twitter.setText("@" + _artist.twitter);
            artist_twitter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("http://www.twitter.com/" + _artist.twitter);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(uri);

                    getActivity().startActivity(i);
                }
            });
        } else {
            artist_twitter.setVisibility(View.GONE);
            artist_twitter_label.setVisibility(View.GONE);
        }

        TextView artist_link = (TextView) root.findViewById(R.id.artist_page_artist_website_right);
        TextView artist_link_label = (TextView) root.findViewById(R.id.artist_page_artist_website_left);
        if (_artist.link != null && _artist.link.length() > 0) {

            artist_link.setText(_artist.link);
            artist_link.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String site = _artist.link;

                    if (!site.startsWith("http://")) {
                        site = "http://" + site;
                    }

                    Uri uri = Uri.parse(site);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(uri);

                    getActivity().startActivity(i);
                }
            });
        } else {
            artist_link.setVisibility(View.GONE);
            artist_link_label.setVisibility(View.GONE);
        }

        TextView artist_mail = (TextView) root.findViewById(R.id.artist_page_artist_email_right);
        TextView artist_mail_label = (TextView) root.findViewById(R.id.artist_page_artist_email_left);

        if (_artist.mail != null && _artist.mail.length() > 0) {
            artist_mail.setText(_artist.mail);
            artist_mail.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{_artist.mail});
                    getActivity().startActivity(Intent.createChooser(i, getResources().getString(R.string.artist_page_artist_mail_prompt)));
                }
            });
        } else {
            artist_mail.setVisibility(View.GONE);
            artist_mail_label.setVisibility(View.GONE);
        }

        //Parse artist gallery
        final ArtistGalleryAdapter adapter = (ArtistGalleryAdapter) gallery.getAdapter();

        String[] list = Assets.apkLlistVectorDirectories(getActivity(), _artistName);

        List<PlaylistEntry> pl = PlaylistManager.loadPlaylist(getActivity(), false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            new LoadGalleryItemTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, list, pl, adapter);
        }
        else {
            new LoadGalleryItemTask().execute(list, pl, adapter);
        }

        gallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GalleryEntry entry = adapter.getItem(position);

                if (entry.selected) {
                    v.setBackgroundResource(R.drawable.gallery_entry_rectangle_unselected);
                    ((CheckBox) v.getTag()).setChecked(false);
                    PlaylistManager.remove(getActivity(), entry.data);
                } else {
                    v.setBackgroundResource(R.drawable.gallery_entry_rectangle_selected);
                    ((CheckBox) v.getTag()).setChecked(true);
                    PlaylistManager.add(getActivity(), entry.data);
                }

                entry.selected = !entry.selected;
            }
        });

        TextView artist_info = (TextView) root.findViewById(R.id.artist_page_artist_info_right);
        TextView artist_info_label = (TextView) root.findViewById(R.id.artist_page_artist_info_left);

        if (_artist.info != null && _artist.info.length() > 0) {
            artist_info.setText(_artist.info);
        } else {
            artist_info.setVisibility(View.GONE);
            artist_info_label.setVisibility(View.GONE);
        }
    }

    public static class ArtistEntry {
        String name = "";
        String mail = "";
        String link = "";
        String info = "";
        String twitter = "";
    }

    public static class GalleryEntry implements Serializable {
        private static final long serialVersionUID = -1328875036825962633L;

        public PlaylistEntry data;
        public boolean selected = false;

        public GalleryEntry(String artist, String path, PlaylistEntry.Location location)
        {
            data = new PlaylistEntry(artist, path, location);
        }
    }

    private class LoadGalleryItemTask extends AsyncTask<Object, Void, Void> {
        ArtistGalleryAdapter _adapter;
        private List<GalleryEntry> _addlist = new ArrayList<GalleryEntry>();

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Object... params) {
            String[] items = (String[]) params[0];
            List<PlaylistEntry> pl = (List<PlaylistEntry>) params[1];
            _adapter = (ArtistGalleryAdapter) params[2];

            for (String v : items) {
                String path = Assets.apkVectorPath(_artistName, v);
                final GalleryEntry entry = new GalleryEntry(_artistName, path, PlaylistEntry.Location.apk);

                try {
                    GalleryEntryContentHandler gech = new GalleryEntryContentHandler(entry);
                    Xml.parse(Assets.apkOpenVectorInfoFromPath(getActivity(), path), Encoding.UTF_8, gech);
                } catch (Exception e) {
                    //e.printStackTrace();
                }

                entry.selected = pl.contains(entry.data);
                _addlist.add(entry);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            for (GalleryEntry ge : _addlist) {
                _adapter.add(ge);
            }
        }

    }
}
