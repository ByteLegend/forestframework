package org.forestframework.http;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.forestframework.annotation.RouteType;

import java.lang.reflect.Method;
import java.util.List;

public class PathRouting extends AbstractRouting {
    public PathRouting(String path, List<HttpMethod> methods, Class<?> handlerClass, Method handlerMethod, Object handlerInstance) {
        super(path, methods, handlerClass, handlerMethod, handlerInstance);
    }

    @Override
    protected Route configurePath(Router router) {
        return router.route(getPath());
    }

    @Override
    public RouteType getRouteType() {
        return RouteType.HANDLER;
    }
}
