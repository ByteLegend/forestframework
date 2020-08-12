package io.forestframework.core.http.websocket;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.forestframework.core.http.AbstractWebRequestHandler;
import io.forestframework.core.http.DefaultWebSocketContext;
import io.forestframework.core.http.RoutingMatchResults;
import io.forestframework.core.http.RoutingMatcher;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingType;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

//import com.google.inject.Injector;
//import io.forestframework.core.config.ConfigProvider;
//import io.forestframework.core.http.ArgumentInjector;
//import io.forestframework.core.http.routing.AbstractRequestHandler;
//import io.forestframework.core.http.routing.PathMatcher;
//import io.forestframework.core.http.routing.Routing;
//import io.forestframework.core.http.routing.RoutingManager;
//import io.forestframework.core.http.routing.RoutingType;
//import io.vertx.core.Promise;
//import io.vertx.core.Vertx;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.ServerWebSocket;
//import org.apiguardian.api.API;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
///**
// * A default {@link WebSocketRequestHandler} implementation which supports:
// *
// * <ol>
// *     <li>Plain WebSocket handling</li>
// * </ol>
// */
@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultWebSocketRequestHandler extends AbstractWebRequestHandler implements WebSocketRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSocketRequestHandler.class);
    private final RoutingMatcher routingMatcher;

    @Inject
    public DefaultWebSocketRequestHandler(Vertx vertx,
                                          Injector injector,
                                          RoutingMatcher routingMatcher) {
        super(vertx, injector);
        this.routingMatcher = routingMatcher;
    }

    @Override
    public void handle(ServerWebSocket webSocket) {
        RoutingMatchResults results = routingMatcher.match(webSocket);

        Routing onOpen = assertOneOrNull(webSocket, results.getMatchedRoutings(RoutingType.ON_WEB_SOCKET_OPEN));
        Routing onMessage = assertOneOrNull(webSocket, results.getMatchedRoutings(RoutingType.ON_WEB_SOCKET_MESSAGE));
        Routing onError = assertOneOrNull(webSocket, results.getMatchedRoutings(RoutingType.ON_WEB_SOCKET_ERROR));
        Routing onClose = assertOneOrNull(webSocket, results.getMatchedRoutings(RoutingType.ON_WEB_SOCKET_CLOSE));

        if (onOpen == null && onMessage == null && onError == null && onClose == null) {
            Promise<Integer> promise = Promise.promise();
            webSocket.setHandshake(promise.future());
            promise.complete(404);
        } else {
            DefaultWebSocketContext context = new DefaultWebSocketContext(injector, webSocket, results);

            // OnWSOpen
            invokeWebSocketHandler(onOpen, context, null, null);

            webSocket.handler(buffer -> {
                // OnWSMessage
                invokeWebSocketHandler(onMessage, context, buffer, null);
            }).exceptionHandler(throwable -> {
                // OnWSError
                invokeWebSocketHandler(onError, context, null, throwable);
            }).endHandler(v -> {
                // OnWSClose
                invokeWebSocketHandler(onClose, context, null, null);
            });
        }
    }

    private Routing assertOneOrNull(ServerWebSocket socket, List<Routing> matchedRoutings) {
        if (matchedRoutings.size() > 1) {
            throw new IllegalArgumentException("Found more than 1 handler when processing " + socket + socket.path() + ": " +
                    matchedRoutings.stream().map(Routing::getHandlerMethod).map(Object::toString).collect(Collectors.joining("\n")));
        }
        return matchedRoutings.isEmpty() ? null : matchedRoutings.get(0);
    }
    // extends AbstractRequestHandler implements WebSocketRequestHandler {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSocketRequestHandler.class);
//    private final List<WebSocketRoutingGroup> websocketRoutings;
//
//    @Inject
//    public DefaultWebSocketRequestHandler(Vertx vertx, Injector injector, ConfigProvider configProvider, RoutingManager routingManager) {
//        super(vertx, injector, configProvider);
//        this.websocketRoutings = createRoutings(routingManager.getRouting(RoutingType.WEB_SOCKET));
//    }
//
//    private List<WebSocketRoutingGroup> createRoutings(List<Routing> routings) {
//        Map<String, List<WebSocketRouting>> pathToHandlers = routings.stream()
//                .map(r -> (WebSocketRouting) r)
//                .collect(Collectors.groupingBy(WebSocketRouting::getPath));
//
//        List<WebSocketRoutingGroup> result = new ArrayList<>();
//
//        for (Map.Entry<String, List<WebSocketRouting>> entry : pathToHandlers.entrySet()) {
//            String path = entry.getKey();
//            Map<WebSocketEventType, WebSocketRouting> eventTypeToRouting = validateAndRemapWebSocketRoutings(path, entry.getValue());
//            result.add(new WebSocketRoutingGroup(path, eventTypeToRouting));
//        }
//        return Collections.unmodifiableList(result);
//    }
//
//    private Map<WebSocketEventType, WebSocketRouting> validateAndRemapWebSocketRoutings(String path, List<WebSocketRouting> routings) {
//        Map<WebSocketEventType, WebSocketRouting> ret = new HashMap<>();
//        for (WebSocketRouting routing : validateWebSocketRoutingsFor(path, routings)) {
//            for (WebSocketEventType type : routing.getWebSocketEventTypes()) {
//                ret.put(type, routing);
//            }
//        }
//        return Collections.unmodifiableMap(ret);
//    }
//
//    private List<WebSocketRouting> validateWebSocketRoutingsFor(String path, List<WebSocketRouting> routings) {
//        Set<WebSocketEventType> eventTypes = new HashSet<>();
//        for (WebSocketRouting routing : routings) {
//            for (WebSocketEventType eventType : routing.getWebSocketEventTypes()) {
//                if (!eventTypes.add(eventType)) {
//                    List<Method> conflictHandlers = routings.stream()
//                            .filter(it -> it.getWebSocketEventTypes().contains(eventType))
//                            .map(WebSocketRouting::getHandlerMethod)
//                            .collect(Collectors.toList());
//                    throw new IllegalArgumentException("Found more than one SockJS handler mapped to " + path + ": " + conflictHandlers);
//                }
//            }
//        }
//
//        return routings;
//    }
//
//    /**
//     * A group of {@link WebSocketRouting}s with same path, but different {@link WebSocketEventType}s.
//     */
//    private static class WebSocketRoutingGroup {
//        private final PathMatcher pathMatcher;
//        private final Map<WebSocketEventType, WebSocketRouting> routings;
//
//        private WebSocketRoutingGroup(String path, Map<WebSocketEventType, WebSocketRouting> routings) {
//            this.pathMatcher = PathMatcher.fromPattern(path);
//            this.routings = routings;
//        }
//
//        private boolean matches(String path) {
//            return pathMatcher.matches(path);
//        }
//
//        private Map<WebSocketEventType, WebSocketRouting> getRoutings() {
//            return routings;
//        }
//    }
//
//


    private void invokeWebSocketHandler(Routing routing, DefaultWebSocketContext context, Buffer buffer, Throwable throwable) {
        if (routing != null) {
            context.getArgumentInjector()
                    .withParameter(Buffer.class, buffer)
                    .withParameterSupplier(String.class, buffer == null ? null : buffer::toString)
                    .with(throwable);

            CompletableFuture<Object> returnValueFuture = invokeRouting(routing, context);
            returnValueFuture.whenComplete((returnValue, failure) -> {
                if (failure != null) {
                    LOGGER.error("", failure);
                }
            });
        }
    }

//    private Map<WebSocketEventType, WebSocketRouting> findMatchingPath(String path) {
//        return websocketRoutings.stream()
//                .filter(it -> it.matches(path))
//                .findFirst()
//                .map(WebSocketRoutingGroup::getRoutings)
//                .orElse(null);
//    }
//}

}