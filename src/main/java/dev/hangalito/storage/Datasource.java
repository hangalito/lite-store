package dev.hangalito.storage;

import dev.hangalito.annotations.Key;
import dev.hangalito.exceptions.NoSuchIndexException;
import dev.hangalito.exceptions.UnsupportedStorageException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Manages the persistence operations for a specific storable class {@code T} with a primary key of type {@code ID}.
 * This class serves as the primary entry point for interacting with LiteStore, providing methods for CRUD operations
 * and index management.
 *
 * @param <T> The type of the storable entity, which must implement {@link Serializable}.
 * @param <ID> The type of the primary key for the storable entity, which must implement {@link Serializable} and {@link Comparable}.
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
@SuppressWarnings({"unchecked"})
public class Datasource<T extends Serializable, ID extends Serializable & Comparable<ID>> {

    /**
     * The singleton instance of {@link LocationService} used to determine file system locations.
     */
    private final LocationService service;

    /**
     * The in-memory index of saved {@code T} entities. Maps primary keys to {@link Index} objects.
     */
    private Map<ID, Index> index;

    /**
     * The {@link Class} type of the entity managed by this datasource.
     */
    private Class<T> table;

    /**
     * The {@link File} storing the actual serialized data for the entities.
     */
    private File file;

    /**
     * Creates a new {@link Datasource} instance.
     * Initializes the {@link LocationService}.
     */
    public Datasource() {
        this.service = LocationService.getInstance();
    }

    /**
     * Initializes this datasource for a specific entity class. This method must be called before
     * performing any persistence-related operations (save, update, delete, findAll, findBy).
     * It sets up the entity class, determines the data file path, and loads the primary index
     * from storage or initializes an empty one if none exists.
     *
     * @param entityClass The class type of the entity dealt by this datasource.
     * @throws RuntimeException if an {@link IOException} or {@link ClassNotFoundException} occurs during index loading.
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
     * If no entities are found, an empty list is returned.
     *
     * @return A {@link List} of all saved entities of type {@code T}.
     * @throws IllegalStateException if the datasource has not been initialized.
     * @throws RuntimeException if an {@link IOException} or {@link ClassNotFoundException} occurs during data retrieval.
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
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return entities;
    }

    /**
     * Saves an entity into the storage. The entity is serialized and appended to the data file.
     * The primary key of the entity is extracted and an {@link Index} entry is created or updated
     * in the in-memory index, which is then persisted to the index file.
     *
     * @param entity The entity of type {@code T} to be stored.
     * @throws IllegalStateException if the datasource has not been initialized.
     * @throws RuntimeException if an {@link IOException} or {@link UnsupportedStorageException} occurs during saving.
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
     * Retrieves an entity instance with a corresponding primary key.
     *
     * @param key The primary key of the entity to be retrieved.
     * @return An {@link Optional} containing the instance of {@code T} if found, or an empty {@link Optional} if no instance was found.
     * @throws IllegalStateException if the datasource has not been initialized.
     * @throws RuntimeException if an {@link IOException} or {@link ClassNotFoundException} occurs during retrieval.
     */
    public Optional<T> findByIndex(ID key) {
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
     * Updates an existing entity in the storage. The updated entity is serialized and appended to the data file.
     * The {@link Index} entry for the given ID is updated to point to the new location of the serialized object.
     *
     * @param id The primary key of the entity to update.
     * @param entity The updated entity of type {@code T}.
     * @throws RuntimeException if an {@link IOException} or {@link UnsupportedStorageException} occurs during updating.
     */
    public void update(ID id, T entity) {
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            raf.seek(raf.length());
            byte[] buffered = Serializer.serialize(entity);
            long pointer = raf.getFilePointer();
            Index idx = new Index(buffered.length, pointer);

            raf.write(buffered, 0, buffered.length);
            index.put(id, idx);
            saveIndex();
        } catch (IOException | UnsupportedStorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all entities that have a specific value for a given field. An index for this field
     * must have been created previously using {@link #createIndex(String)}.
     *
     * @param field The name of the field to look into (e.g., "name").
     * @param value The value of the attribute to group into.
     * @return A {@link List} of all entities of type {@code T} with the provided value in the specified field.
     * @throws NoSuchIndexException If the field trying to access wasn't previously indexed.
     * @throws RuntimeException if an {@link IOException} or {@link ClassNotFoundException} occurs during retrieval.
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
     * Deletes an instance from the datasource. This operation removes the entity's primary key
     * from the in-memory index and persists the updated index to the index file. Note that
     * this does not physically remove the data from the `.dat` file, only its reference from the index.
     *
     * @param instance The instance of type {@code T} to be deleted.
     * @throws RuntimeException if an {@link IOException} occurs during index saving.
     */
    public void delete(T instance) {
        ID id = extractKey(instance);
        index.remove(id);
        saveIndex();
    }

    /**
     * Creates a custom index on a specified field of the entity. This allows for efficient retrieval
     * of entities based on the values of this field using {@link #findBy(String, Object)}.
     *
     * @param name The name of the field in the entity to index.
     * @throws NoSuchFieldException If the specified field was not found in the entity class.
     * @throws RuntimeException if an {@link IllegalAccessException} occurs during field access or an {@link IOException} occurs during index saving.
     * @throws IllegalStateException if no primary key is found for a storable entity during index creation.
     */
    public void createIndex(String name) throws NoSuchFieldException {
        Field field = table.getDeclaredField(name);
        String filename = table.getName() + "#" + field.getName();
        File file = service.getAsIndex(filename);
        Map<Object, List<Index>> fieldIndex = new HashMap<>();

        findAll().forEach(entity -> {
            field.setAccessible(true);
            Object indexValue;

            try {
                indexValue = field.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            fieldIndex.compute(indexValue, (k, v) -> {
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
     * Saves the in-memory primary index of the entities into the persistence storage (the `.idx` file).
     * This method is called internally after any operation that modifies the index (save, update, delete).
     *
     * @throws RuntimeException if an {@link IOException} occurs during index saving.
     */
    private void saveIndex() {
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

    /**
     * Retrieves an entity from the data file based on its {@link Index} metadata.
     * This is an internal helper method used by other retrieval operations.
     *
     * @param index The {@link Index} object containing the size and pointer of the entity.
     * @return The deserialized entity of type {@code T}.
     * @throws RuntimeException if an {@link IOException} or {@link ClassNotFoundException} occurs during retrieval.
     */
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

    /**
     * Extracts the primary key from a given entity instance by looking for the field
     * annotated with {@link Key}.
     *
     * @param entity The entity of type {@code T} from which to extract the key.
     * @return The primary key of type {@code ID}.
     * @throws RuntimeException if an {@link IllegalAccessException} occurs during field access.
     * @throws IllegalStateException if no field annotated with {@link Key} is found in the entity.
     */
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
        throw new IllegalStateException("No field annotated with @Key found in entity " + entity.getClass().getName());
    }

}

