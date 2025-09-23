package dev.hangalito.storage;

import dev.hangalito.exceptions.UnsupportedStorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A utility class responsible for serializing Java objects into byte arrays.
 * It uses standard Java serialization mechanisms to convert {@link Serializable} objects
 * for storage purposes.
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
public class Serializer {
    /**
     * Serializes a given {@link Serializable} object into a byte array.
     *
     * @param object The object of type {@code T} to be serialized. Must not be {@code null}.
     * @param <T> The type of the object, which must implement {@link Serializable}.
     * @return A byte array representing the serialized object.
     * @throws IOException If an I/O error occurs during serialization.
     * @throws UnsupportedStorageException If the provided object is {@code null}.
     */
    public static <T extends Serializable> byte[] serialize(T object) throws IOException, UnsupportedStorageException {
        if (object == null) {
            throw new UnsupportedStorageException("Cannot serialize a null object.");
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(object);
                return output.toByteArray();
            }
        }
    }
}

