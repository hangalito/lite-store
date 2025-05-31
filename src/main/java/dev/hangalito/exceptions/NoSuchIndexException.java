package dev.hangalito.exceptions;

import dev.hangalito.storage.Datasource;

/// Sinalizador de um índice inexistente.
/// Esta exceção é causada nos seguintes métodos
///  - {@link Datasource#findBy(String, Object)}
///
/// @since 1.0
/// @author Bartolomeu Hangalo
public class NoSuchIndexException extends Exception {

    public NoSuchIndexException(String message) {
        super(message);
    }

    public NoSuchIndexException() {
        super();
    }

}
