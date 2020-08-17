package io.forestframework.core.http;

import io.forestframework.core.http.bridge.DefaultBridgeRequestHandler;
import io.forestframework.core.http.routing.RoutingMatchResult;
import io.forestframework.core.http.routing.RoutingMatcher;
import io.forestframework.core.http.websocket.DefaultWebSocketRequestHandler;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * For internal use only.
 *
 * Supports three kinds of request:
 * <ul>
 *     <li>SockJSBridge</li>
 *     <li>WebSocket</li>
 *     <li>Normal request-response</li>
 * </ul>
 *
 * Dispatches the request to the real handlers.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
@Singleton
public class DefaultHttpRequestDispatcher implements HttpRequestHandler {
    private final RoutingMatcher routingMatcher;
    private final DefaultBridgeRequestHandler bridgeRequestHandler;
    private final DefaultWebSocketRequestHandler webSocketRequestHandler;
    private final DefaultPlainHttpRequestHandler httpRequestHandler;

    @Inject
    public DefaultHttpRequestDispatcher(RoutingMatcher routingMatcher,
                                        DefaultBridgeRequestHandler bridgeRequestHandler,
                                        DefaultWebSocketRequestHandler webSocketRequestHandler,
                                        DefaultPlainHttpRequestHandler httpRequestHandler) {
        this.routingMatcher = routingMatcher;
        this.bridgeRequestHandler = bridgeRequestHandler;
        this.webSocketRequestHandler = webSocketRequestHandler;
        this.httpRequestHandler = httpRequestHandler;
    }

    @Override
    public void handle(HttpServerRequest request) {
        RoutingMatchResult routingMatchResult = routingMatcher.match(request);
        routingMatchResult.select(request, bridgeRequestHandler, webSocketRequestHandler, httpRequestHandler);
    }
}





