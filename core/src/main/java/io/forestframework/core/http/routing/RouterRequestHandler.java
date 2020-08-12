package io.forestframework.core.http.routing;

//import com.google.inject.Injector;
//import io.forestframework.core.config.ConfigProvider;
//import io.forestframework.core.http.ChainedRequestHandler;
//import io.forestframework.core.http.HttpMethod;
//import io.forestframework.core.http.HttpStatusCode;
//import io.forestframework.core.http.RequestHandlerChain;
//import io.forestframework.core.http.RerouteNextAwareRoutingContextDecorator;
//import io.forestframework.core.http.sockjs.SockJSEventType;
//import io.vertx.core.Handler;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpServerRequest;
//import io.vertx.ext.web.Router;
//import io.vertx.ext.web.RoutingContext;
//import io.vertx.ext.web.handler.BodyHandler;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.EnumSet;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
//import static io.forestframework.core.http.routing.RoutingPath.RoutingPathNode;
//import static io.forestframework.core.http.routing.RoutingType.HANDLER;
//import static io.forestframework.core.http.routing.RoutingType.POST_HANDLER;
//import static io.forestframework.core.http.routing.RoutingType.PRE_HANDLER;

/**
 * A request handler that leverages Vert.x {@link Router}.
 *
 * @see <a href="https://vertx.io/docs/vertx-web/java/">Vert.x Web Documentation</a>
 */
//@Singleton
public class RouterRequestHandler {
//        extends AbstractRequestHandler implements ChainedRequestHandler {
//    private static final EnumSet<HttpMethod> METHODS_WITHOUT_BODY = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS);
//    private static final String ROUTING_PATH_KEY = "FOREST_ROUTING_PATH";
//    static final Logger LOGGER = LoggerFactory.getLogger(RouterRequestHandler.class);
//    private final Handler<HttpServerRequest> router;
//    private final ConfigProvider configProvider;
//
//    @Inject
//    public RouterRequestHandler(Vertx vertx, Injector injector, RoutingManager routingManager, ConfigProvider configProvider) {
//        super(vertx, injector, configProvider);
//        this.configProvider = injector.getInstance(ConfigProvider.class);
//        this.router = createRouter(routingManager);
//    }
//
//    @Override
//    public void handle(HttpServerRequest request, RequestHandlerChain handlerChain) {
//        router.handle(request);
//    }
//
//    public Handler<HttpServerRequest> createRouter(RoutingManager routingManager) {
//        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
//        router.errorHandler(500, context -> LOGGER.error("", context.failure()));
//
//        configurePrePostHandlerRoutes(router, routingManager.getRouting(PRE_HANDLER));
//        configureHandlerRoutes(router, routingManager.getRouting(HANDLER));
//        configurePrePostHandlerRoutes(router, routingManager.getRouting(POST_HANDLER));
////        configureFinalizingRoute(router);
//
////        configureSockJSRoutes(router, routings.getRouting(SOCK_JS));
//        return router;
//    }
//
////    private void configureSockJSRoutes(Router router, List<Routing> routings) {
////        SockJSHandlerOptions options = configProvider.getInstance("forest.sockjs", SockJSHandlerOptions.class);
////
////        // A single path must has only one handler per event type
////        Map<String, List<SockJSRouting>> pathToHandlers = routings.stream()
////                .map(r -> (SockJSRouting) r)
////                .collect(Collectors.groupingBy(SockJSRouting::getPath));
////
////        for (Map.Entry<String, List<SockJSRouting>> entry : pathToHandlers.entrySet()) {
////            String path = entry.getKey();
////            Map<SockJSEventType, SockJSRouting> eventTypeToRouting = validateAndRemapSockJsRoutings(path, entry.getValue());
////            SockJSHandler handler = SockJSHandler.create(vertx, options);
////
////            router.route(path)
////                    .handler(new SockJSHandlerDecorator(handler, eventTypeToRouting))
////                    .failureHandler(e -> LOGGER.error("Error when handling " + path, e));
////        }
////    }
//
//    private Map<SockJSEventType, SockJSRouting> validateAndRemapSockJsRoutings(String path, List<SockJSRouting> routings) {
//        Map<SockJSEventType, SockJSRouting> ret = new HashMap<>();
//        for (SockJSRouting routing : validateSockJsRoutingsFor(path, routings)) {
//            for (SockJSEventType type : routing.getEventTypes()) {
//                ret.put(type, routing);
//            }
//        }
//        return ret;
//    }
//
//
////    private class SockJSHandlerDecorator implements Handler<RoutingContext> {
////        private final SockJSHandler sockJSHandler;
////        private final Map<SockJSEventType, SockJSRouting> eventTypeToRouting;
////
////        public SockJSHandlerDecorator(SockJSHandler sockJSHandler, Map<SockJSEventType, SockJSRouting> eventTypeToRouting) {
////            this.sockJSHandler = sockJSHandler;
////            this.eventTypeToRouting = eventTypeToRouting;
////        }
////
////        @Override
////        public void handle(RoutingContext routingContext) {
////            sockJSHandler.socketHandler(sockJSSocket -> {
////                // OnOpen
////                invokeOnOpen(routingContext, sockJSSocket);
////
////                sockJSSocket.handler(buffer -> {
////                    // onMessage
////                    invokeOnMessage(routingContext, sockJSSocket, buffer);
////                }).exceptionHandler(throwable -> {
////                    // onError
////                    invokeOnError(routingContext, sockJSSocket, throwable);
////                }).endHandler(v -> {
////                    // onClose
////                    invokeOnClose(routingContext, sockJSSocket);
////                });
////            });
////            sockJSHandler.handle(routingContext);
////        }
////
////        private void invokeSockJSHandler(RoutingContext routingContext, SockJSEventType eventType, PredefinedArguments predefinedArguments) {
////            SockJSRouting onOpenHandler = eventTypeToRouting.get(eventType);
////            if (onOpenHandler != null) {
////                Object[] arguments = resolveArguments(onOpenHandler, routingContext, predefinedArguments);
////                CompletableFuture<Object> returnValueFuture = invokeHandler(onOpenHandler, arguments);
////                returnValueFuture.whenComplete((returnValue, failure) -> {
////                    if (failure != null) {
////                        LOGGER.error("", failure);
////                    }
////                });
////            }
////        }
////
////        private void invokeOnClose(RoutingContext routingContext, SockJSSocket sockJSSocket) {
////            invokeSockJSHandler(routingContext, SockJSEventType.CLOSE,
////                    new PredefinedArguments(routingContext).withArgument(SockJSSocket.class, sockJSSocket));
////        }
////
////        private void invokeOnError(RoutingContext routingContext, SockJSSocket sockJSSocket, Throwable throwable) {
////            invokeSockJSHandler(routingContext, SockJSEventType.ERROR,
////                    new PredefinedArguments(routingContext)
////                            .withArgument(SockJSSocket.class, sockJSSocket)
////                            .withArgument(Throwable.class, throwable));
////        }
////
////        private void invokeOnMessage(RoutingContext routingContext, SockJSSocket sockJSSocket, Buffer buffer) {
////            invokeSockJSHandler(routingContext, SockJSEventType.MESSAGE,
////                    new PredefinedArguments(routingContext)
////                            .withArgument(SockJSSocket.class, sockJSSocket)
////                            .withArgument(Buffer.class, buffer));
////        }
////
////        private void invokeOnOpen(RoutingContext routingContext, SockJSSocket sockJSSocket) {
////            invokeSockJSHandler(routingContext, SockJSEventType.OPEN,
////                    new PredefinedArguments(routingContext).withArgument(SockJSSocket.class, sockJSSocket));
////        }
////    }
//
//    private List<SockJSRouting> validateSockJsRoutingsFor(String path, List<SockJSRouting> routings) {
//        Set<SockJSEventType> eventTypes = new HashSet<>();
//        for (SockJSRouting routing : routings) {
//            for (SockJSEventType eventType : routing.getEventTypes()) {
//                if (!eventTypes.add(eventType)) {
//                    List<Method> conflictHandlers = routings.stream()
//                            .filter(it -> it.getEventTypes().contains(eventType))
//                            .map(SockJSRouting::getHandlerMethod)
//                            .collect(Collectors.toList());
//                    throw new IllegalArgumentException("Found more than one SockJS handler mapped to " + path + ": " + conflictHandlers);
//                }
//            }
//        }
//
//        return routings;
//    }
//
//    private RoutingPath getOrCreatePath(RerouteNextAwareRoutingContextDecorator context) {
//        RoutingPath ret = context.get(ROUTING_PATH_KEY);
//        if (ret == null) {
//            ret = new RoutingPath(context);
//            context.put(ROUTING_PATH_KEY, ret);
//        }
//        return ret;
//    }
//
//    private void configureFinalizingRoute(io.vertx.ext.web.Router router) {
//        io.vertx.ext.web.Route route = router.route("/*");
//        for (HttpMethod method : HttpMethod.values()) {
//            route = route.method(method.toVertxHttpMethod());
//        }
//        route.handler(context -> {
//            RoutingPath path = context.get(ROUTING_PATH_KEY);
//            if (path == null || path.noHandlerInvoked()) {
//                respond404(context);
//            } else if (path.hasFailures()) {
//                path.respond500(devMode);
//            } else if (!context.response().ended()) {
//                context.response().end();
//            }
//        });
//    }
//
//    private void respond404(RoutingContext context) {
//        context.response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
//        context.response().end("NOT FOUND");
//    }
//
//    private io.vertx.ext.web.Route configureRouter(Router router, Routing routing) {
//        io.vertx.ext.web.Route route;
//        String path = routing.getPath();
//        String regexPath = routing.getRegexPath();
//        if (StringUtils.isBlank(path) && StringUtils.isBlank(regexPath)) {
//            throw new IllegalArgumentException("Can't configure router: path and regexPath are both empty");
//        } else if (StringUtils.isNoneBlank(path) && StringUtils.isNoneBlank(regexPath)) {
//            throw new IllegalArgumentException("Can't configure router: path and regexPath are both non-empty");
//        } else if (StringUtils.isBlank(path)) {
//            route = router.routeWithRegex(routing.getRegexPath());
//        } else {
//            route = router.route(routing.getPath());
//        }
//
//        for (HttpMethod method : routing.getMethods()) {
//            route = route.method(method.toVertxHttpMethod());
//        }
//        if (hasBody(routing.getMethods())) {
//            route.handler(BodyHandler.create());
//        }
//        return route;
//    }
//
//    private boolean hasBody(List<HttpMethod> methods) {
//        List<HttpMethod> copy = new ArrayList<>(methods);
//        copy.removeAll(METHODS_WITHOUT_BODY);
//        return !copy.isEmpty();
//    }
//
//    private void configureHandlerRoutes(io.vertx.ext.web.Router router, List<Routing> routings) {
//        for (Routing routing : routings) {
//            configureRouter(router, routing)
//                    .handler(ctx -> {
//                        RerouteNextAwareRoutingContextDecorator context = new RerouteNextAwareRoutingContextDecorator(ctx);
//                        RoutingPath path = getOrCreatePath(context);
//                        RoutingPathNode pathNode = path.addNode(routing);
//                        if (pathNode.isSkipped()) {
//                            path.next();
//                        } else {
//                            CompletableFuture<Object> returnValueFuture = resolveArgumentsAndInvokeHandler(routing, context);
//                            returnValueFuture.thenAccept((Object returnValue) -> {
//                                pathNode.setResult(returnValue);
//                                if (!context.isRerouteInvoked()) {
//                                    invokeResultProcessors(context, routing, returnValue)
//                                            .thenAccept(ret -> path.next())
//                                            .exceptionally((Throwable failure) -> {
//                                                LOGGER.error("", failure);
//                                                pathNode.setFailure(failure);
//                                                path.next();
//                                                return null;
//                                            });
//                                }
//                            }).exceptionally(failure -> {
//                                LOGGER.error("", failure);
//                                pathNode.setResult(failure);
//                                path.next();
//                                return null;
//                            });
//                        }
//                    });
//        }
//    }
//
//    private void configurePrePostHandlerRoutes(Router router, List<Routing> routings) {
//        for (Routing routing : routings) {
//            configureRouter(router, routing)
//                    .handler(ctx -> {
//                        RerouteNextAwareRoutingContextDecorator context = new RerouteNextAwareRoutingContextDecorator(ctx);
//                        RoutingPath path = getOrCreatePath(context);
//                        RoutingPathNode pathNode = path.addNode(routing);
//                        if (pathNode.isSkipped()) {
//                            path.next();
//                        } else {
//                            CompletableFuture<Object> returnValueFuture = resolveArgumentsAndInvokeHandler(routing, context);
//                            returnValueFuture.thenAccept((Object returnValue) -> {
//                                pathNode.setResult(returnValue);
//                                path.next();
//                            }).exceptionally(failure -> {
//                                LOGGER.error("", failure);
//                                pathNode.setResult(failure);
//                                path.next();
//                                return null;
//                            });
//                        }
//                    });
//        }
//    }
//
//    // A valid pre-handler must return void/boolean/Boolean/Future<Boolean>
//    private void validPreHandler(Routing routing) {
//        // TODO
//    }
}
