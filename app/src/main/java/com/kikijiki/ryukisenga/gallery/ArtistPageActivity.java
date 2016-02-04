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

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;

public class ArtistPageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            int selection = getIntent().getIntExtra(ArtistsIndexFragment.SELECTION_BUNDLE_KEY, 0);

            if (selection == 0) {
                Fragment user = new UserGalleryFragment();
                getFragmentManager().beginTransaction().add(android.R.id.content, user).commit();
            } else {
                Fragment artist = new ArtistPageFragment();
                artist.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add(android.R.id.content, artist).commit();
            }
        }
    }
}
