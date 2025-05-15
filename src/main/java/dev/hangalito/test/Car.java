package dev.hangalito.test;

import dev.hangalito.annotations.Key;
import dev.hangalito.annotations.Storage;

import java.io.Serializable;

@Storage
public record Car(
        @Key int id,
        String brand,
        String model
) implements Serializable {
}
