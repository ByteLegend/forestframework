package org.forestframework;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.SharedData;
import org.apache.commons.io.IOUtils;
import org.forestframework.annotation.ApplicationClass;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.bootstrap.HttpServerStarter;
import org.forestframework.config.ConfigProvider;
import org.forestframework.ext.AutoScanComponentClassConfigurer;
import org.forestframework.ext.RouteAnnotationRoutingConfigurer;
import org.forestframework.ext.api.ComponentClassConfigurer;
import org.forestframework.utils.ComponentScanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class Forest {
    private static final List<Class<?>> CORE_EXTENSION_CLASSES = unmodifiableList(asList(AutoScanComponentClassConfigurer.class, RouteAnnotationRoutingConfigurer.class));
    private static final Logger LOGGER = LoggerFactory.getLogger(Forest.class);

    public static void run(Class<?> applicationClass) {
        try {
            Injector injector = createInjector(applicationClass);
            injector.getInstance(HttpServerStarter.class).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Injector createInjector(Class<?> applicationClass) {
        ForestApplication annotation = applicationClass.getAnnotation(ForestApplication.class);
        if (annotation == null) {
            throw new RuntimeException();
        } else {
            Vertx vertx = Vertx.vertx();
            ConfigProvider configProvider = createConfigProvider();
            Injector bootstrapInjector = Guice.createInjector(new CoreModule(vertx, applicationClass, configProvider));
            List<Class<?>> extensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);
            extensionClasses.addAll(Arrays.asList(annotation.include()));
            List<Class<?>> componentClasses = getComponentClasses(bootstrapInjector, extensionClasses);
            return createAppInjector(bootstrapInjector, vertx, applicationClass, configProvider, componentClasses);
        }
    }

    private static ConfigProvider createConfigProvider() {
        try {
            InputStream is = Forest.class.getResourceAsStream("/forest.yml");
            String yaml = IOUtils.toString(is, Charset.defaultCharset());
            return ConfigProvider.fromYaml(yaml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Module> getModules(Injector coreInjector, List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(ComponentScanUtils::isGuiceModule)
                .map(moduleClass -> (Module) coreInjector.getInstance(moduleClass))
                .collect(Collectors.toList());
    }

    private static List<Class<?>> getComponentClasses(Injector coreInjector, List<Class<?>> extensionClasses) {
        LinkedHashSet<Class<?>> componentClasses = new LinkedHashSet<>();
        for (Class<?> extensionClass : extensionClasses) {
            if (extensionClass.isAssignableFrom(Module.class)) {
                componentClasses.add(extensionClass);
            } else if (ComponentClassConfigurer.class.isAssignableFrom(extensionClass)) {
                coreInjector.getInstance((Class<ComponentClassConfigurer>) extensionClass).configure(componentClasses);
            }
        }
        return new ArrayList<>(componentClasses);
    }


    private static Injector createAppInjector(Injector bootstrapInjector,
                                              Vertx vertx,
                                              Class<?> applicationClass,
                                              ConfigProvider configProvider,
                                              List<Class<?>> componentClasses) {
        Module current = new ExtensionModule(vertx, applicationClass, configProvider, componentClasses);
        for (Module module : getModules(bootstrapInjector, componentClasses)) {
            current = Modules.override(current).with(module);
        }
        return Guice.createInjector(current);
    }

    /**
     * Bootstrap injector only contains core modules, including configuration provider, which is mainly used to inject configuration values
     * to an extension instance.
     *
     * @return the bootstrap injector
     */
//    private static Injector createBootstrapInjector() {
//    }


    public static class CoreModule extends AbstractModule {
        private final Vertx vertx;
        private final Class<?> applicationClass;
        private final ConfigProvider configProvider;

        public CoreModule(Vertx vertx,
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
            bind(Class.class).annotatedWith(ApplicationClass.class).toInstance(applicationClass);
        }
    }

    private static class ExtensionModule extends CoreModule {
        private final List<Class<?>> componentClasses;

        public ExtensionModule(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Class<?>> componentClasses) {
            super(vertx, applicationClass, configProvider);
            this.componentClasses = componentClasses;
        }

        @Override
        protected void configure() {
            super.configure();
            bind(new TypeLiteral<List<Class<?>>>() {
            }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }
}

