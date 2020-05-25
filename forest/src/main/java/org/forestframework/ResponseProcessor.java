package org.forestframework;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.forestframework.http.Routing;

import java.lang.annotation.Annotation;

/**
 * Must not invoke {@link HttpServerResponse#end()} method.
 */
public interface ResponseProcessor<A extends Annotation> {
    void processResponse(RoutingContext routingContext, Routing routing, Object returnValue, A annotation);
}
