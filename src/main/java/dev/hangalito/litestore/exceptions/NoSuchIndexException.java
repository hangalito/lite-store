package dev.hangalito.litestore.exceptions;

/**
 * Signals an unexisting index.
 */

public class NoSuchIndexException extends Exception {

    /**
     * Constructs the exception with detailed description.
     * @param message The detailed cause of this exception.
     */
    public NoSuchIndexException(String message) {
        super(message);
    }

    /**
     * Constructs the exception with no message description.
     */
    public NoSuchIndexException() {
        super();
    }

}
