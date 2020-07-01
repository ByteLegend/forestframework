package io.forestframework.core.http.param;

import io.vertx.ext.web.RoutingContext;
import io.forestframework.core.http.routing.Routing;

public interface RoutingParameterResolver<T> {
    T resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex);
}
