package io.forestframework.core.http.result;

import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;

import static io.forestframework.core.http.OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN;
import static io.forestframework.core.http.OptimizedHeaders.HEADER_CONTENT_TYPE;

@Singleton
@FastRoutingCompatible
public class PlainTextResultProcessor implements RoutingResultProcessor {
    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        routingContext.response().putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_TEXT_PLAIN);
        if (returnValue instanceof Buffer) {
            return routingContext.response().end((Buffer) returnValue);
        } else {
            return routingContext.response().end(returnValue.toString());
        }
    }
}
