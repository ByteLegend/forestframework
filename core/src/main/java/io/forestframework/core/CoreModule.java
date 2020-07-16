package io.forestframework.core;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.routing.DefaultRoutings;
import io.forestframework.core.http.routing.Routings;
import io.forestframework.ext.api.StartupContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;

import java.util.Collections;
import java.util.List;

final class CoreModule extends AbstractModule {
    private final StartupContext startupContext;

    public CoreModule(StartupContext startupContext) {
        this.startupContext = startupContext;
    }

    @Override
    protected void configure() {
        bind(ConfigProvider.class).toInstance(startupContext.getConfigProvider());
        bind(Vertx.class).toInstance(startupContext.getVertx());
        bind(EventBus.class).toInstance(startupContext.getVertx().eventBus());
        bind(SharedData.class).toInstance(startupContext.getVertx().sharedData());
        bind(FileSystem.class).toInstance(startupContext.getVertx().fileSystem());
        bind(Class.class).annotatedWith(ForestApplication.class).toInstance(startupContext.getAppClass());
        bind(Routings.class).to(DefaultRoutings.class);
        // @formatter:off
        // components are unmodifiable after injector is created
        bind(new TypeLiteral<List<Class<?>>>() {}).annotatedWith(ComponentClasses.class).toInstance(Collections.unmodifiableList(startupContext.getComponentClasses()));
        // @formatter:on
    }
}
