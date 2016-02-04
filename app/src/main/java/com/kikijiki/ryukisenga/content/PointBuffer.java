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

package com.kikijiki.ryukisenga.content;

import android.graphics.Matrix;
import android.util.FloatMath;

import java.util.ArrayList;

public class PointBuffer {
    public static final float epsilon = 1.0e-20f;

    public ArrayList<Float> pt = new ArrayList<Float>();
    public ArrayList<Float> dl = new ArrayList<Float>();
    public float len = .0f;
    //public RectF bb = new RectF();

    public boolean isEmpty() {
        return pt.size() == 0;
    }

    public void close() {
        append(pt.get(0), pt.get(1));
    }

    public void append(float x, float y, float l) {
        if (l > epsilon) {
            pt.add(x);
            pt.add(y);
            //updateBB(x, y);
            dl.add(l);
            len += l;
        }
    }

    public void append(float x, float y, Matrix m) {
        float[] data = {x, y};

        m.mapPoints(data);

        append(data[0], data[1]);
    }

    public void append(double x, double y, double l) {
        append((float) x, (float) y, (float) l);
    }

    public void append(float x, float y) {
        if (pt.size() < 2) {
            pt.add(x);
            pt.add(y);
            //updateBB(x, y);
            len = .0f;
        } else {
            int i = pt.size() - 2;

            float dx = x - pt.get(i);
            float dy = y - pt.get(i + 1);
            float l = FloatMath.sqrt(dx * dx + dy * dy);

            if (l > epsilon) {
                pt.add(x);
                pt.add(y);
                //updateBB(x, y);
                dl.add(l);
                len += l;
            }
        }
    }

//	private void updateBB(float x, float y)
//	{
//		if(x < bb.left)
//			bb.left = x;
//		else if(x > bb.right)
//			bb.right = x;
//		
//		if(y < bb.top)
//			bb.top = y;
//		else if(y > bb.bottom) 
//			bb.bottom = y;
//	}

    public void append(double x, double y) {
        append((float) x, (float) y);
    }
}