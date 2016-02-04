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

import android.graphics.Paint;

public class Style {
    public Paint line = new Paint();
    public Paint trail = new Paint();
    public Paint background = new Paint();

    public void adjustScaling(float invScale) {
        float swidth = line.getStrokeWidth() * invScale;

        line.setStrokeWidth(swidth);

        if (!line.equals(trail)) {
            swidth = trail.getStrokeWidth() * invScale;
            trail.setStrokeWidth(swidth);
        }
    }
}
