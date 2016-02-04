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

package com.kikijiki.ryukisenga.styles;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.kikijiki.ryukisenga.R;
import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.drawing.vec;
import com.kikijiki.ryukisenga.styles.data.Style;
import com.kikijiki.ryukisenga.styles.data.StyleData;
import com.kikijiki.ryukisenga.styles.data.StyleDataContentHandler;
import com.kikijiki.ryukisenga.styles.data.StyleNameContentHandler;
import com.kikijiki.ryukisenga.styles.data.StyleSerializer;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StyleManager {
    public static final String UNKNOWN_STYLE_NAME = "Unknown style";
    public static final String DEFAULT_STYLE = "ryuki";
    private static final String SEPARATOR = ";";
    private static final String CUSTOM_STYLE_DEFAULT_NAME = "custom_style_";
    private static final String CUSTOM_STYLE_DEFAULT_EXTENSION = ".xml";

    private static void load(Context c, StyleManagerData data) {
        enumDefault(c, data);
        enumCustom(data);
    }

    private static void enumCustom(StyleManagerData data) {
        File[] fileList = Assets.sdOpenStyleDirectory().listFiles();

        if (fileList == null || fileList.length == 0)
            return;

        for (File f : fileList) {
            if (f.isFile()) {
                String fileName = f.getName();
                String name = fileName.substring(0, fileName.lastIndexOf("."));

                StyleEntry entry = new StyleEntry();
                entry.file = fileName;
                entry.isCustom = true;
                entry.id = name;

                StyleNameContentHandler smch = new StyleNameContentHandler(entry);

                InputStream is = Assets.sdOpenStyle(entry.file);

                try {
                    Xml.parse(is, Encoding.UTF_8, smch);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    //e.printStackTrace();
                }

                if (entry.name != null) {
                    entry.id = entry.name;
                    addStyle(data, entry);
                }
            }
        }
    }

    private static void enumDefault(Context c, StyleManagerData data) {
        String[] list = Assets.apkLlistStyles(c);

        String[] name_array = c.getResources().getStringArray(R.array.style_names);
        String[][] names = new String[name_array.length][];
        int i = 0;

        for (String n : name_array) {
            names[i] = n.split(SEPARATOR);
            i++;
        }

        for (String element : list) {
            StyleEntry entry = new StyleEntry();
            entry.file = element;
            entry.isCustom = false;
            entry.id = element.substring(0, element.lastIndexOf("."));
            entry.name = entry.id;

            for (String[] n : names) {
                if (n[0].equalsIgnoreCase(entry.id)) {
                    entry.name = n[1];
                    break;
                }
            }

            addStyle(data, entry);
        }
    }

    private static Style loadStyle(Context c, StyleManagerData data, String style, vec scrSize) {
        Style ret = null;

        StyleEntry entry = data.styles.get(style);

        if (entry != null) {
            if (entry.isCustom) {
                ret = loadCustomStyle(c, entry, scrSize);
            } else {
                ret = loadDefaultStyle(c, entry, scrSize);
            }
        } else {
            ret = loadStyle(c, data, DEFAULT_STYLE, scrSize);
        }

        return ret;
    }

    private static Style loadDefaultStyle(Context c, StyleEntry entry, vec scrSize) {
        Style ret = new Style();

        StyleData data = new StyleData();
        StyleDataContentHandler sch = new StyleDataContentHandler(data);

        InputStream is = Assets.apkOpenStyle(c, entry.file);

        try {
            Xml.parse(is, Encoding.UTF_8, sch);
            ret = data.computeStyle(c, scrSize.x, scrSize.y);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static StyleData loadDefaultStyleData(Context c, StyleEntry entry) {
        StyleData ret = new StyleData();

        StyleDataContentHandler sch = new StyleDataContentHandler(ret);

        InputStream is = Assets.apkOpenStyle(c, entry.file);

        try {
            Xml.parse(is, Encoding.UTF_8, sch);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static void addStyle(StyleManagerData data, StyleEntry se) {
        data.styles.put(se.id, se);

        if (se.isCustom) {
            data.sd.add(se);
        } else {
            data.apk.add(se);
        }
    }

    private static Style loadCustomStyle(Context c, StyleEntry entry, vec scrSize) {
        Style ret = new Style();

        StyleData data = new StyleData();

        InputStream is = Assets.sdOpenStyle(entry.file);
        StyleDataContentHandler sch = new StyleDataContentHandler(data);

        try {
            Xml.parse(is, Encoding.UTF_8, sch);
            ret = data.computeStyle(c, scrSize.x, scrSize.y);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String GetStyleName(StyleManagerData data, String styleId) {
        if (styleId == null)
            return GetStyleName(data, DEFAULT_STYLE);

        StyleEntry style = data.styles.get(styleId);

        if (style == null)
            return GetStyleName(data, DEFAULT_STYLE);

        String style_name = style.name;

        if (style_name != null) {
            return style_name;
        } else {
            return GetStyleName(data, DEFAULT_STYLE);
        }
    }

    private static String generateStyleDefaultName() {
        String ret = null;

        File sdroot = Environment.getExternalStorageDirectory();

        File dir = new File(sdroot, Assets.SD_CUSTOM_STYLES_DIRECTORY);

        int id = -1;

        File[] fileList = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();

                if (!name.startsWith(CUSTOM_STYLE_DEFAULT_NAME))
                    return false;

                if (!name.endsWith(CUSTOM_STYLE_DEFAULT_EXTENSION))
                    return false;

                return true;
            }
        });

        if (fileList != null && fileList.length > 0) {
            for (File f : fileList) {
                String name = f.getName();

                int start = name.indexOf(CUSTOM_STYLE_DEFAULT_NAME) + CUSTOM_STYLE_DEFAULT_NAME.length();
                int end = name.lastIndexOf(CUSTOM_STYLE_DEFAULT_EXTENSION);

                if (start < 0 || end < 0 || start >= end)
                    continue;

                String n = name.substring(start, end);
                int current_id = -1;

                try {
                    current_id = Integer.parseInt(n);
                } catch (NumberFormatException e) {
                    current_id = -1;
                }

                id = Math.max(id, current_id);
            }
        }

        id++;

        ret = CUSTOM_STYLE_DEFAULT_NAME + id + CUSTOM_STYLE_DEFAULT_EXTENSION;

        return ret;
    }

    public static StyleData loadCustomStyleData(Context c, StyleManagerData data, String name) {
        StyleData ret = new StyleData();

        if (name == null) //Load default style
        {
            StyleEntry default_style = data.styles.get(DEFAULT_STYLE);

            ret = loadDefaultStyleData(c, default_style);

            ret.info = new StyleEntry();
            ret.info.name = name;
            ret.info.file = null;
            ret.info.isCustom = true;
            ret.info.id = name;
        } else {
            ret.info = data.styles.get(name);

            if (ret.info != null) {
                try {
                    StyleDataContentHandler sdch = new StyleDataContentHandler(ret);
                    InputStream is = Assets.sdOpenStyle(ret.info.file);

                    Xml.parse(is, Encoding.UTF_8, sdch);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    private static StyleData LoadStyleDataFromFile(String filename) {
        StyleData ret = new StyleData();

        try {
            StyleDataContentHandler sdch = new StyleDataContentHandler(ret);
            InputStream is = Assets.sdOpenStyle(filename);

            Xml.parse(is, Encoding.UTF_8, sdch);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ret.info.file = filename;
        ret.info.isCustom = true;
        ret.info.id = ret.info.name;

        return ret;
    }

    private static void saveStyleData(StyleData style) {
        if (style.info.file == null) {
            style.info.file = generateStyleDefaultName();
        }

        StyleSerializer.save(style);
    }

    public static StyleEntry getStyleInfo(StyleManagerData data, String name) {
        return data.styles.get(name);
    }

    private static void RemoveStyle(StyleEntry style) {
        Assets.sdRemoveStyle(style.file);
    }

    public static Style LoadStyle(Context c, String name, vec viewport) {
        StyleManagerData data = new StyleManagerData();
        load(c, data);
        return loadStyle(c, data, name, viewport);
    }

    public static StyleData LoadStyleData(Context c, String name) {
        StyleManagerData data = new StyleManagerData();
        load(c, data);
        return loadCustomStyleData(c, data, name);
    }

    public static void SaveStyleData(Context c, StyleData style) {
        saveStyleData(style);
    }

    public static void RemoveStyle(Context c, StyleEntry style) {
        RemoveStyle(style);
    }

    public static StyleData LoadStyleDataFromFile(Context c, String filename) {
        return LoadStyleDataFromFile(filename);
    }

    public static boolean isStyleNameAvailable(Context c, String name) {
        StyleManagerData data = new StyleManagerData();
        load(c, data);
        return !data.styles.containsKey(name);
    }

    public static StyleManagerData LoadData(Context c) {
        StyleManagerData data = new StyleManagerData();
        load(c, data);
        return data;
    }

    public static class StyleManagerData {
        Map<String, StyleEntry> styles = new HashMap<String, StyleEntry>();
        ArrayList<StyleEntry> apk = new ArrayList<StyleEntry>();
        ArrayList<StyleEntry> sd = new ArrayList<StyleEntry>();
    }

    public static class StyleEntry implements Serializable {
        private static final long serialVersionUID = -1095687305245273616L;

        public boolean isCustom = false;
        public String file;
        public String name;
        public String id;
    }
}
