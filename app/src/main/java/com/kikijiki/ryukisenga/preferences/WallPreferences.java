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

package com.kikijiki.ryukisenga.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.playlist.PlaylistActivity;
import com.kikijiki.ryukisenga.styles.CustomStylesActivity;

import java.util.HashMap;
import java.util.Map;

public class WallPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static VisibilityListener Visiblitylistener;
    Map<String, Preference> _p = new HashMap<String, Preference>();

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        setupPlaylistPreference();
        setupStyleEditorPreference();
        setupTrailPreference();
        setupLicenseNotes();

        if (Visiblitylistener != null)
            Visiblitylistener.onVisibilityChanged(true);
    }

    @SuppressWarnings("deprecation")
    private void setupLicenseNotes() {
        Preference p = findPreference(Keys.LICENSE_NOTES);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(WallPreferences.this, LicenseNotesActivity.class);
                startActivity(i);
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setupPlaylistPreference() {
        Preference p = findPreference(Keys.EDIT_PLAYLIST);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(WallPreferences.this, PlaylistActivity.class);
                startActivity(i);
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setupStyleEditorPreference() {
        Preference p = findPreference(Keys.STYLE_EDITOR);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(WallPreferences.this, CustomStylesActivity.class);
                startActivity(i);

                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setupTrailPreference() {
        ListPreference p = (ListPreference) findPreference(Keys.SEQUENCE_USE_TRAIL);
        p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (((String) newValue).equalsIgnoreCase("use_trail")) {
                    preference.setSummary(R.string.preferences_sequence_use_trail_enabled);
                } else {
                    preference.setSummary(R.string.preferences_sequence_use_trail_disabled);
                }

                return true;
            }
        });

        if (p.getValue().equalsIgnoreCase("use_trail")) {
            p.setSummary(R.string.preferences_sequence_use_trail_enabled);
        } else {
            p.setSummary(R.string.preferences_sequence_use_trail_disabled);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Visiblitylistener != null)
            Visiblitylistener.onVisibilityChanged(false);
    }

    @SuppressWarnings("deprecation")
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

    public class Keys {
        public static final String RYUKISENGA_PREFERENCES = "ryukisenga";

        public final static String PLAYLIST_PREFERENCE_KEY = "playlist";
        public final static String PLAYLIST_SHUFFLE_PREFERENCE = "shuffle";

        public static final String EDIT_PLAYLIST = "preferences_edit_playlist";
        public static final String STYLE_EDITOR = "preferences_edit_styles";

        public static final String SEQUENCE_DISPLAY_MODE = "preferences_sequence_display_mode";
        public static final String SEQUENCE_SCROLL_MODE = "preferences_sequence_scroll_mode";
        public static final String SEQUENCE_LINE_SPEED = "preferences_sequence_line_speed";
        public static final String SEQUENCE_TRAIL_SPEED = "preferences_sequence_trail_speed";
        public static final String SEQUENCE_USE_TRAIL = "preferences_sequence_use_trail";
        public static final String SEQUENCE_GROUP_LENGTH = "preferences_sequence_group_length";
        public static final String SEQUENCE_TRANSITION_LENGTH = "preferences_playlist_interval";

        public static final String LICENSE_NOTES = "preferences_license_notes";
    }
}
