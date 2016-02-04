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

package com.kikijiki.ryukisenga.playlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.gallery.ArtistPageFragment.GalleryEntry;
import com.kikijiki.ryukisenga.playlist.PlaylistManager.PlaylistEntry.Location;
import com.kikijiki.ryukisenga.preferences.WallPreferences;
import com.kikijiki.ryukisenga.styles.StyleManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager {
    private static void save(List<PlaylistEntry> pl, Context c) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = settings.edit();

        StringBuffer sb = new StringBuffer();

        for (PlaylistEntry e : pl) {
            sb.append(e.toString());
            sb.append(PlaylistEntry.DELIMITATOR);
        }

        editor.putString(WallPreferences.Keys.PLAYLIST_PREFERENCE_KEY, sb.toString());

        editor.commit();
    }

    private static List<PlaylistEntry> load(Context c) {
        ArrayList<PlaylistEntry> ret = new ArrayList<PlaylistEntry>();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

        if (settings == null)
            return null;

        String pl = settings.getString(WallPreferences.Keys.PLAYLIST_PREFERENCE_KEY, "");
        String[] t = pl.split(PlaylistEntry.DELIMITATOR);

        String[] buf = new String[PlaylistEntry.FIELD_COUNT];
        int j = 0;

        for (int i = 0; i < t.length; i++) {
            buf[j] = t[i];
            j++;

            if (j == PlaylistEntry.FIELD_COUNT) {
                PlaylistEntry e = new PlaylistEntry(buf);
                ret.add(e);

                j = 0;
            }
        }

        return ret;
    }

    private static void drop(List<PlaylistEntry> pl, int from, int to) {
        if (from > 0 && from < pl.size() && to < pl.size() && to >= 0) {
            PlaylistEntry tmp = pl.get(from);
            pl.remove(from);
            pl.add(to, tmp);
        }
    }

    public static void setShuffle(Context c, boolean shuffle) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(WallPreferences.Keys.PLAYLIST_SHUFFLE_PREFERENCE, shuffle);
        editor.commit();
    }

    public static boolean getShuffle(Context c) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

        return settings.getBoolean(WallPreferences.Keys.PLAYLIST_SHUFFLE_PREFERENCE, false);
    }

    public static void changeStyle(Context c, PlaylistEntry entry, String style) {
        List<PlaylistEntry> pl = load(c);
        int pos = pl.indexOf(entry);

        if (pos < 0)
            return;

        PlaylistEntry target = pl.get(pos);
        target.style = style;
        entry.style = style;

        save(pl, c);
    }

    public static void add(Context c, PlaylistEntry e) {
        List<PlaylistEntry> pl = load(c);

        if (e.style == null) {
            e.style = StyleManager.DEFAULT_STYLE;
        }

        if (!pl.contains(e))
            pl.add(e);

        save(pl, c);
    }

    public static void remove(Context c, PlaylistEntry e) {
        List<PlaylistEntry> pl = load(c);
        pl.remove(e);
        save(pl, c);
    }

    public static void drop(Context c, int from, int to) {
        List<PlaylistEntry> pl = load(c);
        drop(pl, from, to);
        save(pl, c);
    }

    public static List<PlaylistEntry> loadPlaylist(Context c, boolean shuffled) {
        if (c == null)
            return null;

        ArrayList<PlaylistEntry> ret = new ArrayList<PlaylistEntry>();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

        if (settings == null)
            return null;

        String pl = settings.getString(WallPreferences.Keys.PLAYLIST_PREFERENCE_KEY, "");
        String[] t = pl.split(PlaylistEntry.DELIMITATOR);

        String[] buf = new String[PlaylistEntry.FIELD_COUNT];
        int j = 0;

        ArrayList<PlaylistEntry> remove_list = new ArrayList<PlaylistEntry>();

        for (int i = 0; i < t.length; i++) {
            buf[j] = t[i];
            j++;

            if (j == PlaylistEntry.FIELD_COUNT) {
                PlaylistEntry e = new PlaylistEntry(buf);

                if (checkPlaylistEntry(c, e)) {
                    ret.add(e);
                } else {
                    remove_list.add(e);
                }


                j = 0;
            }
        }

        if (shuffled) {
            if (settings.getBoolean(WallPreferences.Keys.PLAYLIST_SHUFFLE_PREFERENCE, true))
                Collections.shuffle(ret);
        }

        for (PlaylistEntry e : remove_list) {
            PlaylistManager.remove(c, e);
        }

        return ret;
    }

    private static boolean checkPlaylistEntry(Context c, PlaylistEntry e) {
        if (e.location == Location.apk) {
            return Assets.apkCheckVector(c, e.path);
        } else {
            File file = new File(e.path);
            return file.exists() && file.isFile();
        }
    }

    public static class PlaylistEntry implements Parcelable {
        public static final int FIELD_COUNT = 5;
        public final static String DELIMITATOR = ";";
        private static final long serialVersionUID = -4113093637344964891L;
        public String artist;
        public String title;
        public Location location;
        public String path;
        public String style;

        public PlaylistEntry(String artist, String path, Location location)
        {
            this.artist = artist;
            this.path = path;
            this.location = location;
        }

        public PlaylistEntry(Parcel in) {
            artist = in.readString();
            title = in.readString();
            location = Location.valueOf(in.readString());
            path = in.readString();
            style = in.readString();
        }

        public PlaylistEntry(String s) {
            String[] t = s.split(DELIMITATOR);

            if (t.length >= FIELD_COUNT) {
                artist = t[0];
                title = t[1];
                location = Location.valueOf(t[2]);
                path = t[3];
                style = t[4];
            }
        }

        public PlaylistEntry(String[] data) {
            if (data.length >= FIELD_COUNT) {
                artist = data[0];
                title = data[1];
                location = Location.valueOf(data[2]);
                path = data[3];
                style = data[4];
            }
        }

        public String toString() {
            return artist + DELIMITATOR + title + DELIMITATOR + location.toString() + DELIMITATOR + path + DELIMITATOR + style;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (obj.getClass() == GalleryEntry.class) {
                final GalleryEntry other = (GalleryEntry) obj;

                boolean samePath = (this.path == other.data.path) || (this.path != null && this.path.equals(other.data.path));
                if (!samePath)
                    return false;

                boolean sameArtist = (this.artist == other.data.artist) || (this.artist != null && this.artist.equalsIgnoreCase(other.data.artist));
                if (!sameArtist)
                    return false;

                boolean sameTitle = (this.title == other.data.title) || (this.title != null && this.title.equalsIgnoreCase(other.data.title));
                if (!sameTitle)
                    return false;
            } else if (obj.getClass() == getClass()) {
                final PlaylistEntry other = (PlaylistEntry) obj;

                boolean samePath = (this.path == other.path) || (this.path != null && this.path.equals(other.path));
                if (!samePath)
                    return false;

                boolean sameArtist = (this.artist == other.artist) || (this.artist != null && this.artist.equalsIgnoreCase(other.artist));
                if (!sameArtist)
                    return false;

                boolean sameTitle = (this.title == other.title) || (this.title != null && this.title.equalsIgnoreCase(other.title));
                if (!sameTitle)
                    return false;
            } else {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + artist.hashCode();
            hash = 89 * hash + title.hashCode();
            return hash;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(artist);
            parcel.writeString(title);
            parcel.writeString(location.toString());
            parcel.writeString(path);
            parcel.writeString(style);
        }

        public static final Parcelable.Creator<PlaylistEntry> CREATOR
                = new Parcelable.Creator<PlaylistEntry>() {
            public PlaylistEntry createFromParcel(Parcel in) {
                return new PlaylistEntry(in);
            }

            public PlaylistEntry[] newArray(int size) {
                return new PlaylistEntry[size];
            }
        };

        public enum Location {
            apk,
            sd
        }
    }
}
