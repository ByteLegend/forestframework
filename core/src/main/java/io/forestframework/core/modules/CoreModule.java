package io.forestframework.core.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import io.forestframework.core.Component;
import io.forestframework.core.ComponentClasses;
import io.forestframework.core.ForestApplication;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.Router;
import io.forestframework.ext.api.StartupContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;

@API(status = API.Status.INTERNAL, since = "0.1")
public class CoreModule extends AbstractModule {
    private final StartupContext startupContext;

    public CoreModule(StartupContext startupContext) {
        this.startupContext = startupContext;
    }

    @Override
    protected void configure() {
        bind(StartupContext.class).toInstance(startupContext);
        bind(ConfigProvider.class).toInstance(startupContext.getConfigProvider());
        bind(Vertx.class).toInstance(startupContext.getVertx());
        bind(EventBus.class).toInstance(startupContext.getVertx().eventBus());
        bind(SharedData.class).toInstance(startupContext.getVertx().sharedData());
        bind(FileSystem.class).toInstance(startupContext.getVertx().fileSystem());
        bind(Class.class).annotatedWith(ForestApplication.class).toInstance(startupContext.getAppClass());

        bindScope(ForestApplication.class, Scopes.SINGLETON);
        bindScope(Router.class, Scopes.SINGLETON);
        bindScope(Component.class, Scopes.SINGLETON);

        // @formatter:off
        // components are unmodifiable after injector is created
        bind(new TypeLiteral<List<Class<?>>>() { }).annotatedWith(ComponentClasses.class).toInstance(Collections.unmodifiableList(startupContext.getComponentClasses()));
        // @formatter:on
    }
}
