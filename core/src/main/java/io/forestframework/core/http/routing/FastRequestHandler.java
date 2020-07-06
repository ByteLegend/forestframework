package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import io.forestframework.core.config.Config;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * A "fast" routing engine implementation which only handles "fast" routing. A routing is fast when:
 *
 * <ol>
 *     <li>1. No regex/path parameters. Path matching is slow.</li>
 *     <li>2. No {@link RoutingContext in handler parameters}. {@link RoutingContext} is heavy, calling next() is slow.</li>
 *     <li>3. No prefix-matching interceptors. The prefix scanning is performed at startup phase.</li>
 * </ol>
 *
 * Fast routing is matched by hashtable lookup and invoked directly, without {@link io.vertx.ext.web.Router}/{@link RoutingContext} creation.
 */
@Singleton
public class FastRequestHandler extends AbstractRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FastRequestHandler.class);
    private final DefaultRoutings routings;
    private final Map<String, Routing> pathToFastRoutingMap;

    @Inject
    public FastRequestHandler(Injector injector,
                              Vertx vertx,
                              Routings routings,
                              @Config("forest.environment") String environment) {
        super(vertx, injector, routings, environment);
        this.routings = (DefaultRoutings) routings;
        this.pathToFastRoutingMap = createPathToFastRoutingMap();
    }

    @Override
    public void handle(HttpServerRequest request) {
        Routing routing = pathToFastRoutingMap.get(request.path());

        if (routing == null) {
            next(request);
        } else {
            doHandle(routing, request);
        }
    }

    public void doHandle(Routing routing, HttpServerRequest request) {
        RoutingContext context = new FastRoutingContext(request);
        Object[] arguments = resolveArguments(routing, context);
        CompletableFuture<Object> returnValueFuture = invokeHandler(routing, arguments);
        returnValueFuture
                .thenAccept(returnValue ->
                        processResult(context, routing, returnValue).thenAccept(ret -> {
                            if (!request.response().ended()) {
                                request.response().end();
                            }
                        }).exceptionally((Throwable failure) -> {
                            onHandlerFailure(context, failure);
                            return null;
                        }))
                .exceptionally((Throwable failure) -> {
                    onHandlerFailure(context, failure);
                    return null;
                });
    }

    private Map<String, Routing> createPathToFastRoutingMap() {
        return routings.getRouting(RoutingType.HANDLER)
                .stream()
                .filter(this::isFastRouting)
                .collect(toMap(Routing::getPath, r -> r, (x, y) -> x));
    }

    private boolean isFastRouting(Routing routing) {
        return routing.getRegexPath().isEmpty() && noPrefixMatchingInterceptors(routing)  && noSlowParamResolverOrResultProcessor(routing);
    }

    private boolean noSlowParamResolverOrResultProcessor(Routing routing) {
        return false;
    }

    private boolean noPrefixMatchingInterceptors(Routing routing) {
        return Stream.of(RoutingType.values())
                .filter(routingType -> routingType != RoutingType.HANDLER)
                .map(routings::getRoutingPrefixes)
                .flatMap(List::stream)
                .noneMatch(prefix -> routing.getPath().startsWith(prefix));
    }

    void onHandlerFailure(RoutingContext context, Throwable failure) {
        LOGGER.error("", failure);
        context.response().setStatusCode(HttpStatusCode.SERVER_ERROR.getCode());
        context.response().putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN);
        context.response().write(ExceptionUtils.getStackTrace(failure));
        if (!context.response().ended()) {
            context.response().end();
        }
    }
}
