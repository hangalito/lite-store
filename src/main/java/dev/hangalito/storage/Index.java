package dev.hangalito.storage;

import java.io.Serial;
import java.io.Serializable;

public record Index(int size, long pointer) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Index newSize(int size) {
        return new Index(size, pointer);
    }

    public Index newPointer(long pointer) {
        return new Index(size, pointer);
    }
}
