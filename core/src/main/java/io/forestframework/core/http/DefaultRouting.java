package io.forestframework.core.http;

import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DefaultRouting implements Routing {
    private final RoutingType type;

    private final String path;

    private final String regexPath;

    private final List<HttpMethod> methods;

    private final Method handlerMethod;

    public DefaultRouting(Route route, Method handlerMethod) {
        this(route.type(), route.value(), route.regex(), Arrays.asList(route.methods()), handlerMethod);
    }

    public DefaultRouting(RoutingType type, String path, String regexPath, List<HttpMethod> methods, Method handlerMethod) {
        this.type = type;
        this.path = path;
        this.regexPath = regexPath;
        this.methods = methods;
        this.handlerMethod = handlerMethod;
    }

    @Override
    public RoutingType getType() {
        return type;
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
    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public String toString() {
        return "DefaultRouting{" +
                "path='" + path + '\'' +
                ", regexPath='" + regexPath + '\'' +
                ", methods=" + methods +
                ", handlerMethod=" + handlerMethod +
                '}';
    }
}
