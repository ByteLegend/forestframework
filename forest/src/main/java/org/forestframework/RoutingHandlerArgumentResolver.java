package org.forestframework;

import io.vertx.ext.web.RoutingContext;
import org.forestframework.http.Routing;

import java.lang.annotation.Annotation;

public interface RoutingHandlerArgumentResolver<T, A extends Annotation> {
    T resolveArgument(Routing routing, Class<? extends T> argumentType, RoutingContext routingContext, A annotation);
}
