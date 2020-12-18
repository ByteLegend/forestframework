package io.forestframework.ext.api;

import com.google.inject.GuiceExt;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.LookupInterceptor;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.forestframework.core.config.Config;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.modules.CoreModule;
import io.forestframework.utils.SealableList;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class DefaultApplicationContext implements ApplicationContext, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationContext.class);

    private final Vertx vertx;
    private final Class<?> appClass;
    private final ConfigProvider configProvider;
    private final List<Extension> extensions;
    private final SealableList<Module> modules = new SealableList<>();
    private final SealableList<Class<?>> componentClasses = new SealableList<>();
    private Injector injector;

    public DefaultApplicationContext(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Extension> extensions) {
        this.vertx = vertx;
        this.appClass = applicationClass;
        this.configProvider = configProvider;
        this.extensions = Collections.unmodifiableList(extensions);
    }

    public Vertx getVertx() {
        return vertx;
    }

    public Class<?> getAppClass() {
        return appClass;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    @Override
    public List<Module> getModules() {
        return modules;
    }

    @Override
    public List<Class<?>> getComponents() {
        return componentClasses;
    }

    public Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("The application hasn't started yet!");
        }
        return injector;
    }

    public void start() {
        configureComponents();
        createInjector();
        configureApplication();
    }

    protected void configureComponents() {
        // 1. Start. Instantiate all extensions and configure components.
        getExtensions().forEach(extension -> {
            LOGGER.debug("Apply extension beforeInjector: {}", extension.getClass());
            extension.start(this);
        });
    }

    protected void createInjector() {
        // 2. Filter out all Module classes, instantiate them and create the injector
        injector = createInjector(this, modules);
        LOGGER.debug("Injector created successfully with {}", modules);
        modules.seal();
    }

    protected void configureApplication() {
        // 4. Configure the application
        getExtensions().forEach(extension -> extension.configure(injector));
    }

    @Override
    public void close() throws Exception {
        for (Extension extension : getExtensions()) {
            extension.close();
        }
    }

    private static Injector createInjector(ApplicationContext applicationContext, List<Module> modules) {
        Module current = new CoreModule(applicationContext);
        for (Module module : modules) {
            current = Modules.override(current).with(module);
        }
        return GuiceExt.createInjector(new LookupInterceptor() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T intercept(Key<T> key) {
                if (key.getAnnotation() != null && key.getAnnotation().annotationType() == Config.class) {
                    return (T) applicationContext.getConfigProvider().getInstance(((Config) key.getAnnotation()).value(), key.getTypeLiteral().getRawType());
                }
                return null;
            }
        }, current);
    }
}
