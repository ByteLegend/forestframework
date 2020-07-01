package io.forestframework.ext.api;

import com.google.inject.Injector;

/**
 * Allows customization for Forest application.
 */
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
