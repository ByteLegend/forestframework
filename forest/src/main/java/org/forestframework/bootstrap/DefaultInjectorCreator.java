package org.forestframework.bootstrap;

import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.Vertx;
import org.forestframework.Forest;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.Router;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.reflect.ClassPath.from;

public class DefaultInjectorCreator implements InjectorCreator {
    @Override
    public Injector createInjector(Class<?> applicationClass) {
        Vertx vertx = Vertx.vertx();
        List<Class<?>> componentClasses = scanComponentClasses(applicationClass);
        List<Module> modules = getModuleClasses(componentClasses);
        modules.add(new CoreModule(vertx));
        return Guice.createInjector(modules);
    }


    protected List<Class<?>> scanComponentClasses(Class<?> applicationClass) {
        String packageName = applicationClass.getPackage().getName();
        try {
            return from(applicationClass.getClassLoader())
                    .getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(this::isForestComponentClass)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected boolean isForestComponentClass(Class<?> klass) {
        return isGuavaModule(klass) || isRouter(klass);
    }

    private boolean isGuavaModule(Class<?> klass) {
        return Module.class.isAssignableFrom(klass);
    }

    private boolean isRouter(Class<?> klass) {
        return klass.isAnnotationPresent(Router.class);
    }

    private List<Module> getModuleClasses(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(this::isGuavaModule)
                .map(klass -> (Class<? extends Module>) klass)
                .map(Forest::instantiate)
                .collect(Collectors.toList());
    }

    public static class CoreModule extends AbstractModule {
        private final Vertx vertx;
        private final List<Class<?>> componentClasses;

        public CoreModule(Vertx vertx, List<Class<?>> componentClasses) {
            this.vertx = vertx;
            this.componentClasses = componentClasses;
        }

        @Override
        protected void configure() {
            bind(Vertx.class).toInstance(vertx);
            bind(List.class).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }
}
