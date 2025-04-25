package dev.hangalito.storage;

import dev.hangalito.exceptions.UnsupportedStorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serializer {
   public static <T extends Serializable> byte[] serialize(T object) throws IOException, UnsupportedStorageException {
        if (object == null) {
            throw new UnsupportedStorageException();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(object);
                return output.toByteArray();
            }
        }
    }
}
