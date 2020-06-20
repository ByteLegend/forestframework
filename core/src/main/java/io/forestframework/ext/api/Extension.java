package io.forestframework.ext.api;

import com.google.inject.Injector;

public interface Extension {
    default void beforeInjector(ExtensionContext extensionContext) {
    }

    default void configure(Injector injector) {
    }
}
