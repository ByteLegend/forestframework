package org.forestframework;

import io.vertx.ext.web.RoutingContext;
import org.forestframework.http.Routing;

/**
 * Usually, a processor returns returnValue again, so the next processor can process it.
 */
public interface RoutingResultProcessor {
    Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue);
}
