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

package com.kikijiki.ryukisenga;

public class Uti {
    public static float safeParseFloat(String value) {
        if (value == null || value.length() == 0)
            return .0f;

        float ret;
        float tmp = .0f;

        try {
            tmp = Float.parseFloat(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            ret = tmp;
        }

        return ret;
    }

    public static float safeParseFloat(String value, float def) {
        if (value == null || value.length() == 0)
            return def;

        float ret;
        float tmp = def;

        try {
            tmp = Float.parseFloat(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            ret = tmp;
        }

        return ret;
    }

    public static Float safeParseNullableFloat(String value) {
        if (value == null || value.length() == 0)
            return null;

        Float ret;
        float tmp = .0f;

        try {
            tmp = Float.parseFloat(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            ret = tmp;
        }

        return ret;
    }
}
