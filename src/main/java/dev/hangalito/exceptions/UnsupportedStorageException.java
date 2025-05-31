package dev.hangalito.exceptions;

import dev.hangalito.annotations.Storable;

import java.io.Serializable;

public class UnsupportedStorageException extends Exception {
    public UnsupportedStorageException() {
        super("Trying to store an unsupported type of object. " +
                "Make sure to annotate it with " + Storable.class.getName() +
                " and implement " + Serializable.class.getName() + " interface");
    }
}
