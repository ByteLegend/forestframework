package io.forestframework.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.forestframework.annotation.RoutingType;
import io.forestframework.config.Config;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A "fast" routing engine implementation. Fast means there's no interceptors,
 * no regex/path parameters, so the routing matching can be really fast:
 * the handler lookup can be performed via hashtable, and no need to
 * invoke {@link RoutingContext.next()}.
 */
public class FastRoutingEngine extends DefaultRoutingEngine {
    Handler<HttpServerRequest> router;

    @Inject
    public FastRoutingEngine(Injector injector,
                             Vertx vertx,
                             @Routings Map<RoutingType, List<Routing>> routings,
                             @Config("forest.environment") String environment) {
        super(injector, vertx, routings, environment);
        Map<String, Routing> pathToRouting = routings.get(RoutingType.HANDLER).stream()
                .collect(Collectors.toMap(Routing::getPath, x -> x));
        router = new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                Routing routing = pathToRouting.get(request.path());
                if (routing == null) {
                    request.response().setStatusCode(404).end();
                }


                RoutingContext context = new AbstractRoutingContextDecorator(null) {
                    @Override
                    public HttpServerRequest request() {
                        return request;
                    }

                    @Override
                    public HttpServerResponse response() {
                        return request.response();
                    }
                };

                Object[] arguments = resolveArguments(routing, context);
                CompletableFuture<Object> returnValueFuture = invokeHandler(routing, arguments);

                returnValueFuture.thenAccept(returnValue -> {
                    processResult(context, routing, returnValue).thenAccept(ret -> {
                        if (!request.response().ended()) {
                            request.response().end();
                        }
                    }).exceptionally((Throwable failure) -> {
                        onHandlerFailure(context, failure);
                        return null;
                    });

                }).exceptionally((Throwable failure) -> {
                    onHandlerFailure(context, failure);
                    return null;
                });


            }
        };
    }

    void onHandlerFailure(RoutingContext context, Throwable failure) {
        LOGGER.error("", failure);
        context.response().setStatusCode(500);
        context.response().putHeader("Context-Type", "text/plain");
        context.response().write(ExceptionUtils.getStackTrace(failure));
        if (!context.response().ended()) {
            context.response().end();
        }
    }

    @Override
    public Handler<HttpServerRequest> createRouter() {
        return router;
    }
}
