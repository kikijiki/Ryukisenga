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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleEntry;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleManagerData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StylePickerActivity extends Activity {
    public static final String SELECTED_STYLE_EXTRA = "selected_style";
    public static final String SELECTED_ITEM_EXTRA = "selected_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.style_picker);

        final ExpandableListView list = (ExpandableListView) findViewById(R.id.style_picker_style_list);
        final StylePickerAdapter adapter = new StylePickerAdapter();

        list.setAdapter(adapter);

        list.setOnGroupExpandListener(new OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition) {
                int len = adapter.getGroupCount();

                for (int i = 0; i < len; i++) {
                    if (i != groupPosition) {
                        list.collapseGroup(i);
                    }
                }
            }
        });


        if (getIntent().getExtras() != null) {
            PlaylistEntry selected = (PlaylistEntry) getIntent().getExtras().getParcelable(SELECTED_ITEM_EXTRA);

            if (selected != null) {
                int group = adapter.getItemGroupFromId(selected.style);

                if (group >= 0) {
                    list.expandGroup(group);
                } else {
                    list.expandGroup(0);
                }
            } else {
                finish();
            }
        }

        list.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                StyleEntry sel = (StyleEntry) v.getTag();

                Intent data = getIntent();
                data.putExtra(SELECTED_STYLE_EXTRA, sel.id);
                setResult(RESULT_OK, data);
                finish();

                return true;
            }
        });
    }

    @SuppressLint("UseSparseArrays")
    public class StylePickerAdapter extends BaseExpandableListAdapter {
        private Map<Integer, List<StyleEntry>> _styles = new HashMap<Integer, List<StyleEntry>>();

        public StylePickerAdapter() {
            StyleManagerData data = StyleManager.LoadData(StylePickerActivity.this);
            _styles.put(0, data.apk);
            _styles.put(1, data.sd);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return _styles.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View ret;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) StylePickerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ret = inflater.inflate(R.layout.style_picker_list_entry, null);
            } else {
                ret = convertView;
            }

            StyleEntry cur = _styles.get(groupPosition).get(childPosition);

            TextView style = (TextView) ret.findViewById(R.id.style_picker_entry_style_name);

            style.setText(cur.name);

            ret.setTag(cur);

            return ret;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return _styles.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return _styles.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return _styles.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View ret;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) StylePickerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ret = inflater.inflate(R.layout.style_picker_list_group, null);
            } else {
                ret = convertView;
            }

            String group_name = "";

            switch (groupPosition) {
                case 0:
                    group_name = StylePickerActivity.this.getString(R.string.style_picker_default_style);
                    break;

                case 1:
                    group_name = StylePickerActivity.this.getString(R.string.style_picker_custom_style);
                    break;
            }

            TextView group = (TextView) ret.findViewById(R.id.style_picker_list_group_name);
            group.setText(group_name);

            return ret;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public int[] getItemPositionFromName(String item) {
            Set<Integer> keys = _styles.keySet();

            int[] ret = new int[]{-1, -1};

            for (Integer i : keys) {
                List<StyleEntry> list = _styles.get(i);
                ret[1] = 0;

                for (StyleEntry se : list) {
                    if (se.name.equals(item)) {
                        ret[0] = i;

                        return ret;
                    }

                    ret[1]++;
                }
            }

            ret[0] = ret[1] = -1;

            return ret;
        }

        public int getItemGroupFromId(String item) {
            Set<Integer> keys = _styles.keySet();

            int ret = -1;

            for (Integer i : keys) {
                List<StyleEntry> list = _styles.get(i);

                for (StyleEntry se : list) {
                    if (se.id.equals(item)) {
                        ret = i;

                        return ret;
                    }
                }
            }

            return -1;
        }

        public int getItemGroupFromName(String item) {
            Set<Integer> keys = _styles.keySet();

            int ret = -1;

            for (Integer i : keys) {
                List<StyleEntry> list = _styles.get(i);

                for (StyleEntry se : list) {
                    if (se.name.equals(item)) {
                        ret = i;

                        return ret;
                    }
                }
            }

            return -1;
        }
    }
}
