package org.forestframework;

import com.google.inject.Injector;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.bootstrap.HttpServerStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Forest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Forest.class);

    public static void run(Class<?> applicationClass) throws Exception {
        try {
            Injector injector = createInjector(applicationClass);
            injector.getInstance(HttpServerStarter.class).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

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

    public static <T> T instantiate(Class<T> klass) {
        try {
            return klass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

