package dev.hangalito.exceptions;

import dev.hangalito.annotations.Storage;

import java.io.Serializable;

public class UnsupportedStorageException extends Exception {

    public UnsupportedStorageException(String message) {
        super(message);
    }

    public UnsupportedStorageException() {
        super("Trying to store an unsupported type of object. " +
                "Make sure to annotate it with " + Storage.class.getName() +
                " and implement " + Serializable.class.getName() + " interface");
    }
}
