package io.forestframework.core.http.result;

import io.forestframework.core.http.routing.Routing;
import io.vertx.ext.web.RoutingContext;

/**
 * Usually, a processor returns returnValue again, so the next processor can process it.
 */
public interface RoutingResultProcessor {
    Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue);
}
