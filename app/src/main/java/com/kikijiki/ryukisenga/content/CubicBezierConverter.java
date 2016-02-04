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

public class CubicBezierConverter implements CubicBezierFlattener {
    private final static int _recursion_limit = 16;
    private static double _curve_collinearity_epsilon = 1e-8;
    private static double _curve_angle_tolerance_epsilon = 0.01;
    private static double _distance_tolerance_square = 0.25; ////
    private static double _angle_tolerance = 0.1; ////
    private static double _cusp_limit = 0.0; ////

    public CubicBezierConverter(float invScale) {
        if (invScale < 1.0f) {
            _curve_angle_tolerance_epsilon *= invScale;
            _distance_tolerance_square *= invScale;
            _angle_tolerance *= invScale;
        }
    }

    private static void recursiveCall(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, int depth, PointBuffer buf) {
        if (depth > _recursion_limit) {
            return;
        }

        double x12 = (x1 + x2) / 2;
        double y12 = (y1 + y2) / 2;
        double x23 = (x2 + x3) / 2;
        double y23 = (y2 + y3) / 2;
        double x34 = (x3 + x4) / 2;
        double y34 = (y3 + y4) / 2;
        double x123 = (x12 + x23) / 2;
        double y123 = (y12 + y23) / 2;
        double x234 = (x23 + x34) / 2;
        double y234 = (y23 + y34) / 2;
        double x1234 = (x123 + x234) / 2;
        double y1234 = (y123 + y234) / 2;

        double dx = x4 - x1;
        double dy = y4 - y1;

        double d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
        double d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
        double da1, da2, k;

        if (d3 > _curve_collinearity_epsilon) {
            if (d2 > _curve_collinearity_epsilon) {
                if ((d2 + d3) * (d2 + d3) <= _distance_tolerance_square * (dx * dx + dy * dy)) {
                    if (_angle_tolerance < _curve_angle_tolerance_epsilon) {
                        buf.append(x23, y23);//, d2 + d3);
                        return;
                    }

                    k = Math.atan2(y3 - y2, x3 - x2);
                    da1 = Math.abs(k - Math.atan2(y2 - y1, x2 - x1));
                    da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - k);
                    if (da1 >= Math.PI) da1 = 2 * Math.PI - da1;
                    if (da2 >= Math.PI) da2 = 2 * Math.PI - da2;

                    if (da1 + da2 < _angle_tolerance) {
                        buf.append(x23, y23);
                        return;
                    }

                    if (_cusp_limit <= Double.MIN_VALUE) {
                        if (da1 > _cusp_limit) {
                            buf.append(x2, y2);
                            return;
                        }

                        if (da2 > _cusp_limit) {
                            buf.append(x3, y3);
                            return;
                        }
                    }
                }
            } else {
                if (d3 * d3 <= _distance_tolerance_square * (dx * dx + dy * dy)) {
                    if (_angle_tolerance < _curve_angle_tolerance_epsilon) {
                        buf.append(x23, y23);
                        return;
                    }

                    da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));

                    if (da1 >= Math.PI) {
                        da1 = 2 * Math.PI - da1;
                    }

                    if (da1 < _angle_tolerance) {
                        buf.append(x2, y2);
                        buf.append(x3, y3);
                        return;
                    }

                    if (_cusp_limit <= Double.MIN_VALUE) {
                        if (da1 > _cusp_limit) {
                            buf.append(x3, y3);
                            return;
                        }
                    }
                }
            }
        } else {
            if (d2 > _curve_collinearity_epsilon) {
                if (d2 * d2 <= _distance_tolerance_square * (dx * dx + dy * dy)) {
                    if (_angle_tolerance < _curve_angle_tolerance_epsilon) {
                        buf.append(x23, y23);
                        return;
                    }

                    da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                    if (da1 >= Math.PI) da1 = 2 * Math.PI - da1;

                    if (da1 < _angle_tolerance) {
                        buf.append(x2, y2, d2);
                        buf.append(x3, y3, d3);
                        return;
                    }

                    if (_cusp_limit <= Double.MIN_VALUE) {
                        if (da1 > _cusp_limit) {
                            buf.append(x2, y2);
                            return;
                        }
                    }
                }
            } else {
                k = dx * dx + dy * dy;
                if (k == 0) {
                    d2 = lengthSquare(x1, y1, x2, y2);
                    d3 = lengthSquare(x4, y4, x3, y3);
                } else {
                    k = 1 / k;
                    da1 = x2 - x1;
                    da2 = y2 - y1;
                    d2 = k * (da1 * dx + da2 * dy);
                    da1 = x3 - x1;
                    da2 = y3 - y1;
                    d3 = k * (da1 * dx + da2 * dy);

                    if (d2 > 0 && d2 < 1 && d3 > 0 && d3 < 1) {
                        return;
                    }
                    if (d2 <= 0) d2 = lengthSquare(x2, y2, x1, y1);
                    else if (d2 >= 1) d2 = lengthSquare(x2, y2, x4, y4);
                    else d2 = lengthSquare(x2, y2, x1 + d2 * dx, y1 + d2 * dy);

                    if (d3 <= 0) d3 = lengthSquare(x3, y3, x1, y1);
                    else if (d3 >= 1) d3 = lengthSquare(x3, y3, x4, y4);
                    else d3 = lengthSquare(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
                }
                if (d2 > d3) {
                    if (d2 < _distance_tolerance_square) {
                        buf.append(x2, y2);
                        return;
                    }
                } else {
                    if (d3 < _distance_tolerance_square) {
                        buf.append(x3, y3);
                        return;
                    }
                }
            }
        }

        recursiveCall(x1, y1, x12, y12, x123, y123, x1234, y1234, depth + 1, buf);
        recursiveCall(x1234, y1234, x234, y234, x34, y34, x4, y4, depth + 1, buf);
    }

    private static double lengthSquare(double x1, double y1, double x2, double y2) {
        return Math.sqrt(x1 * x1 + x2 * x2);
    }

    public void convert(boolean first, float[] p, PointBuffer buf) {
        if (first)
            buf.append(p[0], p[1]);

        recursiveCall(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], 0, buf);

        buf.append(p[6], p[7]);
    }
}