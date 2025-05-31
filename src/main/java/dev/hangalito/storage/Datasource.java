package dev.hangalito.storage;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class Datasource<T extends Serializable, ID extends Serializable & Comparable<ID>> {

    /// Location service instance
    private final LocationService service;

    /// The index of saved {T} entities
    private Map<ID, Index> index;

    ///  The class type of the entity
    private Class<T> table;

    /// The file storing the actual data
    private File file;

    /**
     * Creates a new {@link Datasource} instance.
     */
    public Datasource() {
        this.service = LocationService.getInstance();
    }

    /**
     * Initializes this datasource.
     *
     * @param entityClass The class type of the entity dealt by this datasource.
     */
    public void init(Class<T> entityClass) {
        this.table = entityClass;
        file = new File(service.getAsFile(), entityClass.getName() + ".dat");

        try (InputStream input = new FileInputStream(service.getAsIndex(entityClass.getName()))) {
            if (input.available() > 0) {
                ObjectInputStream stream = new ObjectInputStream(input);
                this.index = (Map<ID, Index>) stream.readObject();
                stream.close();
            } else {
                this.index = new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves all saved entities into the storage.
     *
     * @return The saved entities.
     */
    public List<T> findAll() {
        if (index == null || file == null) {
            throw new IllegalStateException("Datasource not initialized");
        }

        List<T> entities = new ArrayList<>();
        index.values().forEach(index -> {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buff = new byte[index.size()];
                raf.seek(index.pointer());
                raf.read(buff, 0, index.size());
                try (ByteArrayInputStream input = new ByteArrayInputStream(buff)) {
                    try (ObjectInputStream stream = new ObjectInputStream(input)) {
                        entities.add((T) stream.readObject());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return entities;
    }

    /**
     * Saves an entity into the storage.
     *
     * @param entity The entity to be stored.
     */
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
        } catch (IOException | UnsupportedStorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves an entity instance with a corresponding key.
     *
     * @param key The key of the entity to be retrieved.
     * @return An instance of {@link T}, or {@code null} if no instance was found.
     */
    public synchronized Optional<T> findByIndex(ID key) {
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

    /**
     * Tries to retrieve all entities with a given value of an attribute.
     * The index of this field must be created before trying to look for.
     *
     * @param field The name of the field to look into.
     * @param value The value of the attribute to group into.
     * @return All the entities with the provided value in the field.
     * @throws NoSuchIndexException If the field trying to access wasn't previously created.
     */
    public List<T> findBy(String field, Object value) throws NoSuchIndexException {
        Map<Object, List<Index>> fieldIndex = new HashMap<>();
        String filename = table.getName() + "#" + field;
        File file = service.getAsIndex(filename, false);

        if (file == null) {
            throw new NoSuchIndexException("Index '" + field + "' not created");
        }

        try (InputStream input = new FileInputStream(file)) {
            if (input.available() == 0) {
                return Collections.emptyList();
            }
            try (ObjectInputStream stream = new ObjectInputStream(input)) {
                Object object = stream.readObject();
                fieldIndex.putAll((Map<?, ? extends List<Index>>) object);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<T> entities = new ArrayList<>();
        if (fieldIndex.containsKey(value)) {
            for (Index idx : fieldIndex.get(value)) {
                entities.add(findByIndex(idx));
            }
        }
        return entities;
    }

    /**
     * Deletes an instance from the datasource.
     *
     * @param instance The instance to be deleted.
     */
    public void delete(T instance) {
        ID id = extractKey(instance);
        index.remove(id);
        saveIndex();
    }

    /**
     * Create an index in a specified field of the entity.
     *
     * @param name The name of the entity to index.
     * @throws NoSuchFieldException If this field wasn't found in the entity.
     */
    public void createIndex(String name) throws NoSuchFieldException {
        Field field = table.getDeclaredField(name);
        String filename = table.getName() + "#" + field.getName();
        File file = service.getAsIndex(filename);
        Map<Object, List<Index>> fieldIndex = new HashMap<>();

        findAll().forEach(entity -> {
            field.setAccessible(true);
            Object index;

            try {
                index = field.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            fieldIndex.compute(index, (k, v) -> {
                ID key = null;
                if (v == null) {
                    v = new ArrayList<>();
                }


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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to save the in-memory index of the entities into the persistence storage.
     */
    private synchronized void saveIndex() {
        try {
            try (OutputStream output = new FileOutputStream(service.getAsIndex(table.getName()))) {
                try (ObjectOutputStream stream = new ObjectOutputStream(output)) {
                    stream.writeObject(index);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private T findByIndex(Index index) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buff = new byte[index.size()];
            raf.seek(index.pointer());
            raf.read(buff, 0, index.size());
            try (ByteArrayInputStream input = new ByteArrayInputStream(buff)) {
                try (ObjectInputStream stream = new ObjectInputStream(input)) {
                    return (T) stream.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ID extractKey(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isAnnotated = field.isAnnotationPresent(Key.class);
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

}
