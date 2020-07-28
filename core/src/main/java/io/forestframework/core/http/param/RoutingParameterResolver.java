package io.forestframework.core.http.param;

import io.forestframework.core.http.routing.Routing;
import io.vertx.ext.web.RoutingContext;

public interface RoutingParameterResolver<T> {
    T resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex);
}
