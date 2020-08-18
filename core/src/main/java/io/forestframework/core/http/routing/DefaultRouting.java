package io.forestframework.core.http.routing;

import io.forestframework.core.http.HttpMethod;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.List;

/**
 * For internal usage only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultRouting implements Routing {
    private final boolean blocking;

    private final RoutingType type;

    private final String path;

    private final String regexPath;

    private final List<HttpMethod> methods;

    private final Method handlerMethod;

    public DefaultRouting(boolean blocking, RoutingType type, String path, String regexPath, List<HttpMethod> methods, Method handlerMethod) {
        this.blocking = blocking;
        this.type = type;
        this.path = path;
        this.regexPath = regexPath;
        this.methods = methods;
        this.handlerMethod = handlerMethod;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
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
