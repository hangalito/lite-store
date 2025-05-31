package dev.hangalito.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marca um atributo de uma classe gerenciável como chave identificadora.
 * @since 1.0
 * @author Bartolomeu Hangalo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Key {
}
