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

package com.kikijiki.ryukisenga.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import com.kikijiki.ryukisenga.R;

public class LicenseMessageActivity extends Activity {
    public static final String PROMPT_LICENCE = "licence";
    public static final String PROMPT_ERROR = "error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        init();
    }

    private void init() {
        Intent i = getIntent();

        String type = i.getStringExtra("type");

        if (PROMPT_LICENCE.equals(type)) {
            showLicenceWarning();
        } else if (PROMPT_ERROR.equals(type)) {
            showServerError();
        } else {
            finish();
        }
    }

    private void showLicenceWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(this.getString(R.string.licenseError));
        builder.setTitle(this.getString(R.string.licenseErrorTitle));

        builder.setPositiveButton(getString(R.string.licenseOpenMarket), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("market://search?q=pub:\"KIKIJIKI\""); //String resource wont have upper case?
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(marketIntent);
                LicenseMessageActivity.this.finish();
            }
        });

        builder.setNegativeButton(getString(R.string.licenseExit), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LicenseMessageActivity.this.finish();

                Intent returnHome = new Intent(Intent.ACTION_MAIN);
                returnHome.addCategory(Intent.CATEGORY_HOME);
                returnHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(returnHome);
            }

        });

        builder.setCancelable(false);
        builder.create().show();
    }

    private void showServerError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(this.getString(R.string.licenceOtherError));
        builder.setTitle(this.getString(R.string.licenseOtherErrorTitle));

        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LicenseMessageActivity.this.finish();

                Intent returnHome = new Intent(Intent.ACTION_MAIN);
                returnHome.addCategory(Intent.CATEGORY_HOME);
                returnHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(returnHome);
            }
        });

        builder.setCancelable(false);
        builder.create().show();
    }
}
