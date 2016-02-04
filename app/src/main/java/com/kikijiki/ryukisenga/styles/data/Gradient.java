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

package com.kikijiki.ryukisenga.styles.data;

import android.graphics.LinearGradient;
import android.graphics.Shader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Gradient implements Serializable {
    private static final long serialVersionUID = 5174731127550760763L;

    public ArrayList<GradientStop> stop = new ArrayList<GradientStop>();

    public float angle;

    public boolean uniform = false;

    public Shader computeShader(float sizeX, float sizeY) {
        if (stop.size() < 2) {
            return null;
        }

        int stop_count = stop.size();

        int[] sorted_colors = new int[stop_count];
        float[] sorted_offsets = new float[stop_count];

        final float[] offsets = new float[stop_count];
        final int colors[] = new int[stop_count];

        Integer[] indices = new Integer[stop_count];

        for (int i = 0; i < stop_count; i++) {
            colors[i] = stop.get(i).color;
            offsets[i] = stop.get(i).offset;
            indices[i] = i;
        }

        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return offsets[rhs] > offsets[lhs] ? -1 : 1;
            }
        });

        for (int i = 0; i < stop_count; i++) {
            sorted_offsets[i] = offsets[indices[i]];
            sorted_colors[i] = colors[indices[i]];
        }

        float w = sizeX;
        float h = sizeY;
        double w2 = w * .5f;
        double h2 = h * .5f;

        float fromX = (float) w2;
        float fromY = (float) h2;
        float toX = (float) w2;
        float toY = (float) h2;

        double pi = Math.PI;
        double pi2 = pi * .5f;

        if (angle > pi2) {
            double a = pi - angle;

            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;

            double q = h2 - mp * w2;
            double l = Math.max(h2, Math.abs(q) / Math.sqrt(1 + mp * mp));

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX += l * c;
            fromY += l * s;
            toX -= l * c;
            toY -= l * s;
        } else if (angle > 0) {
            double a = angle;
            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;
            double q = h2 - mp * w2;
            double l = Math.max(h2, Math.abs(q) / Math.sqrt(1 + mp * mp));

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX -= l * c;
            fromY += l * s;
            toX += l * c;
            toY -= l * s;
        } else if (angle > -pi2) {
            double a = -angle;
            double s = -Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;
            double q = h2 - mp * w2;
            double l = Math.abs(q) / Math.sqrt(1 + mp * mp);

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX -= l * c;
            fromY += l * s;
            toX += l * c;
            toY -= l * s;
        } else {
            double a = pi + angle;

            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = (float) Math.tan(a);
            double mp = -1.0f / m;

            double q = h2 - mp * w2;
            double l = Math.abs(q) / Math.sqrt(1 + mp * mp);

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX += l * c;
            fromY -= l * s;
            toX -= l * c;
            toY += l * s;
        }

        Shader gradient = new LinearGradient(
                fromX,
                fromY,
                toX,
                toY,
                sorted_colors,
                uniform ? null : sorted_offsets,
                Shader.TileMode.CLAMP);

        return gradient;
    }

    public Shader[] computePreviewShaders(float w, float h, int start, int end) {
        int stop_count = stop.size();

        int[] sorted_colors = new int[stop_count + 2];
        float[] sorted_offsets = new float[stop_count + 2];

        final float[] offsets = new float[stop_count + 2];
        final int colors[] = new int[stop_count + 2];

        Integer[] indices = new Integer[stop_count + 2];

        colors[0] = start;
        colors[stop_count + 1] = end;
        offsets[0] = .0f;
        offsets[stop_count + 1] = 1.0f;
        indices[0] = 0;
        indices[stop_count + 1] = stop_count + 1;

        for (int i = 0; i < stop_count; i++) {
            colors[i + 1] = stop.get(i).color;
            offsets[i + 1] = stop.get(i).offset;
            indices[i + 1] = i + 1;
        }

        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return offsets[rhs] > offsets[lhs] ? -1 : 1;
            }
        });

        for (int i = 0; i < stop_count + 2; i++) {
            sorted_offsets[i] = offsets[indices[i]];
            sorted_colors[i] = colors[indices[i]];
        }

        double w2 = w * .5f;
        double h2 = h * .5f;

        float fromX = (float) w2;
        float fromY = (float) h2;
        float toX = (float) w2;
        float toY = (float) h2;

        double pi = Math.PI;
        double pi2 = pi * .5f;

        if (angle > pi2) {
            double a = pi - angle;

            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;

            double q = h2 - mp * w2;
            double l = Math.max(h2, Math.abs(q) / Math.sqrt(1 + mp * mp));

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX += l * c;
            fromY += l * s;
            toX -= l * c;
            toY -= l * s;
        } else if (angle > 0) {
            double a = angle;
            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;
            double q = h2 - mp * w2;
            double l = Math.max(h2, Math.abs(q) / Math.sqrt(1 + mp * mp));

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX -= l * c;
            fromY += l * s;
            toX += l * c;
            toY -= l * s;
        } else if (angle > -pi2) {
            double a = -angle;
            double s = -Math.sin(a);
            double c = Math.cos(a);

            double m = Math.tan(a);
            double mp = -1.0f / m;
            double q = h2 - mp * w2;
            double l = Math.abs(q) / Math.sqrt(1 + mp * mp);

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX -= l * c;
            fromY += l * s;
            toX += l * c;
            toY -= l * s;
        } else {
            double a = pi + angle;

            double s = Math.sin(a);
            double c = Math.cos(a);

            double m = (float) Math.tan(a);
            double mp = -1.0f / m;

            double q = h2 - mp * w2;
            double l = Math.abs(q) / Math.sqrt(1 + mp * mp);

            if (Double.isNaN(l)) {
                l = w2;
            }

            fromX += l * c;
            fromY -= l * s;
            toX -= l * c;
            toY += l * s;
        }

        Shader gradient = new LinearGradient(
                fromX,
                fromY,
                toX,
                toY,
                sorted_colors,
                uniform ? null : sorted_offsets,
                Shader.TileMode.CLAMP);

        Shader flat_gradient = new LinearGradient(
                0,
                0,
                w,
                0,
                sorted_colors,
                uniform ? null : sorted_offsets,
                Shader.TileMode.CLAMP);

        return new Shader[]{gradient, flat_gradient};
    }

    public void addStop(Float off, int col) {
        if (off == null) {
            uniform = true;
            stop.add(new GradientStop(.0f, col));
        } else {
            stop.add(new GradientStop(off, col));
        }
    }

    public void addStop(Float off, int col, float a) {
        if (off == null)
            uniform = true;

        stop.add(new GradientStop(off, col, a));
    }

    public Gradient clone() {
        Gradient ret = new Gradient();


        ret.angle = angle;
        ret.uniform = uniform;
        ret.stop.addAll(stop);

        return ret;
    }

    public void loadDefaultGradient() {
        angle = (float) (-Math.PI * .25D);
        uniform = true;
        stop.clear();
        addStop(null, StyleData.DEFAULT_GRADIENT_COLOR_0);
        addStop(null, StyleData.DEFAULT_GRADIENT_COLOR_1);
    }

    public void addExtremes(int start, int end) {
        stop.add(0, new GradientStop(.0f, start));
        stop.add(stop.size(), new GradientStop(1.0f, end));
    }
}