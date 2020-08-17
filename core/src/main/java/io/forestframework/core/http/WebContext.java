package io.forestframework.core.http;

import java.util.Map;

/**
 * Represents the context for the handling of a request.
 */
public interface WebContext {
    WebContext put(String key, Object obj);

    <T> T get(String key);

    <T> T remove(String key);

    Map<String, Object> data();

    /**
     * Returns a map of named parameters as defined in path declaration with their actual values
     *
     * @return the map of named parameters
     */
    Map<String, String> pathParams();

    /**
     * Gets the value of a single path parameter
     *
     * @param name the name of parameter as defined in path declaration
     * @return the actual value of the parameter or null if it doesn't exist
     */
    default String pathParam(String name) {
        return pathParams().get(name);
    }
}
