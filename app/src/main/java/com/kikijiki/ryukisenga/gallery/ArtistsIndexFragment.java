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

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ArtistsIndexFragment extends ListFragment {
    public static final String SELECTION_BUNDLE_KEY = "selection";
    public static final String ARTIST_BUNDLE_KEY = "artist";
    private boolean _dual;
    private int _selection = -1;

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);

        int pad = getResources().getDimensionPixelSize(R.dimen.list_padding);

        ListView list = getListView();
        //list.setSelector(android.R.color.transparent);
        //list.setCacheColorHint(0);
        list.setPadding(pad, pad, pad, pad);
        list.setVerticalFadingEdgeEnabled(false);
        list.setHorizontalFadingEdgeEnabled(false);
        list.setDivider(null);
        list.setDividerHeight(getActivity().getResources().getDimensionPixelSize(R.dimen.default_divider_height));

        setListAdapter(new ArtistsIndexAdapter(getActivity()));

        View artist_gallery = getActivity().findViewById(R.id.artists_index_artist_page_fragment);

        _dual = artist_gallery != null && artist_gallery.getVisibility() == View.VISIBLE;

        loadArtists();

        int pre = 1;

        if (savedState != null) {
            pre = savedState.getInt(SELECTION_BUNDLE_KEY, 1);
            if (pre < 0) pre = 1;
        }

        if (_dual) {
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showDetails(pre);
        }
    }

    private void loadArtists() {
        ArtistsIndexAdapter adapter = new ArtistsIndexAdapter(getActivity());

        ArrayList<String> artists = new ArrayList<String>(Arrays.asList(Assets.listApkArtists(getActivity())));

        if (artists.contains("Samples")){
            artists.remove("Samples");
            artists.add("Samples");
        }

        if (artists.contains("Contribute")){
            artists.remove("Contribute");
            artists.add("Contribute");
        }

        for (String a : artists) {
            adapter.add(a);
        }

        setListAdapter(adapter);
    }

    void showDetails(int index) {
        if (_dual) {
            showFragment(index);
        } else {
            showActivity(index);
        }

        _selection = index;
    }

    private void showFragment(int index) {
        if (index == _selection)
            return;

        if (index == 0) {
            getListView().setItemChecked(index, true);

            UserGalleryFragment details = new UserGalleryFragment();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.artists_index_artist_page_fragment, details);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        } else {
            String artist = (String) getListAdapter().getItem(index);

            getListView().setItemChecked(index, true);

            ArtistPageFragment details = ArtistPageFragment.newInstance(index, artist);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.artists_index_artist_page_fragment, details);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    private void showActivity(int index) {
        if (index == 0) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ArtistPageActivity.class);
            startActivity(intent);
        } else {
            String artist = (String) getListAdapter().getItem(index);

            Intent intent = new Intent();
            intent.setClass(getActivity(), ArtistPageActivity.class);
            intent.putExtra(SELECTION_BUNDLE_KEY, index);
            intent.putExtra(ARTIST_BUNDLE_KEY, artist);
            startActivity(intent);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showDetails(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTION_BUNDLE_KEY, _selection);
    }

    public static class ArtistsIndexEntry {
        String dir;
        String name;
        int imageCount;
    }
}
