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

package com.kikijiki.ryukisenga.preferences;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PreferenceEntry {
    String id;
    Preference preference;

    @SuppressWarnings("deprecation")
    public PreferenceEntry(String id, PreferenceActivity pa) {
        this.id = id;
        preference = pa.findPreference(id);
    }

    @SuppressWarnings("deprecation")
    public PreferenceEntry(String id, PreferenceActivity pa, OnPreferenceClickListener listener) {
        this.id = id;
        preference = pa.findPreference(id);
        preference.setOnPreferenceClickListener(listener);
    }
}
