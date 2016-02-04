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

package com.kikijiki.ryukisenga.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.kikijiki.ryukisenga.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class ColorPickerDialog {
    private AlertDialog dialog;
    private ColorPickerListener listener;
    private ColorPicker picker;
    public ColorPickerDialog(Context context, int color, ColorPickerListener listener) {
        this.listener = listener;
        View view = LayoutInflater.from(context).inflate(R.layout.activity_color_picker, null);
        picker = (ColorPicker) view.findViewById(R.id.picker);
        picker.setColor(color);
        picker.setOldCenterColor(color);
        picker.setShowOldCenterColor(true);

        SVBar svBar = (SVBar) view.findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);

        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ColorPickerDialog.this.listener.onOk(ColorPickerDialog.this, picker.getColor());
                    }
                })
                .create();
        dialog.setView(view);
    }

    public void show() {
        dialog.show();
    }

    public interface ColorPickerListener {
        void onCancel(ColorPickerDialog dialog);

        void onOk(ColorPickerDialog dialog, int color);
    }
}
