package org.forestframework.config;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides a Vertx options instance from a key.
 *
 * For example, given
 *
 * <pre>
 *     forest:
 *       http:
 *         port: 8081
 *         compressionSupported: false
 *         compressionLevel: 6
 * </pre>
 *
 * You can inject the port by:
 *
 * <pre>
 * @Named("forest.http.port") Integer port;
 * </pre>
 *
 * Or
 * <pre>
 * @Named("forest.http") HttpServerOptions serverOptions.
 * </pre>
 */
public class ConfigurationProvider {
    private final JsonObject model;

    public ConfigurationProvider(JsonObject model) {
        this.model = model;
    }

    public <T> Optional<T> getInstance(String key, Class<T> klass) {
        List<String> nodes = Arrays.asList(key.split("\\."));
        JsonObject current = model;

        for (int i = 0; i < nodes.size() - 1; ++i) {
            try {
                current = current.getJsonObject(nodes.get(i));
                if (current == null) {
                    return Optional.empty();
                }
            } catch (ClassCastException e) {
                throw new RuntimeException(e);
            }
        }

        if (klass == Integer.class) {
            return (Optional<T>) Optional.of(current.getInteger(nodes.get(nodes.size() - 1)));
        } else if (klass == Long.class) {
            return (Optional<T>) Optional.of(current.getLong(nodes.get(nodes.size() - 1)));
        } else if (klass == String.class) {
            return (Optional<T>) Optional.of(current.getString(nodes.get(nodes.size() - 1)));
        } else if (klass == Map.class) {

        }

    }
}
