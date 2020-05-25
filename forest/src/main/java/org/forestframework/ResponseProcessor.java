package org.forestframework;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Must not invoke {@link HttpServerResponse#end()} method.
 */
public interface ResponseProcessor {
    void processResponse(RoutingContext routingContext);
}
