package dev.hangalito.litestore;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A helper interface for interacting with the library.
 * @param <E> The type of the entity handled by this repository.
 * @param <K> The type of the primary key/identifier of its instances.
 */
public interface Repository<E, K> {
    /**
     * Fetches all data from the datasource.
     * @return {@link Stream} of {@link E}
     * @throws Exception Exception during the processing.
     */
    Stream<E> fetch() throws Exception;

    /**
     * Retrieves an instance in the datasource with
     * the specified primary key.
     * @param key The key of instance to retrieve.
     * @return {@link Optional} of {@link E}
     * @throws Exception Exception during the processing.
     */
    Optional<E> findByKey(K key) throws Exception;

    /**
     * Save an entity into the database.
     * @param entity The entity instance to be saved.
     * @throws Exception Exception during the processing.
     */
    void save(E entity) throws Exception;


    /**
     * Update the details of the instance with a given primary key.
     * @param key The key of the instance to update.
     * @param entity The details to update.
     * @throws Exception Exception during the processing.
     */
    void update(K key, E entity) throws Exception;

    /**
     * Save a collection of instances in a row.
     * @param collection The collection containing the instances to be persisted.
     * @param <C> The type of collection.
     *
     * @throws Exception Exception during the processing.
     */
    default <C extends Collection<? extends E>> void saveAll(C collection) throws Exception {
        for (E e : collection) {
            save(e);
        }
    }
}
