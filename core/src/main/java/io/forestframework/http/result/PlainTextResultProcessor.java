package io.forestframework.http.result;

import io.forestframework.RoutingResultProcessor;
import io.forestframework.http.Routing;
import io.vertx.ext.web.RoutingContext;

public class PlainTextResultProcessor implements RoutingResultProcessor {
    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        routingContext.response().putHeader("Content-Type", "text/plain");
        return routingContext.response().end(returnValue.toString());
    }
}
