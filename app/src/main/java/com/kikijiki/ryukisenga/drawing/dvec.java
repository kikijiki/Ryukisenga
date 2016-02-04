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

import android.util.Log;

import java.util.Random;

public class dvec {
    public double x;
    public double y;

    public dvec() {
        reset();
    }

    public dvec(Random r, double mag) {
        double a = r.nextDouble() * Math.PI * 2;

        x = mag * Math.cos(a);
        y = mag * Math.sin(a);
    }

    public dvec(Random r) {
        this(r, 1.0f);
    }

    public dvec(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public dvec(dvec v) {
        x = v.x;
        y = v.y;
    }

    public void reset() {
        x = .0f;
        y = .0f;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public void log() {
        Log.d("sen", toString());
    }

    public dvec clone() {
        return new dvec(x, y);
    }

    public void copy(dvec v) {
        x = v.x;
        y = v.y;
    }

    public dvec add(dvec v) {
        return new dvec(x + v.x, y + v.y);
    }

    public dvec sub(dvec v) {
        return new dvec(x - v.x, y - v.y);
    }

    public void swap() {
        double tmp = x;
        x = y;
        y = tmp;
    }

    public dvec mul(double s) {
        return new dvec(x * s, y * s);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
}