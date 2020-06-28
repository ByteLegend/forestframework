package io.forestframework.http.result;

import io.forestframework.RoutingResultProcessor;
import io.forestframework.http.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;

@Singleton
public class PlainTextResultProcessor implements RoutingResultProcessor {
    private static final CharSequence RESPONSE_TYPE_PLAIN = HttpHeaders.createOptimized("text/plain");
    private static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        routingContext.response().putHeader(HEADER_CONTENT_TYPE, RESPONSE_TYPE_PLAIN);
        if (returnValue instanceof Buffer) {
            return routingContext.response().end((Buffer) returnValue);
        } else {
            return routingContext.response().end(returnValue.toString());
        }
    }
}
