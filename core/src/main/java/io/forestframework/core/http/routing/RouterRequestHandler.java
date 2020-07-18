package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.RerouteNextAwareRoutingContextDecorator;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.forestframework.core.http.routing.RoutingPath.RoutingPathNode;
import static io.forestframework.core.http.routing.RoutingType.HANDLER;
import static io.forestframework.core.http.routing.RoutingType.POST_HANDLER;
import static io.forestframework.core.http.routing.RoutingType.PRE_HANDLER;

/**
 * A request handler that leverages Vert.x {@link Router}.
 *
 * @see <a href="https://vertx.io/docs/vertx-web/java/">Vert.x Web Documentation</a>
 */
@Singleton
public class RouterRequestHandler extends AbstractRequestHandler {
    private static final EnumSet<HttpMethod> METHODS_WITHOUT_BODY = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS);
    private static final String ROUTING_PATH_KEY = "FOREST_ROUTING_PATH";
    static final Logger LOGGER = LoggerFactory.getLogger(RouterRequestHandler.class);
    private static final String ENABLED_STATES_KEY = "FOREST_ROUTING_ENGINE_ENABLED_STATES";
    private final Handler<HttpServerRequest> router;

    @Inject
    public RouterRequestHandler(Vertx vertx, Injector injector, Routings routings, String environment) {
        super(vertx, injector, routings, environment);
        this.router = createRouter();
    }

    @Override
    public void handle(HttpServerRequest request) {
        router.handle(request);
    }

    public Handler<HttpServerRequest> createRouter() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
        router.errorHandler(500, context -> LOGGER.error("", context.failure()));

        configurePrePostHandlerRoutes(router, routings.getRouting(PRE_HANDLER));
        configureHandlerRoutes(router, routings.getRouting(HANDLER));
        configurePrePostHandlerRoutes(router, routings.getRouting(POST_HANDLER));
        configureFinalizingRoute(router);
        return router;
    }

    private RoutingPath getOrCreatePath(RerouteNextAwareRoutingContextDecorator context) {
        RoutingPath ret = context.get(ROUTING_PATH_KEY);
        if (ret == null) {
            ret = new RoutingPath(context);
            context.put(ROUTING_PATH_KEY, ret);
        }
        return ret;
    }

    private void configureFinalizingRoute(io.vertx.ext.web.Router router) {
        io.vertx.ext.web.Route route = router.route("/*");
        for (HttpMethod method : HttpMethod.values()) {
            route = route.method(method.toVertxHttpMethod());
        }
        route.handler(context -> {
            RoutingPath path = context.get(ROUTING_PATH_KEY);
            if (path == null || path.noHandlerInvoked()) {
                respond404(context);
            } else if (path.hasFailures()) {
                path.respond500(devMode);
            } else if (!context.response().ended()) {
                context.response().end();
            }
        });
    }

    private void respond404(RoutingContext context) {
        context.response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
        context.response().end("NOT FOUND");
    }

    private io.vertx.ext.web.Route configureRouter(Router router, Routing routing) {
        io.vertx.ext.web.Route route;
        String path = routing.getPath();
        String regexPath = routing.getRegexPath();
        if (StringUtils.isBlank(path) && StringUtils.isBlank(regexPath)) {
            throw new IllegalArgumentException("Can't configure router: path and regexPath are both empty");
        } else if (StringUtils.isNoneBlank(path) && StringUtils.isNoneBlank(regexPath)) {
            throw new IllegalArgumentException("Can't configure router: path and regexPath are both non-empty");
        } else if (StringUtils.isBlank(path)) {
            route = router.routeWithRegex(routing.getRegexPath());
        } else {
            route = router.route(routing.getPath());
        }

        for (HttpMethod method : routing.getMethods()) {
            route = route.method(method.toVertxHttpMethod());
        }
        if (hasBody(routing.getMethods())) {
            route.handler(BodyHandler.create());
        }
        return route;

    }

    private boolean hasBody(List<HttpMethod> methods) {
        List<HttpMethod> copy = new ArrayList<>(methods);
        copy.removeAll(METHODS_WITHOUT_BODY);
        return !copy.isEmpty();
    }

    private void configureHandlerRoutes(io.vertx.ext.web.Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            configureRouter(router, routing)
                    .handler(ctx -> {
                        RerouteNextAwareRoutingContextDecorator context = new RerouteNextAwareRoutingContextDecorator(ctx);
                        RoutingPath path = getOrCreatePath(context);
                        RoutingPathNode pathNode = path.addNode(routing);
                        if (pathNode.isSkipped()) {
                            path.next();
                        } else {
                            CompletableFuture<Object> returnValueFuture = resolveArgumentsAndInvokeHandler(routing, context);
                            returnValueFuture.thenAccept((Object returnValue) -> {
                                pathNode.setResult(returnValue);
                                if (!context.isRerouteInvoked()) {
                                    invokeResultProcessors(context, routing, returnValue)
                                            .thenAccept(ret -> path.next())
                                            .exceptionally((Throwable failure) -> {
                                                pathNode.setFailure(failure);
                                                path.next();
                                                return null;
                                            });
                                }
                            }).exceptionally(failure -> {
                                LOGGER.error("", failure);
                                pathNode.setResult(failure);
                                path.next();
                                return null;
                            });
                        }
                    });
        }
    }

    private void configurePrePostHandlerRoutes(Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            configureRouter(router, routing)
                    .handler(ctx -> {
                        RerouteNextAwareRoutingContextDecorator context = new RerouteNextAwareRoutingContextDecorator(ctx);
                        RoutingPath path = getOrCreatePath(context);
                        RoutingPathNode pathNode = path.addNode(routing);
                        if (pathNode.isSkipped()) {
                            path.next();
                        } else {
                            CompletableFuture<Object> returnValueFuture = resolveArgumentsAndInvokeHandler(routing, context);
                            returnValueFuture.thenAccept((Object returnValue) -> {
                                pathNode.setResult(returnValue);
                                path.next();
                            }).exceptionally(failure -> {
                                pathNode.setResult(failure);
                                path.next();
                                return null;
                            });
                        }
                    });
        }
    }

    // A valid pre-handler must return void/boolean/Boolean/Future<Boolean>
    private void validPreHandler(Routing routing) {
        // TODO
    }
}
