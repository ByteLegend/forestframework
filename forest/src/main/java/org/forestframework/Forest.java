package org.forestframework;

import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.bootstrap.HttpServerStarter;
import org.forestframework.utils.ComponentScanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.reflect.ClassPath.from;
import static org.forestframework.utils.ComponentScanUtils.isGuavaModule;
import static org.forestframework.utils.ComponentScanUtils.isRouter;

public class Forest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Forest.class);

    public static void run(Class<?> applicationClass) throws Exception {
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
            List<Class<?>> componentClasses = scanComponentClasses(applicationClass, annotation);
            CoreModule coreModule = new CoreModule(componentClasses);
            return createOverridingInjector(coreModule, customModules(componentClasses));
        }
    }

    private static Injector createOverridingInjector(Module coreModule, List<Module> customModules) {
        Module current = coreModule;
        for (Module module : customModules) {
            current = Modules.override(current).with(module);
        }
        return Guice.createInjector(current);
    }

    @SuppressWarnings("unchecked")
    private static List<Module> customModules(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(ComponentScanUtils::isGuavaModule)
                .map(klass -> (Class<? extends Module>) klass)
                .map(Forest::instantiate)
                .collect(Collectors.toList());
    }

    private static List<Class<?>> scanComponentClasses(Class<?> applicationClass, ForestApplication annotation) {
        String packageName = applicationClass.getPackage().getName();
        try {
            LinkedHashSet<Class<?>> componentClasses = from(applicationClass.getClassLoader())
                    .getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(Forest::isComponentClass)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            componentClasses.addAll(Arrays.asList(annotation.include()));
            Stream.of(annotation.includeName()).map(Forest::loadClass).forEach(componentClasses::add);
            return new ArrayList<>(componentClasses);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isComponentClass(Class<?> klass) {
        return isGuavaModule(klass) || isRouter(klass);
    }

    private List<Module> getModuleClasses(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(ComponentScanUtils::isGuavaModule)
                .map(klass -> (Class<? extends Module>) klass)
                .map(Forest::instantiate)
                .collect(Collectors.toList());
    }

    public static class CoreModule extends AbstractModule {
        private final Vertx vertx = Vertx.vertx();
        private final List<Class<?>> componentClasses;

        public CoreModule(List<Class<?>> componentClasses) {
            this.componentClasses = componentClasses;
        }

        @Override
        protected void configure() {
            bind(Vertx.class).toInstance(vertx);
            bind(EventBus.class).toInstance(vertx.eventBus());
            bind(new TypeLiteral<List<Class<?>>>() {
            }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }

    public static <T> T instantiate(Class<T> klass) {
        try {
            return klass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

