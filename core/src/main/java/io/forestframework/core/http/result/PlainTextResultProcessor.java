package io.forestframework.core.http.result;

import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.HttpContext;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import javax.inject.Singleton;

import static io.forestframework.core.http.OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN;
import static io.forestframework.core.http.OptimizedHeaders.HEADER_CONTENT_TYPE;

@Singleton
@FastRoutingCompatible
public class PlainTextResultProcessor implements RoutingResultProcessor {
    @Override
    public Object processResponse(WebContext webContext, Routing routing, Object returnValue) {
        HttpContext context = (HttpContext) webContext;

        HttpServerResponse response = context.response();
        response.putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_TEXT_PLAIN);
        if (returnValue instanceof Buffer) {
            return response.write((Buffer) returnValue);
        } else {
            return response.write(returnValue.toString());
        }
    }
}
