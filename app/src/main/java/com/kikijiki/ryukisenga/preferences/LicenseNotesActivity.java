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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

import com.kikijiki.ryukisenga.R;

@SuppressLint("SetJavaScriptEnabled")
public class LicenseNotesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.license_notes);

        WebView license = (WebView) findViewById(R.id.license_content);
        license.getSettings().setJavaScriptEnabled(true);
        ;

        license.loadUrl("file:///android_asset/app/license/license.html");
        license.setBackgroundColor(Color.TRANSPARENT);
    }

}
