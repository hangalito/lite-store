package dev.hangalito.storage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface Repository<E, K> {
    Stream<E> fetch() throws Exception;

    Optional<E> findByKey(K key) throws Exception;

    void save(E entity) throws Exception;

    void update(K key, E entity) throws Exception;

    default <C extends Collection<? extends E>> void saveAll(C collection) throws Exception {
        for (E e : collection) {
            save(e);
        }
    }
}
