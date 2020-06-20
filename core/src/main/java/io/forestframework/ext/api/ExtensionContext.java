package io.forestframework.ext.api;

import io.forestframework.config.ConfigProvider;
import io.vertx.core.Vertx;

import java.util.List;

public class ExtensionContext {
    private final Vertx vertx;
    private final Class<?> applicationClass;
    private final ConfigProvider configProvider;
    private final List<Class<?>> componentClasses;

    public ExtensionContext(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Class<?>> componentClasses) {
        this.vertx = vertx;
        this.applicationClass = applicationClass;
        this.configProvider = configProvider;
        this.componentClasses = componentClasses;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public List<Class<?>> getComponentClasses() {
        return componentClasses;
    }
}
