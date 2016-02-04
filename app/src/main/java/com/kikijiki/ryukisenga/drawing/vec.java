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

package com.kikijiki.ryukisenga.drawing;

import android.graphics.Matrix;
import android.util.Log;

import java.util.Random;

public class vec {
    public float x;
    public float y;

    public vec() {
        reset();
    }

    public vec(Random r, float mag) {
        float a = r.nextFloat() * (float) Math.PI * 2;

        x = (float) (mag * Math.cos(a));
        y = (float) (mag * Math.sin(a));
    }

    public vec(Random r) {
        this(r, 1.0f);
    }

    public vec(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public vec(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public vec(vec v) {
        x = v.x;
        y = v.y;
    }

    public vec(dvec v) {
        x = (float) v.x;
        y = (float) v.y;
    }

    public void reset() {
        x = .0f;
        y = .0f;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public void log() {
        Log.d("vec", toString());
    }

    public vec clone() {
        return new vec(x, y);
    }

    public void copy(vec v) {
        x = v.x;
        y = v.y;
    }

    public vec add(vec v) {
        return new vec(x + v.x, y + v.y);
    }

    public vec sub(vec v) {
        return new vec(x - v.x, y - v.y);
    }

    public void swap() {
        float tmp = x;
        x = y;
        y = tmp;
    }

    public vec mul(float s) {
        return new vec(x * s, y * s);
    }

    public vec div(float s) {
        vec ret = new vec();

        if (s > Float.MIN_VALUE) {
            ret.x = x / s;
            ret.y = y / s;
        } else {
            ret.x = Float.MAX_VALUE;
            ret.y = Float.MAX_VALUE;
        }

        return ret;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public vec versor() {
        float n = length();
        vec ret = this.clone();

        if (n > Float.MIN_VALUE) {
            ret.x = x / n;
            ret.y = y / n;
        } else {
            if (Math.abs(x) > Math.abs(y)) {
                ret.x = Math.signum(x);
                ret.y = .0f;
            } else {
                ret.x = .0f;
                ret.y = Math.signum(y);
            }
        }

        return ret;
    }

    public boolean isNan() {
        return Float.isNaN(x) || Float.isNaN(y);
    }

    public boolean isInfinite() {
        return Float.isInfinite(x) || Float.isInfinite(y);
    }

    public vec transform(Matrix m) {
        float c[] = new float[]{x, y};
        m.mapPoints(c);

        x = c[0];
        y = c[1];

        return this;
    }
}