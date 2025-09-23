package dev.hangalito.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as storable by the LiteStore library. Classes annotated with {@code @Storable}
 * are eligible for persistence. Such classes must also specify a primary key field using the {@link Key} annotation.
 * The primary key serves as the unique identifier for each instance of the storable class.
 * <p>
 * Example:
 * <pre>
 * {@code
 * @Storable
 * public class MyClass {
 *     @Key
 *     private int id;
 *     private String name;
 *     // other fields, getters, and setters
 * }
 * }
 * </pre>
 *
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Storable {
    /**
     * An optional value that can be associated with the storable class. Its specific usage
     * might be defined by the application using LiteStore.
     * @return A string value, defaults to an empty string.
     */
    String value() default "";
}

