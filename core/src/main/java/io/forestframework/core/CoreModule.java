package io.forestframework.core;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.ExtensionContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;

import java.util.Collections;
import java.util.List;

final class CoreModule extends AbstractModule {
    private final ExtensionContext extensionContext;

    public CoreModule(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    @Override
    protected void configure() {
        bind(ConfigProvider.class).toInstance(extensionContext.getConfigProvider());
        bind(Vertx.class).toInstance(extensionContext.getVertx());
        bind(EventBus.class).toInstance(extensionContext.getVertx().eventBus());
        bind(SharedData.class).toInstance(extensionContext.getVertx().sharedData());
        bind(FileSystem.class).toInstance(extensionContext.getVertx().fileSystem());
        bind(Class.class).annotatedWith(ForestApplication.class).toInstance(extensionContext.getApplicationClass());
        // @formatter:off
        // components are unmodifiable after injector is created
        bind(new TypeLiteral<List<Class<?>>>() {}).annotatedWith(ComponentClasses.class).toInstance(Collections.unmodifiableList(extensionContext.getComponentClasses()));
        // @formatter:on
    }
}
