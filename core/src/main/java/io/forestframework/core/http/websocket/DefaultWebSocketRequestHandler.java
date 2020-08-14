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

}