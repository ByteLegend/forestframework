package io.forestframework.core.http.routing;

import com.google.inject.Singleton;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * For internal usage only.
 */
@Singleton
public class RequestHandlerChain implements RequestHandler {
    private final List<RequestHandler> handlers;

    @Inject
    public RequestHandlerChain(Http404RequestHandler http404RequestHandler,
                               FastRequestHandler fastRequestHandler,
                               RouterRequestHandler routerRequestHandler) {
        this.handlers = Arrays.asList(http404RequestHandler, fastRequestHandler, routerRequestHandler);
        http404RequestHandler.setNext(fastRequestHandler);
        fastRequestHandler.setNext(routerRequestHandler);
    }

    @Override
    public void handle(HttpServerRequest request) {
        handlers.get(0).handle(request);
    }
}
