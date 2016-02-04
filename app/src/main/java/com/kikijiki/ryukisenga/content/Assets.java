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

package com.kikijiki.ryukisenga.content;

import android.content.Context;
import android.os.Environment;

import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry;
import com.kikijiki.ryukisenga.styles.data.StyleData;

import org.xml.sax.Attributes;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Assets {
    public final static String GALLERY_DIRECTORY = "gallery";
    public final static String VECTORS_DIRECTORY = "vectors";
    public final static String ARTISTS_DIRECTORY = "artists";
    public final static String ARTISTS_PATH = GALLERY_DIRECTORY + File.separator + ARTISTS_DIRECTORY;
    public final static String PENS_DIRECTORY = "pens";
    public final static String PENS_PATH = GALLERY_DIRECTORY + File.separator + PENS_DIRECTORY;
    public final static String TILES_DIRECTORY = "tiles";
    public final static String TILES_PATH = GALLERY_DIRECTORY + File.separator + TILES_DIRECTORY;
    public final static String STYLES_DIRECTORY = "styles";
    public final static String STYLES_PATH = GALLERY_DIRECTORY + File.separator + STYLES_DIRECTORY;

    public final static String VECTOR_FILE_EXTENSION = ".svg";
    public final static String ARTIST_INFO_FILE = "info.xml";
    public final static String VECTOR_INFO_FILE = "info.xml";
    public final static String ARTIST_ICON_FILE = "icon.png";
    public final static String VECTOR_ICON_FILE = "icon.png";

    public static final String PREVIEW_SEQUENCE_PATH = "app/style_editor/preview";
    public static final String PREVIEW_SEQUENCE_PATH_LANDSCAPE = "app/style_editor/preview-land";

    public final static String SD_DIRECTORY = "RyukiSenga";
    public final static String SD_CUSTOM_STYLES_DIRECTORY = SD_DIRECTORY + File.separator + STYLES_DIRECTORY;
    public final static String SD_CUSTOM_GALLERY_DIRECTORY = SD_DIRECTORY + File.separator + "gallery";

    public static final String SD_CACHE_PATH = SD_DIRECTORY + File.separator + "cache";
    public static final String SD_CACHE_USER_GALLERY_DIRECTORY = "svg";

    public static final String CACHE_POSTFIX = ".thumb";

    public static String checkPathForAppend(String path) {
        if (path == null)
            path = "";

        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separator;
        }
    }

    public static String apkArtistPath(String artist) {
        return ARTISTS_PATH + File.separator + artist;
    }

    public static String apkArtistVectorsPath(String artist) {
        return ARTISTS_PATH + File.separator + artist + File.separator + VECTORS_DIRECTORY;
    }

    public static String apkVectorPath(String artist, String vectorDirectory) {
        return ARTISTS_PATH + File.separator + artist + File.separator + VECTORS_DIRECTORY + File.separator + vectorDirectory;
    }

    public static String apkTilePath(String tile) {
        return TILES_PATH + File.separator + tile;
    }

    public static String apkStylePath(String style) {
        return STYLES_PATH + File.separator + style;
    }

    public static boolean apkCheckVector(Context c, String path) {
        String[] list = null;

        try {
            list = c.getAssets().list(path);
        } catch (IOException e) {
        }

        if (list == null || list.length == 0)
            return false;

        return true;
    }

    public static InputStream apkOpenAsset(Context c, String path) {
        InputStream ret = null;

        try {
            ret = c.getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static InputStream apkOpenArtistInfo(Context c, String artist) {
        return apkOpenAsset(c, apkArtistPath(artist) + File.separator + ARTIST_INFO_FILE);
    }

    public static InputStream apkOpenArtistInfoFromPath(Context c, String path) {
        return apkOpenAsset(c, checkPathForAppend(path) + ARTIST_INFO_FILE);
    }

    public static InputStream apkOpenArtistIcon(Context c, String artist) {
        return apkOpenAsset(c, apkArtistPath(artist) + File.separator + ARTIST_ICON_FILE);
    }

    public static InputStream apkOpenArtistIconFromPath(Context c, String path) {
        return apkOpenAsset(c, checkPathForAppend(path) + ARTIST_ICON_FILE);
    }

    public static InputStream apkOpenVectorInfo(Context c, String artist, String vectorDirectory) {
        return apkOpenAsset(c, apkVectorPath(artist, vectorDirectory) + File.separator + VECTOR_INFO_FILE);
    }

    public static InputStream apkOpenVectorInfoFromPath(Context c, String path) {
        return apkOpenAsset(c, checkPathForAppend(path) + VECTOR_INFO_FILE);
    }

    public static InputStream apkOpenVectorData(Context c, String artist, String vectorDirectory) {
        InputStream ret = null;

        try {
            String[] files = c.getAssets().list(apkVectorPath(artist, vectorDirectory));

            for (String file : files) {
                if (file.endsWith(VECTOR_FILE_EXTENSION)) {
                    ret = c.getAssets().open(apkVectorPath(artist, vectorDirectory) + file);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static InputStream sdOpenVectorDataFromPath(Context c, String path) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        File file = new File(path);
        InputStream is = null;

        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return is;
    }

    public static InputStream apkOpenVectorDataFromPath(Context c, String path) {
        InputStream ret = null;

        try {
            String[] files = c.getAssets().list(path);

            for (String file : files) {
                if (file.endsWith(VECTOR_FILE_EXTENSION)) {
                    ret = c.getAssets().open(checkPathForAppend(path) + file);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static InputStream apkOpenVectorIcon(Context c, String artist, String vectorDirectory) {
        return apkOpenAsset(c, apkVectorPath(artist, vectorDirectory) + VECTOR_ICON_FILE);
    }

    public static InputStream apkOpenVectorIconFromPath(Context c, String path) {
        return apkOpenAsset(c, checkPathForAppend(path) + VECTOR_ICON_FILE);
    }

    public static InputStream apkOpenTile(Context c, String tile) {
        return apkOpenAsset(c, apkTilePath(tile));
    }

    public static InputStream apkOpenTile(Context c, Attributes a) {
        return apkOpenAsset(c, TILES_PATH + XMLFormat.styleGetFillTile(a));
    }

    public static InputStream apkOpenStyle(Context c, String style) {
        return apkOpenAsset(c, apkStylePath(style));
    }

    public static String[] listApkArtists(Context c) {
        String[] ret = null;

        try {
            ret = c.getAssets().list(ARTISTS_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String[] apkLlistVectorDirectories(Context c, String artist) {
        String[] ret = null;

        try {
            ret = c.getAssets().list(ARTISTS_PATH + File.separator + artist + File.separator + VECTORS_DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String[] apkLlistStyles(Context c) {
        String[] ret = null;

        try {
            ret = c.getAssets().list(STYLES_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String apkVectorIconFromPath(String path) {
        return checkPathForAppend(path) + VECTOR_ICON_FILE;
    }

    public static InputStream openVectorInfo(Context c, PlaylistEntry entry) {
        if (entry.location == PlaylistEntry.Location.apk) {
            return apkOpenVectorInfoFromPath(c, entry.path);
        }

        return null;
    }

    public static InputStream openVectorData(Context c, PlaylistEntry entry) {
        if (entry.location == PlaylistEntry.Location.apk) {
            return apkOpenVectorDataFromPath(c, entry.path);
        } else {
            return sdOpenVectorDataFromPath(c, entry.path);
        }
    }

    public static InputStream sdOpenStyle(String file) {
        return sdOpenAsset(sdStylePath(file));
    }

    private static InputStream sdOpenAsset(String path) {
        InputStream ret = null;

        try {
            File sdroot = Environment.getExternalStorageDirectory();
            File asset = new File(sdroot, path);
            ret = new FileInputStream(asset);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static String sdStylePath(String file) {
        return SD_CUSTOM_STYLES_DIRECTORY + File.separator + file;
    }

    public static File sdOpenStyleDirectory() {
        File sdroot = Environment.getExternalStorageDirectory();
        return new File(sdroot, Assets.SD_CUSTOM_STYLES_DIRECTORY);
    }

    public static File sdOpenStyleFile(String filename) {
        File sdroot = Environment.getExternalStorageDirectory();
        File dir = new File(sdroot, SD_CUSTOM_STYLES_DIRECTORY);
        dir.mkdirs();

        return new File(dir, filename);
    }

    public static OutputStream sdWriteStyle(String filename) {
        OutputStream ret = null;

        try {
            File file = sdOpenStyleFile(filename);
            ret = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void sdRemoveStyle(String filename) {
        File file = sdOpenStyleFile(filename);
        file.delete();
    }

    public static InputStream sdOpenTile(Context c, String tile) {
        String path = tile.replaceFirst(StyleData.CUSTOM_TILE_LOCATION_PREFIX, "");

        File img = new File(path);

        InputStream is = null;

        try {
            is = new FileInputStream(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return is;
    }

    public static String[] apkListTiles(Context c) {
        String[] ret = null;

        try {
            ret = c.getAssets().list(TILES_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static ArrayList<String> sdScanForVectors(final Context c) {
        ArrayList<String> ret = new ArrayList<String>();

        File sdroot = Environment.getExternalStorageDirectory();
        File gallery = new File(sdroot, SD_CUSTOM_GALLERY_DIRECTORY);
        gallery.mkdirs();

        sdRecursiveVectorScan(gallery, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                } else {
                    return pathname.getName().endsWith(VECTOR_FILE_EXTENSION);
                }
            }
        }, ret);

        return ret;
    }

    private static void sdRecursiveVectorScan(File dir, FileFilter filter, ArrayList<String> buffer) {
        if (dir.isDirectory()) {
            File[] list = dir.listFiles(filter);

            if (list != null && list.length > 0) {
                for (File f : list) {
                    sdRecursiveVectorScan(f, filter, buffer);
                }
            }
        } else {
            buffer.add(dir.getPath());
        }
    }

    public static InputStream sdOpenVector(String path) {
        InputStream is = null;

        try {
            @SuppressWarnings("resource")
            FileInputStream fs = new FileInputStream(path);
            is = fs;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return is;
    }
}
