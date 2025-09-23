package dev.hangalito.litestore;

import java.io.Serial;
import java.io.Serializable;

/**
 * Every instance in demands an index in the data source.
 * This class is responsible for representing and transporting
 * this index with the required data.
 * @param size The size of the object.
 * @param pointer The object location in the data source.
 */
public record Index(
        int size,
        long pointer
) implements Serializable {
    /**
     * Serial Version UID
     */
    @Serial
    private static final long serialVersionUID = 1L;
}
