package me.jacobtread.instaviewer;

import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

import java.io.Serializable;
import java.util.Objects;

public class InstaMessage implements Serializable {

    public int id;
    public String account;
    public String username;
    public String message;
    public boolean deleted;

    public InstaMessage(int id, String account, String username, String message, boolean deleted) {
        this.id = id;
        this.account = account;
        this.username = username;
        this.message = message;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstaMessage that = (InstaMessage) o;
        return
                id == that.id &&
                        Objects.equals(account, that.account) &&
                        Objects.equals(username, that.username) &&
                        Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, account, username, message, deleted);
    }
}
