package io.forestframework.core.http.routing;

import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.core.http.param.ParameterResolver;
import io.forestframework.core.http.result.ResultProcessor;
import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.core.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Routing} instance represents not only a {@link Route}, but also all necessary parts to process the
 * {@link io.vertx.ext.web.RoutingContext}/{@link io.vertx.core.http.HttpServerRequest}/{@link io.vertx.core.http.HttpServerResponse}
 * on this route. Typically, a route is processed with the following steps:
 * <ol>
 *     <li>1. Locate the routing handler (a {@link Method} instance).</li>
 *     <li>2. Resolve all parameters from the routing handler's parameters.</li>
 *     <li>3. Invoke the hander and get return value.</li>
 *     <li>4. Process the return value in various ways.</li>
 * </ol>
 */
public interface Routing {
    default RoutingType getType() {
        return RoutingType.HANDLER;
    }

    List<HttpMethod> getMethods();

    default Class<?> getHandlerClass() {
        return getHandlerMethod().getClass();
    }

    Method getHandlerMethod();

    default Object getHandlerInstance() {
        return null;
    }

    default String getPath() {
        return "";
    }

    default String getRegexPath() {
        return "";
    }

    default Class<? extends RoutingParameterResolver<?>> getParameterResolver(int index) {
        ParameterResolver resolver = AnnotationMagic.getOneAnnotationOnMethodParameter(getHandlerMethod(), index, ParameterResolver.class);
        if (resolver == null) {
            throw new IllegalArgumentException("Don't know how to resolve param " + index + " of " + getHandlerMethod());
        }
        return resolver.by();
    }

    default List<Class<? extends RoutingResultProcessor>> getResultProcessors(Object returnValue) {
        return AnnotationMagic.getAnnotationsOnMethod(getHandlerMethod(), ResultProcessor.class)
                .stream()
                .map(ResultProcessor::by)
                .collect(Collectors.toList());
    }
}

