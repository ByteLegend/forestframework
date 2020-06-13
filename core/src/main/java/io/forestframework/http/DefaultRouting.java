package io.forestframework.http;

import io.forestframework.annotation.Route;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultRouting implements Routing {
    private final String path;

    private final String regexPath;

    private final List<HttpMethod> methods;

    private final Class<?> handlerClass;

    private final Method handlerMethod;

    private final Object handlerInstance;

    public DefaultRouting(Route route, Class<?> handlerClass, Method handlerMethod, Object handlerInstance) {
        this(route.value(), route.regex(), Arrays.asList(route.methods()), handlerClass, handlerMethod, handlerInstance);
    }

    public DefaultRouting(String path, String regexPath, List<HttpMethod> methods, Class<?> handlerClass, Method handlerMethod, Object handlerInstance) {
        this.path = path;
        this.regexPath = regexPath;
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
    public String getRegexPath() {
        return regexPath;
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

}
