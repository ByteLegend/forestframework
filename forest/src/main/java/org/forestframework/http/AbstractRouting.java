package org.forestframework.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;

import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractRouting implements Routing {
    private final String path;

    private final List<HttpMethod> methods;

    private final Class<?> handlerClass;

    private final Method handlerMethod;

    private final Object handlerInstance;

    public AbstractRouting(String path, List<HttpMethod> methods, Class<?> handlerClass, Method handlerMethod, Object handlerInstance) {
        this.path = path;
        this.methods = methods;
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
        this.handlerInstance = handlerInstance;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<HttpMethod> getMethods() {
        return methods;
    }

    @Override
    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    @Override
    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public Object getHandlerInstance() {
        return handlerInstance;
    }

    @Override
    public Route configure(Route route) {
        Route ret = configurePath(route);
        for (HttpMethod method : methods) {
            ret = ret.method(method);
        }
        return ret;
    }

    protected abstract Route configurePath(Route route);
}
