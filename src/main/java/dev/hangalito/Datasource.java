package dev.hangalito;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Datasource<T extends Serializable, ID extends Serializable & Comparable<ID>> {

    private final LocationService service;
    private Map<ID, Index> index;
    private Class<T> table;
    private Class<ID> key;
    private File file;

    public Datasource(LocationService service) {
        this.service = service;
    }

    public void init(Class<T> table, Class<ID> key) throws IOException {
        this.table = table;
        this.key = key;
        file = new File(service.getAsFile(), table.getName() + ".dat");

        File indexFile = new File(service.getAsFile(), table.getName() + ".idx");
        if (!indexFile.exists()) {
            indexFile.createNewFile();
        }
        try (InputStream input = new FileInputStream(indexFile)) {
            if (input.available() > 0) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    //noinspection unchecked
                    this.index = (Map<ID, Index>) stream.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.index = new HashMap<>();
            }
        }
    }

    public void save(T entity, ID key) {
        if (file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] bytes = Serializer.serialize(entity);
            raf.seek(raf.length());
            Index index = new Index(bytes.length, raf.getFilePointer());
            this.index.put(key, index);
            saveIndex();
            raf.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void saveIndex() throws IOException {
        File file = new File(service.getAsFile(), table.getName() + ".idx");
        try (OutputStream output = new FileOutputStream(file)) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(index);
            }
        }
    }

    public synchronized T load(ID key) {
        if (file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }

        if (!index.containsKey(key)) {
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            Index idx = index.get(key);
            raf.seek(idx.pointer());
            byte[] bytes = new byte[idx.size()];
            raf.read(bytes, 0, idx.size());

            try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    var object = stream.readObject();
                    return (T) object;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Unexpected exception: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
