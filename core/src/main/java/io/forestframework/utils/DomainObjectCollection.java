package io.forestframework.utils;

import org.apiguardian.api.API;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A List with forestframework-specific methods.
 *
 * @param <E> the element type.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface DomainObjectCollection<E> extends Collection<E> {
    /**
     * Executes the given action against all objects in this collection, and any objects subsequently added to this collection.
     *
     * @param action The action to be executed
     */
    void all(Consumer<? super E> action);

    void order(Object... objects);
}
