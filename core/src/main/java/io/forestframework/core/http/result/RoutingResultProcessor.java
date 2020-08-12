package io.forestframework.core.http.result;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

/**
 * Usually, a processor returns returnValue again, so the next processor can process it.
 */
public interface RoutingResultProcessor {
    Object processResponse(WebContext context, Routing routing, Object returnValue);
}
