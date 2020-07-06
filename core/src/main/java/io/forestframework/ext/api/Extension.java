package io.forestframework.ext.api;

import com.google.inject.Injector;
import org.apiguardian.api.API;

/**
 * Allows customization for Forest application.
 */
@API(status = API.Status.STABLE, since = "1.0")
public interface Extension {
    /**
     * Configure the context which is used to create the injector.
     */
    default void beforeInjector(ExtensionContext extensionContext) {
    }

    /**
     * Configure the services inside the injector.
     */
    default void afterInjector(Injector injector) {
    }
}
