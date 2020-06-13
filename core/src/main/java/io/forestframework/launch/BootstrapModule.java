package io.forestframework.launch;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import io.forestframework.annotation.ApplicationClass;
import io.forestframework.config.ConfigProvider;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;

final class BootstrapModule extends AbstractModule {
    private final Vertx vertx;
    private final Class<?> applicationClass;
    private final ConfigProvider configProvider;

    BootstrapModule(Vertx vertx,
                    Class<?> applicationClass,
                    ConfigProvider configProvider) {
        this.vertx = vertx;
        this.applicationClass = applicationClass;
        this.configProvider = configProvider;
    }

    @Override
    protected <T> Provider<T> getProvider(Key<T> key) {
        return super.getProvider(key);
    }

    @Override
    protected <T> Provider<T> getProvider(Class<T> type) {
        return super.getProvider(type);
    }

    @Override
    protected void requireBinding(Key<?> key) {
        super.requireBinding(key);
    }

    @Override
    protected void configure() {
        bind(ConfigProvider.class).toInstance(configProvider);
        bind(Vertx.class).toInstance(vertx);
        bind(EventBus.class).toInstance(vertx.eventBus());
        bind(SharedData.class).toInstance(vertx.sharedData());
        bind(FileSystem.class).toInstance(vertx.fileSystem());
        bind(Class.class).annotatedWith(ApplicationClass.class).toInstance(applicationClass);
    }
}
