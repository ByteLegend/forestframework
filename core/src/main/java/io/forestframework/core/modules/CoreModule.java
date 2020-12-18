package io.forestframework.core.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.forestframework.core.Component;
import io.forestframework.core.ForestApplication;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.Router;
import io.forestframework.ext.api.ApplicationContext;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "0.1")
public class CoreModule extends AbstractModule {
    private final ApplicationContext applicationContext;

    public CoreModule(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void configure() {
        bind(ApplicationContext.class).toInstance(applicationContext);
        bind(ConfigProvider.class).toInstance(applicationContext.getConfigProvider());
        bind(Vertx.class).toInstance(applicationContext.getVertx());
        bind(EventBus.class).toInstance(applicationContext.getVertx().eventBus());
        bind(SharedData.class).toInstance(applicationContext.getVertx().sharedData());
        bind(FileSystem.class).toInstance(applicationContext.getVertx().fileSystem());
        bind(Class.class).annotatedWith(ForestApplication.class).toInstance(applicationContext.getAppClass());

        bindScope(ForestApplication.class, Scopes.SINGLETON);
        bindScope(Router.class, Scopes.SINGLETON);
        bindScope(Component.class, Scopes.SINGLETON);
    }
}
