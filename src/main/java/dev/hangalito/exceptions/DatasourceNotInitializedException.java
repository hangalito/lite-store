package dev.hangalito.exceptions;

public class DatasourceNotInitializedException extends Exception {
    public DatasourceNotInitializedException() {
        super("Datasource wasn't properly initialized");
    }

    public DatasourceNotInitializedException(String message) {
        super(message);
    }

    public DatasourceNotInitializedException(Throwable cause) {
        super(cause);
    }
}
