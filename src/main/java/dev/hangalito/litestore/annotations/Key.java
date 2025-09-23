package dev.hangalito.litestore.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a field within a storable class as the primary key.
 * This annotation is used to identify the unique identifier for instances of a {@link Storable} class.
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Key {
}
