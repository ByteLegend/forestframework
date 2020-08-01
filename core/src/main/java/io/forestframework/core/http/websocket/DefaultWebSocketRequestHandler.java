package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.InjectableParameters;
import io.forestframework.core.http.routing.AbstractRequestHandler;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.http.routing.RoutingType;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A default {@link WebSocketRequestHandler} implementation which supports:
 *
 * <ol>
 *     <li>Plain WebSocket handling</li>
 * </ol>
 */
@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultWebSocketRequestHandler extends AbstractRequestHandler implements WebSocketRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSocketRequestHandler.class);
    private final Map<String, Map<WebSocketEventType, WebSocketRouting>> websocketRoutings;

    @Inject
    public DefaultWebSocketRequestHandler(Vertx vertx, Injector injector, ConfigProvider configProvider, RoutingManager routingManager) {
        super(vertx, injector, configProvider);
        this.websocketRoutings = createRoutings(routingManager.getRouting(RoutingType.WEB_SOCKET));
    }

    private Map<String, Map<WebSocketEventType, WebSocketRouting>> createRoutings(List<Routing> routings) {
        Map<String, List<WebSocketRouting>> pathToHandlers = routings.stream()
                .map(r -> (WebSocketRouting) r)
                .collect(Collectors.groupingBy(WebSocketRouting::getPath));

        Map<String, Map<WebSocketEventType, WebSocketRouting>> result = new HashMap<>();

        for (Map.Entry<String, List<WebSocketRouting>> entry : pathToHandlers.entrySet()) {
            String path = entry.getKey();
            Map<WebSocketEventType, WebSocketRouting> eventTypeToRouting = validateAndRemapWebSocketRoutings(path, entry.getValue());
            result.put(path, eventTypeToRouting);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<WebSocketEventType, WebSocketRouting> validateAndRemapWebSocketRoutings(String path, List<WebSocketRouting> routings) {
        Map<WebSocketEventType, WebSocketRouting> ret = new HashMap<>();
        for (WebSocketRouting routing : validateWebSocketRoutingsFor(path, routings)) {
            for (WebSocketEventType type : routing.getWebSocketEventTypes()) {
                ret.put(type, routing);
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    private List<WebSocketRouting> validateWebSocketRoutingsFor(String path, List<WebSocketRouting> routings) {
        Set<WebSocketEventType> eventTypes = new HashSet<>();
        for (WebSocketRouting routing : routings) {
            for (WebSocketEventType eventType : routing.getWebSocketEventTypes()) {
                if (!eventTypes.add(eventType)) {
                    List<Method> conflictHandlers = routings.stream()
                            .filter(it -> it.getWebSocketEventTypes().contains(eventType))
                            .map(WebSocketRouting::getHandlerMethod)
                            .collect(Collectors.toList());
                    throw new IllegalArgumentException("Found more than one SockJS handler mapped to " + path + ": " + conflictHandlers);
                }
            }
        }

        return routings;
    }


    @Override
    public void handle(ServerWebSocket socket) {
        Map<WebSocketEventType, WebSocketRouting> matchingPath = findMatchingPath(socket.path());
        if (matchingPath == null) {
            socket.reject();
        } else {
            WebSocketRoutingContext routingContext = new WebSocketRoutingContext(socket);
            // OnWSOpen
            invokeOnOpen(matchingPath, routingContext, socket);

            socket.handler(buffer -> {
                // OnWSMessage
                invokeOnMessage(matchingPath, routingContext, socket, buffer);
            }).exceptionHandler(throwable -> {
                // OnWSErrro
                invokeOnError(matchingPath, routingContext, socket, throwable);
            }).endHandler(v -> {
                invokeOnClose(matchingPath, routingContext, socket);
            });
        }
    }

    private void invokeWebSocketHandler(Map<WebSocketEventType, WebSocketRouting> matchingPath, WebSocketEventType eventType, InjectableParameters injectableParameters) {
        WebSocketRouting handler = matchingPath.get(eventType);
        if (handler != null) {
            CompletableFuture<Object> returnValueFuture = resolveArgumentsAndInvokeHandler(handler, injectableParameters);
            returnValueFuture.whenComplete((returnValue, failure) -> {
                if (failure != null) {
                    LOGGER.error("", failure);
                }
            });
        }
    }

    private InjectableParameters createInjectableParameters(WebSocketRoutingContext routingContext, WebSocketEventType eventType, ServerWebSocket webSocket) {
        return new InjectableParameters(injector)
                .with(routingContext)
                .withParameter(ServerWebSocket.class, webSocket)
                .withParameter(WebSocketEventType.class, eventType)
                .withParameter(Buffer.class, null)
                .withParameter(Throwable.class, null)
                .withParameter(Exception.class, null);
    }

    private void invokeOnClose(Map<WebSocketEventType, WebSocketRouting> matchingPath, WebSocketRoutingContext routingContext, ServerWebSocket webSocket) {
        invokeWebSocketHandler(matchingPath, WebSocketEventType.CLOSE,
                createInjectableParameters(routingContext, WebSocketEventType.CLOSE, webSocket));
    }

    private void invokeOnError(Map<WebSocketEventType, WebSocketRouting> matchingPath, WebSocketRoutingContext routingContext, ServerWebSocket webSocket, Throwable throwable) {
        invokeWebSocketHandler(matchingPath, WebSocketEventType.ERROR,
                createInjectableParameters(routingContext, WebSocketEventType.ERROR, webSocket).withParameter(Throwable.class, throwable));
    }

    private void invokeOnMessage(Map<WebSocketEventType, WebSocketRouting> matchingPath, WebSocketRoutingContext routingContext, ServerWebSocket webSocket, Buffer buffer) {
        invokeWebSocketHandler(matchingPath, WebSocketEventType.MESSAGE,
                createInjectableParameters(routingContext, WebSocketEventType.MESSAGE, webSocket).withParameter(Buffer.class, buffer));
    }

    private void invokeOnOpen(Map<WebSocketEventType, WebSocketRouting> matchingPath, WebSocketRoutingContext routingContext, ServerWebSocket webSocket) {
        invokeWebSocketHandler(matchingPath, WebSocketEventType.OPEN,
                createInjectableParameters(routingContext, WebSocketEventType.OPEN, webSocket));
    }

    private Map<WebSocketEventType, WebSocketRouting> findMatchingPath(String path) {
        return websocketRoutings.get(path);
    }
}
