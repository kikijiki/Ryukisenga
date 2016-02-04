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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleEntry;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleManagerData;

public class CustomStylesActivity extends Activity {
    protected static final int ADD_STYLE_RETURN_CODE = 1;
    protected static final int EDIT_STYLE_RETURN_CODE = 2;

    private CustomStylesAdapter _adapter;
    private ListView _list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_stylelist);

        _list = (ListView) this.findViewById(R.id.stylelist_list);

        Button add_button = new Button(this);
        add_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomStylesActivity.this, StyleEditorActivity.class);
                startActivityForResult(i, ADD_STYLE_RETURN_CODE);
            }
        });

        add_button.setText(R.string.stylelist_add);

        AbsListView.LayoutParams p = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.normal_height));
        add_button.setLayoutParams(p);

        _list.addFooterView(add_button);
        _list.setFooterDividersEnabled(true);
        _list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent styleEditorIntent = new Intent(CustomStylesActivity.this, StyleEditorActivity.class);

                StyleEntry entry = (StyleEntry) view.getTag();
                styleEditorIntent.putExtra(StyleEditorActivity.STYLE_FILENAME_EXTRA, entry.file);

                startActivityForResult(styleEditorIntent, EDIT_STYLE_RETURN_CODE);
            }
        });

        _list.setAdapter(_adapter = new CustomStylesAdapter(this));
        loadStyles();
    }

    private void loadStyles() {
        _adapter.clear();

        StyleManagerData data = StyleManager.LoadData(this);

        for (StyleEntry e : data.sd) {
            if (e.name != null && e.name.length() > 0) {
                _adapter.add(e);
            }
        }

        _adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ADD_STYLE_RETURN_CODE:
            case EDIT_STYLE_RETURN_CODE:
                loadStyles();
                break;
        }
    }

    private static class CustomStylesAdapter extends ArrayAdapter<StyleEntry> {
        public CustomStylesAdapter(Context c) {
            super(c, R.layout.custom_stylelist_entry, R.id.playlist_item_title);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = null;
            final StyleEntry cur = getItem(position);

            if (convertView == null) {
                ret = LayoutInflater.from(getContext()).inflate(R.layout.custom_stylelist_entry, null);
            } else {
                ret = convertView;
            }

            TextView title = (TextView) ret.findViewById(R.id.stylelist_item_title);
            Button delete = (Button) ret.findViewById(R.id.stylelist_item_delete);

            title.setText(cur.name);

            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.custom_style_list_confirm_delete_title)
                            .setMessage(getContext().getString(R.string.custom_style_list_confirm_delete_message) + " \"" + cur.name + "\"?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    remove(cur);
                                }
                            })
                            .setNeutralButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setCancelable(true)
                            .show();

                }
            });

            delete.setTag(cur);
            ret.setTag(cur);

            return ret;
        }

        @Override
        public void remove(StyleEntry object) {
            StyleManager.RemoveStyle(getContext(), object);
            super.remove(object);
        }
    }
}
