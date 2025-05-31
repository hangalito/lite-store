package dev.hangalito.exceptions;

/// Sinalizador de uma fonte de dados inexistente.
/// Esta exceção serã causada quando um [Datasource] para uma classe
/// não gerenciável.
///
/// @since 1.0
/// @author Bartolomeu Hangalo
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
