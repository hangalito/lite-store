package dev.hangalito.exceptions;

public class NoKeyDefinedException extends Exception {
    public NoKeyDefinedException(String message) {
        super(message);
    }

    public NoKeyDefinedException() {
        super();
    }
}
