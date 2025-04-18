package dev.hangalito.test;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public record ProgrammingLanguage(
        Integer id,
        String name,
        Double rating
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public ProgrammingLanguage(String name, Double rating) {
        this(counter.incrementAndGet(), name, rating);
    }
}
