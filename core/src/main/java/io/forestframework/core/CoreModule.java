package io.forestframework.core;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.forestframework.annotation.RoutingType;
import io.forestframework.config.ConfigProvider;
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
    private final Vertx vertx;
    private final Class<?> applicationClass;
    private final ConfigProvider configProvider;
    private final List<Class<?>> componentClasses;
    private final Map<RoutingType, List<Routing>> routings = initRoutings();

    public CoreModule(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Class<?>> componentClasses) {
        this.vertx = vertx;
        this.applicationClass = applicationClass;
        this.configProvider = configProvider;
        // components are unmodifiable after injector is created
        this.componentClasses = Collections.unmodifiableList(componentClasses);
    }

    private Map<RoutingType, List<Routing>> initRoutings() {
        return Stream.of(RoutingType.values())
                .collect(Collectors.toConcurrentMap(key -> key, value -> new ArrayList<>()));
    }


    @Override
    protected void configure() {
        bind(ConfigProvider.class).toInstance(configProvider);
        bind(Vertx.class).toInstance(vertx);
        bind(EventBus.class).toInstance(vertx.eventBus());
        bind(SharedData.class).toInstance(vertx.sharedData());
        bind(FileSystem.class).toInstance(vertx.fileSystem());
        bind(Class.class).annotatedWith(ForestApplication.class).toInstance(applicationClass);
        bind(new TypeLiteral<List<Class<?>>>() {
        }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        bind(new TypeLiteral<Map<RoutingType, List<Routing>>>() {
        }).annotatedWith(Routings.class).toInstance(routings);
    }
}
