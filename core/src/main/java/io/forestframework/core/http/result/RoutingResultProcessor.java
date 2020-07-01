package io.forestframework.core.http.result;

import io.vertx.ext.web.RoutingContext;
import io.forestframework.core.http.routing.Routing;

/**
 * Usually, a processor returns returnValue again, so the next processor can process it.
 */
public interface RoutingResultProcessor {
    Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue);
}
