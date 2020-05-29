package org.forestframework.http;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.forestframework.annotation.RouteType;

import java.lang.reflect.Method;
import java.util.List;

public interface Routing {
    RouteType getRouteType();

    List<HttpMethod> getMethods();

    String getPath();

    Class<?> getHandlerClass();

    Method getHandlerMethod();

    Object getHandlerInstance();

    Route configure(Router router);


//    private final HttpMethod httpMethod;
//    private final String path;
//    private final Method method;
//    private final Object instance;
//
//    public Routing(Object instance, Method method) {
//        this.instance = instance;
//        this.method = method;
//        this.path = method.getAnnotation(Route.class).value();
//        this.httpMethod = method.getAnnotation(Route.class).method();
//    }
}