package io.forestframework.core.http;

import io.forestframework.core.http.routing.Route;

import java.lang.reflect.Method;

public class DefaultSockJSRouting extends DefaultRouting {
    public DefaultSockJSRouting(Route route, Method handlerMethod) {
        super(route, handlerMethod);
    }
}
