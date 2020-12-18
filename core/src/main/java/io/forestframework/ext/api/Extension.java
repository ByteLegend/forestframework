package io.forestframework.ext.api;

import com.google.inject.Injector;
import org.apiguardian.api.API;

/**
 * Allows customization for Forest application.
 */
@API(status = API.Status.STABLE, since = "1.0")
public interface Extension extends AutoCloseable {
    /**
     * Configure the context which is used to create the injector.
     */
    default void start(ApplicationContext applicationContext) {
    }

    /**
     * Configure the services inside the injector.
     */
    default void configure(Injector injector) {
    }

    @Override
    default void close() throws Exception {
    }
}
