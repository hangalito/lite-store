package dev.hangalito.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anota uma classe suscetível ao armazenamento.
 * Após anotadas as classes armazenáveis, estas devem especificar o
 * atributo que será utilizado como chave da classe. A chave é o
 * atribúto único que serve para identificar cada instância.
 * <p>
 * Exemplo:
 * {@snippet class = Gerenciavel}
 *
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Storable {
    String value() default "";
}
