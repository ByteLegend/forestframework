package org.forestframework;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import kotlin.coroutines.Continuation;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.bootstrap.HttpServerStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Forest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Forest.class);

    public static void run(Class<?> applicationClass) throws Exception {
        Injector injector = createInjector(applicationClass);
        injector.getInstance(HttpServerStarter.class).start();

//        Vertx vertx = Vertx.vertx();
//        List<Class<?>> componentClasses = scanComponentClasses(applicationClass);
//        List<Module> modules = getModuleClasses(componentClasses);
//        modules.add(new CoreModule(vertx));
//
//        Injector injector = Guice.createInjector(modules);
//        vertx.exceptionHandler(e -> LOGGER.error("", e));
//        List<Class<?>> routerClasses = getRouterClasses(componentClasses);
//        vertx.deployVerticle(new HttpVerticle(injector, vertx, routerClasses));
    }

    private static Injector createInjector(Class<?> applicationClass) {
        ForestApplication annotation = applicationClass.getAnnotation(ForestApplication.class);
        if (annotation == null) {
            throw new RuntimeException();
        } else {
            return instantiate(annotation.injectorCreatedBy()).createInjector(applicationClass);
        }
    }

    private static List<Class<?>> getRouterClasses(List<Class<?>> componentClasses) {
        return componentClasses.stream().filter(Forest::isRouter).collect(Collectors.toList());
    }

    private static List<Module> getModuleClasses(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(Forest::isGuavaModule)
                .map(klass -> (Class<? extends Module>) klass)
                .map(Forest::instantiate)
                .collect(Collectors.toList());
    }

    public static <T> T instantiate(Class<T> klass) {
        try {
            return klass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean isGuavaModule(Class<?> klass) {
        return Module.class.isAssignableFrom(klass);
    }

    private static boolean isRouter(Class<?> klass) {
        return klass.isAnnotationPresent(org.forestframework.annotation.Router.class);
    }

    private static boolean isForestComponentClass(Class<?> klass) {
        return isGuavaModule(klass) || isRouter(klass);
    }
}

