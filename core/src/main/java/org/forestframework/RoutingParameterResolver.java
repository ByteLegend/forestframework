package org.forestframework;

import io.vertx.ext.web.RoutingContext;
import org.forestframework.http.Routing;

public interface RoutingParameterResolver<T> {
    T resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex);
}
