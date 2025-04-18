package dev.hangalito;

import java.io.Serial;
import java.io.Serializable;

public record Index(int size, long pointer) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
