package me.jacobtread.instaviewer;

import java.io.Serializable;

public class Wrapper <T extends Serializable> implements Serializable {
    private final T wrapped;

    public Wrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    public T get() {
        return wrapped;
    }
}
