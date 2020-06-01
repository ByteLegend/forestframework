package org.forestframework;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.SharedData;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.bootstrap.HttpServerStarter;
import org.forestframework.ext.AutoScanComponentClassConfigurer;
import org.forestframework.ext.RouteAnnotationRoutingConfigurer;
import org.forestframework.ext.api.ComponentClassConfigurer;
import org.forestframework.utils.ComponentScanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Injector coreInjector = Guice.createInjector(new CoreModule(applicationClass));
            List<Class<?>> extensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);
            extensionClasses.addAll(Arrays.asList(annotation.include()));
            List<Class<?>> componentClasses = getComponentClasses(coreInjector, extensionClasses);
            return createAppInjector(coreInjector, componentClasses);
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
            } else if (extensionClass.isAssignableFrom(ComponentClassConfigurer.class)) {
                coreInjector.getInstance((Class<ComponentClassConfigurer>) extensionClass).configure(componentClasses);
            }
        }
        return new ArrayList<>(componentClasses);
    }


    private static Injector createAppInjector(Injector coreInjector, List<Class<?>> componentClasses) {
        Module current = new ComponentClassesModule(componentClasses);
        for (Module module : getModules(coreInjector, componentClasses)) {
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
        private final Vertx vertx = Vertx.vertx();
        private final Class<?> applicationClass;

        public CoreModule(Class<?> applicationClass) {
            this.applicationClass = applicationClass;
        }
        //        private final List<Class<?>> componentClasses;

//        public CoreModule(List<Class<?>> componentClasses) {
//            this.componentClasses = componentClasses;
//        }

        @Override
        protected <T> Provider<T> getProvider(Key<T> key) {
            return super.getProvider(key);
        }

        @Override
        protected void configure() {
            bind(Vertx.class).toInstance(vertx);
            bind(EventBus.class).toInstance(vertx.eventBus());
            bind(SharedData.class).toInstance(vertx.sharedData());
            bind(Class.class).annotatedWith(Names.named("forest.application.class")).toInstance(applicationClass);
//            bind(new TypeLiteral<List<Class<?>>>() {
//            }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }

    private static class ComponentClassesModule extends AbstractModule {
        private final List<Class<?>> componentClasses;

        public ComponentClassesModule(List<Class<?>> componentClasses) {
            this.componentClasses = componentClasses;
        }

        @Override
        protected void configure() {
            bind(new TypeLiteral<List<Class<?>>>() {
            }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }
}

