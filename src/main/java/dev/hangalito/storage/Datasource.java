package dev.hangalito.storage;

import dev.hangalito.annotations.Key;
import dev.hangalito.annotations.Storage;
import dev.hangalito.exceptions.DatasourceNotInitializedException;
import dev.hangalito.exceptions.NoKeyDefinedException;
import dev.hangalito.exceptions.UnsupportedStorageException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
public final class Datasource<E extends Serializable, K extends Serializable & Comparable<K>> implements Repository<E, K> {

    private volatile Class<E> entityClass;
    private volatile File tablefile;
    private volatile File indexfile;
    private volatile Map<K, Index> storageIndex;

    public Datasource() {
    }

    @Override
    public Stream<E> fetch() throws DatasourceNotInitializedException {
        isInitialized();
        Stream.Builder<E> builder = Stream.builder();
        storageIndex.values().forEach(index -> {
            long position = index.pointer();
            int size = index.size();
            byte[] buff = new byte[size];

            try (RandomAccessFile raf = new RandomAccessFile(tablefile, "r")) {
                raf.seek(position);
                raf.read(buff, 0, size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                E entity = deserialize(buff);
                builder.add(entity);
            } catch (UnsupportedStorageException e) {
                throw new RuntimeException(e);
            }
        });
        return builder.build();
    }

    @Override
    public Optional<E> findByKey(K key) throws DatasourceNotInitializedException, UnsupportedStorageException {
        isInitialized();
        if (!storageIndex.containsKey(key)) {
            return Optional.empty();
        }

        try (RandomAccessFile raf = new RandomAccessFile(tablefile, "r")) {
            Index index = storageIndex.get(key);
            if (index == null) {
                return Optional.empty();
            }
            raf.seek(index.pointer());
            byte[] buff = new byte[index.size()];
            raf.read(buff);
            return Optional.of(deserialize(buff));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void save(E entity) throws DatasourceNotInitializedException, NoKeyDefinedException {
        isInitialized();
        K key = extractKey(entity);
        try (RandomAccessFile raf = new RandomAccessFile(tablefile, "rw")) {
            long pointer = raf.length();
            byte[] buff = Serializer.serialize(entity);
            int size = buff.length;

            raf.seek(pointer);
            raf.write(buff);

            Index index = new Index(size, pointer);
            storageIndex.put(key, index);
            updateIndex();
        } catch (IOException | UnsupportedStorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void update(K key, E entity) throws DatasourceNotInitializedException {
        isInitialized();
        try (RandomAccessFile raf = new RandomAccessFile(tablefile, "rw")) {
            Index index = storageIndex.get(key);
            if (index == null) {
                throw new NoKeyDefinedException();
            }
            raf.seek(index.pointer());
            byte[] buff = Serializer.serialize(entity);
            raf.write(buff);

            Index newIndex = index.newSize(buff.length);
            storageIndex.remove(key);
            storageIndex.put(extractKey(entity), newIndex);
            updateIndex();
        } catch (IOException | UnsupportedStorageException | NoKeyDefinedException e) {
            throw new RuntimeException(e);
        }
    }


    private synchronized void loadIndex() {
        try (InputStream input = new FileInputStream(indexfile)) {
            if (input.available() > 0) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    Object read = stream.readObject();
                    if (read instanceof Map) {
                        storageIndex.putAll((Map<K, Index>) read);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void updateIndex() {
        try (OutputStream output = new FileOutputStream(indexfile)) {
            try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                stream.writeObject(storageIndex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void init(Class<E> c) throws IOException, UnsupportedStorageException, NoKeyDefinedException {
        this.entityClass = c;
        assertClass(c);
        LocationService service = LocationService.getInstance();
        tablefile = new File(service.getAsFile(), c.getName() + ".ser");
        indexfile = new File(service.getAsFile(), c.getName() + ".idx");
        storageIndex = new HashMap<>();
        if (!tablefile.exists()) {
            tablefile.createNewFile();
        }
        if (!indexfile.exists()) {
            indexfile.createNewFile();
        }
        loadIndex();
    }

    private void assertClass(Class<E> eClass) throws UnsupportedStorageException, NoKeyDefinedException {
        if (!eClass.isAnnotationPresent(Storage.class)) {
            throw new UnsupportedStorageException("Storage entity not mapped");
        }
        boolean doesntHaveKey = true;
        for (Field field : eClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Key.class)) {
                doesntHaveKey = false;
                break;
            }
        }
        if (doesntHaveKey) {
            throw new NoKeyDefinedException("No key defined for this entity");
        }

    }

    private E deserialize(byte[] buff) throws UnsupportedStorageException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(buff)) {
            try (ObjectInputStream stream = new ObjectInputStream(input)) {
                return (E) stream.readObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedStorageException("Unable to load the data");
        }
    }

    private void isInitialized() throws DatasourceNotInitializedException {
        if (tablefile == null || indexfile == null || storageIndex == null) {
            throw new DatasourceNotInitializedException();
        }
    }

    private K extractKey(E entity) throws NoKeyDefinedException {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Key.class)) {
                field.setAccessible(true);
                try {
                    return (K) field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new NoKeyDefinedException();
    }
}
