package dev.hangalito;

import dev.hangalito.annotations.Key;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Datasource<T extends Serializable, ID extends Serializable & Comparable<ID>> {

    private final LocationService service;
    private Map<ID, Index> index;
    private Class<T> table;
    private File file;

    public Datasource(LocationService service) {
        this.service = service;
    }

    public void init(Class<T> table) throws IOException {
        this.table = table;
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

    public void save(T entity) {
        if (file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] bytes = Serializer.serialize(entity);
            raf.seek(raf.length());
            Index index = new Index(bytes.length, raf.getFilePointer());
            this.index.put(extractKey(entity), index);
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

    public synchronized Optional<T> load(ID key) {
        if (file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }

        if (!index.containsKey(key)) {
            return Optional.empty();
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            Index idx = index.get(key);
            raf.seek(idx.pointer());
            byte[] bytes = new byte[idx.size()];
            raf.read(bytes, 0, idx.size());

            try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    var object = stream.readObject();
                    return Optional.of((T) object);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Unexpected exception: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized T load(Index index) throws IOException, ClassNotFoundException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buff = new byte[index.size()];
            raf.seek(index.pointer());
            raf.read(buff, 0, index.size());
            try (ByteArrayInputStream input = new ByteArrayInputStream(buff)) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    return (T) stream.readObject();
                }
            }
        }
    }

    public Stream<T> findBy(String field, Object value) throws IOException, ClassNotFoundException {
        Map<Object, List<Index>> fieldIndex = new ConcurrentHashMap<>();
        File file = new File(this.service.getAsFile(), table.getName() + "#" + field + ".idx");
        try (InputStream input = new FileInputStream(file)) {
            if (input.available() == 0) {
                return Stream.empty();
            }
            try (ObjectInputStream stream = new ObjectInputStream(input)) {
                var object = stream.readObject();
                fieldIndex.putAll((Map<?, ? extends List<Index>>) object);
            }
        }
        Stream.Builder<T> builder = Stream.builder();
        if (fieldIndex.containsKey(value)) {
            for (Index idx : fieldIndex.get(value)) {
                builder.add(load(idx));
            }
        }
        return builder.build();
    }

    public void index(String name) throws NoSuchFieldException, IOException {
        Field field = table.getDeclaredField(name);
        File file = new File(this.service.getAsFile(), table.getName() + "#" + field.getName() + ".idx");
        if (!file.exists()) {
            file.createNewFile();
        }
        Map<Object, List<Index>> fieldIndex = new ConcurrentHashMap<>();
        findAll().parallel().forEach(entity -> {
            field.setAccessible(true);
            Object index;
            try {
                index = field.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            fieldIndex.computeIfAbsent(index, k -> new ArrayList<>());
            fieldIndex.computeIfPresent(index, (k, v) -> {
                ID key = null;
                for (Field declaredField : entity.getClass().getDeclaredFields()) {
                    if (declaredField.isAnnotationPresent(Key.class)) {
                        declaredField.setAccessible(true);
                        try {
                            key = (ID) declaredField.get(entity);
                            break;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (key == null) {
                    throw new IllegalStateException("No key found for this storage entity");
                }
                v.add(Datasource.this.index.get(key));
                return v;
            });
        });

        try (OutputStream output = new FileOutputStream(file)) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(fieldIndex);
            }
        }
    }

    private ID extractKey(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isAnnotated = field.isAnnotationPresent(Key.class);
            System.out.println("verifying field " + field);
            if (isAnnotated) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    return (ID) value;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public Stream<T> findAll() {
        if (index == null || file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }

        Stream.Builder<T> builder = Stream.builder();
        index.values().forEach(index -> {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buff = new byte[index.size()];
                raf.seek(index.pointer());
                raf.read(buff, 0, index.size());
                try (ByteArrayInputStream input = new ByteArrayInputStream(buff)) {
                    try (ObjectInputStream stream = new ObjectInputStream(input)) {
                        builder.add((T) stream.readObject());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return builder.build();
    }

}
