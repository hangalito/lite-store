package dev.hangalito.exceptions;

public class NoSuchIndexException extends Exception {

    public NoSuchIndexException(String message) {
        super(message);
    }

    public NoSuchIndexException() {
        super();
    }

}
