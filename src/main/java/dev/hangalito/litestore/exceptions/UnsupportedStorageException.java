package dev.hangalito.litestore.exceptions;

import dev.hangalito.litestore.annotations.Storable;

import java.io.Serializable;

/**
 * Signals that the storing class is not annotated.
 */
public class UnsupportedStorageException extends Exception {
    /**
     * Constructor with default error message.
     */
    public UnsupportedStorageException() {
        super("Trying to store an unsupported type of object. " +
                      "Make sure to annotate it with " + Storable.class.getName() +
                      " and implement " + Serializable.class.getName() + " interface");
    }

    /**
     * Constructs the exception with a custom message.
     * @param s The detailed cause of the exception.
     */
    public UnsupportedStorageException(String s) {
        super(s);
    }
}
