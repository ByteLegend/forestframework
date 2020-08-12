package io.forestframework.core.http.param;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

public interface RoutingParameterResolver<T> {
    T resolveParameter(WebContext context, Routing routing, int paramIndex);
}
