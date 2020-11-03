package me.jacobtread.instaviewer;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IconListParcelable implements Parcelable {

    public static final Creator<IconListParcelable> CREATOR = new Creator<IconListParcelable>() {
        @Override
        public IconListParcelable createFromParcel(Parcel in) {
            return new IconListParcelable(in);
        }

        @Override
        public IconListParcelable[] newArray(int size) {
            return new IconListParcelable[size];
        }
    };

    private final Map<String, Icon> icons = new HashMap<>();

    protected IconListParcelable(Parcel in) {
        if (in == null) {
            return;
        }
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readString();
            Icon icon = in.readParcelable(ClassLoader.getSystemClassLoader());
            icons.put(name, icon);
        }
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Set<Map.Entry<String, Icon>> entries = icons.entrySet();
        dest.writeInt(entries.size());
        for (Map.Entry<String, Icon> entry : entries) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    public void add(String username, Icon icon) {
        icons.put(username, icon);
    }

    public Icon get(String username) {
        if (!contains(username)) {
            return null;
        }
        return icons.get(username);
    }

    public void clear() {
        icons.clear();
    }

    public boolean contains(String username) {
        return icons.containsKey(username);
    }

    public Map<String, Icon> getIcons() {
        return icons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconListParcelable that = (IconListParcelable) o;
        return Objects.equals(icons, that.icons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icons);
    }
}
