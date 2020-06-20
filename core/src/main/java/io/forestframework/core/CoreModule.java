package io.forestframework.core;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.forestframework.annotation.RoutingType;
import io.forestframework.config.ConfigProvider;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;
import io.forestframework.http.Routing;
import io.forestframework.http.Routings;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class CoreModule extends AbstractModule {
    private final ExtensionContext extensionContext;
    private final Map<RoutingType, List<Routing>> routings = initRoutings();

    public CoreModule(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    private Map<RoutingType, List<Routing>> initRoutings() {
        return Stream.of(RoutingType.values())
                .collect(Collectors.toConcurrentMap(key -> key, value -> new ArrayList<>()));
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
        bind(new TypeLiteral<Map<RoutingType, List<Routing>>>() {}).annotatedWith(Routings.class).toInstance(routings);
        // @formatter:on
    }
}
