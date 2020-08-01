package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.inject.Injector;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.param.ParameterResolver;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.result.ResultProcessor;
import io.forestframework.core.http.result.RoutingResultProcessor;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Routing} instance represents not only a {@link Route}, but also all necessary parts to process the
 * {@link io.vertx.ext.web.RoutingContext}/{@link io.vertx.core.http.HttpServerRequest}/{@link io.vertx.core.http.HttpServerResponse}
 * on this route. Typically, a route is processed with the following steps:
 * <ol>
 *     <li>1. Locate the routing handler (a {@link Method} instance).</li>
 *     <li>2. Resolve all parameters from the routing handler's parameters.</li>
 *     <li>3. Invoke the handler and get return value.</li>
 *     <li>4. Process the return value in various ways.</li>
 * </ol>
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface Routing {
    Method getHandlerMethod();

    default RoutingType getType() {
        return RoutingType.HANDLER;
    }

    default List<HttpMethod> getMethods() {
        return Collections.singletonList(HttpMethod.GET);
    }

    default Object getHandlerInstance(Injector injector) {
        return injector.getInstance(getHandlerMethod().getDeclaringClass());
    }

    default String getPath() {
        return "";
    }

    default String getRegexPath() {
        return "";
    }

    default RoutingParameterResolver<?> getParameterResolver(Injector injector, int index) {
        ParameterResolver resolver = AnnotationMagic.getOneAnnotationOnMethodParameterOrNull(getHandlerMethod(), index, ParameterResolver.class);
        if (resolver == null) {
            return null;
        }
//        if (resolver == null) {
//            throw new IllegalArgumentException("Don't know how to resolve param " + index + " of " + getHandlerMethod());
//        }
        return injector.getInstance(resolver.resolver());
    }

    default List<RoutingResultProcessor> getResultProcessors(Injector injector, Object returnValue) {
        return AnnotationMagic.getAnnotationsOnMethod(getHandlerMethod(), ResultProcessor.class)
                .stream()
                .map(ResultProcessor::by)
                .map(injector::getInstance)
                .collect(Collectors.toList());
    }
}

