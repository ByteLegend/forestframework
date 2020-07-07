package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import io.forestframework.core.config.Config;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * A routing engine return 404 upon the urls which is sure to be "NotFound" (by prefix matching)
 *
 * For example, you have an interceptor handler "/*" and handler "/user/:id", then url "/unknown" is sure to be 404.
 */
@Singleton
public class Http404RequestHandler extends AbstractRequestHandler {
    private final List<String> handlerPathPrefixes;
    private final DefaultRoutings routings;

    @Inject
    public Http404RequestHandler(Vertx vertx, Injector injector, Routings routings, @Config("forest.environment") String environment) {
        super(vertx, injector, routings, environment);
        this.routings = (DefaultRoutings) routings;
        this.handlerPathPrefixes = this.routings.getRoutingPrefixes(RoutingType.HANDLER);
    }

    @Override
    public void handle(HttpServerRequest request) {
        if (handlerPathPrefixes.stream().noneMatch(prefix -> request.path().startsWith(prefix))) {
            request.response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
            request.response().putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN);
            request.response().end("NOT FOUND");
        } else {
            next(request);
        }
    }
}
