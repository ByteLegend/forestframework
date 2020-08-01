package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.ChainedRequestHandler;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.RequestHandlerChain;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A routing engine return 404 upon the urls which is sure to be "NotFound" (by prefix matching)
 *
 * For example, you have an interceptor handler "/*" and handler "/user/:id", then url "/unknown" is sure to be 404.
 */
@Singleton
public class Http404RequestHandler extends AbstractRequestHandler implements ChainedRequestHandler {
    private final List<String> handlerPathPrefixes;
    private final DefaultRoutingManager routingManager;

    @Inject
    public Http404RequestHandler(Vertx vertx, Injector injector, RoutingManager routingManager, ConfigProvider configProvider) {
        super(vertx, injector, configProvider);
        this.routingManager = (DefaultRoutingManager) routingManager;
        this.handlerPathPrefixes = Stream.of(this.routingManager.getRoutingPrefixes(RoutingType.HANDLER),
                this.routingManager.getRoutingPrefixes(RoutingType.SOCK_JS),
                this.routingManager.getRoutingPrefixes(RoutingType.SOCK_JS_BRIDGE)
        ).flatMap(List::stream).collect(Collectors.toList());
    }


    @Override
    public void handle(HttpServerRequest request, RequestHandlerChain handlerChain) {
        if (handlerPathPrefixes.stream().noneMatch(prefix -> request.path().startsWith(prefix))) {
            request.response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
            request.response().putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN);
            request.response().end("NOT FOUND");
        } else {
            handlerChain.handleNext(request);
        }
    }
}
