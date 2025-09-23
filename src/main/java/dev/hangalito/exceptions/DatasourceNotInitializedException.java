package dev.hangalito.exceptions;

/**
 * Signals an unexisting data source.
 * <p>
 * This exception will happen when a {@link dev.hangalito.storage.Datasource}
 * is used for a non-manageable class.
 */
public class DatasourceNotInitializedException extends Exception {

    /**
     * Constructs the exception with a default error message.
     */
    public DatasourceNotInitializedException() {
        super("Datasource wasn't properly initialized");
    }

    /**
     * Constructs the exception with a custom error message.
     * @param message The detailed description of the exception.
     */
    public DatasourceNotInitializedException(String message) {
        super(message);
    }

}
