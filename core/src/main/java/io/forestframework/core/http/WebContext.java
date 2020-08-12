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
}
