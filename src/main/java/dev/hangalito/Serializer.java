package dev.hangalito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serializer {
    public static  <T extends Serializable> byte[] serialize(T object) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(object);
                return output.toByteArray();
            }
        }
    }
}
