package io.forestframework.utils;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface Sealable {
    /**
     * Seals this object, preventing any further modification to this object.
     */
    void seal();

    default boolean isSealed() {
        return false;
    }

    default void checkSealed() {
        if (isSealed()) {
            throw new UnsupportedOperationException("This object is already sealed and immutable!");
        }
    }
}
