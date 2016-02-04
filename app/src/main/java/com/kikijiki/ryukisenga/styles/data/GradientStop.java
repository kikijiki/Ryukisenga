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

package com.kikijiki.ryukisenga.styles.data;

import android.graphics.Color;

import java.io.Serializable;

public class GradientStop implements Serializable {
    private static final long serialVersionUID = 3013760917946709105L;

    public float offset = .0f;
    public int color = Color.BLACK;

    public GradientStop(float off, int col, float a) {
        this.offset = off;

        int alpha = (int) (a * 255.0f);

        this.color = (col & 0x00ffffff) | (alpha << 24);
    }

    public GradientStop(float off, int col) {
        this.offset = off;
        this.color = col;
    }
}