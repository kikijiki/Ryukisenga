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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerArrayAdapter extends ArrayAdapter<String> {
    private Context _c;

    private int[] _res = new int[4];

    public SpinnerArrayAdapter(Context context, int resource, int textViewResourceId, int dropDownResource, int dropDownTextViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);

        _c = context;

        _res[0] = resource;
        _res[1] = textViewResourceId;
        _res[2] = dropDownResource;
        _res[3] = dropDownTextViewResourceId;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(_c).inflate(_res[2], null);
        }

        TextView text = (TextView) convertView.findViewById(_res[3]);
        text.setText(this.getItem(position));

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(_c).inflate(_res[0], null);
        }

        TextView text = (TextView) convertView.findViewById(_res[1]);
        text.setText(this.getItem(position));

        return convertView;
    }

}
