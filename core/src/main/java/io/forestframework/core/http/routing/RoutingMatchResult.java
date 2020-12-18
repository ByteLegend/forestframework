package io.forestframework.core.http.routing;

import io.forestframework.core.http.DefaultHttpRequest;
import io.forestframework.core.http.DefaultPlainHttpRequestHandler;
import io.forestframework.core.http.bridge.BridgeEventType;
import io.forestframework.core.http.bridge.DefaultBridgeRequestHandler;
import io.forestframework.core.http.websocket.DefaultWebSocketRequestHandler;
import io.forestframework.core.http.websocket.WebSocketEventType;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

import java.util.Map;

@API(status = API.Status.INTERNAL, since = "0.1")
public interface RoutingMatchResult {
    void select(HttpServerRequest request,
                DefaultBridgeRequestHandler bridgeRequestHandler,
                DefaultWebSocketRequestHandler webSocketRequestHandler,
                DefaultPlainHttpRequestHandler plainHttpRequestHandler);

    class BridgeRoutingMatchResult implements RoutingMatchResult {
        private final Map<BridgeEventType, BridgeRouting> routings;

        public BridgeRoutingMatchResult(Map<BridgeEventType, BridgeRouting> routings) {
            this.routings = routings;
        }

        @Override
        public void select(HttpServerRequest request,
                           DefaultBridgeRequestHandler bridgeRequestHandler,
                           DefaultWebSocketRequestHandler webSocketRequestHandler,
                           DefaultPlainHttpRequestHandler plainHttpRequestHandler) {
            bridgeRequestHandler.handle(new DefaultHttpRequest(request, this));
        }

        public Map<BridgeEventType, BridgeRouting> getRoutings() {
            return routings;
        }
    }

    class WebSocketRoutingMatchResult implements RoutingMatchResult {
        private final String path;
        private final Map<String, String> pathParams;
        private Map<WebSocketEventType, WebSocketRouting> routings;

        public WebSocketRoutingMatchResult(String path, Map<String, String> pathParams) {
            this.path = path;
            this.pathParams = pathParams;
        }

        @Override
        public void select(HttpServerRequest request,
                           DefaultBridgeRequestHandler bridgeRequestHandler,
                           DefaultWebSocketRequestHandler webSocketRequestHandler,
                           DefaultPlainHttpRequestHandler plainHttpRequestHandler) {
            webSocketRequestHandler.handle(new DefaultHttpRequest(request, this));
        }

        public Map<String, String> getPathParams() {
            return pathParams;
        }

        public String getPath() {
            return path;
        }

        public Map<WebSocketEventType, WebSocketRouting> getRoutings() {
            return routings;
        }

        public void setRoutings(Map<WebSocketEventType, WebSocketRouting> routings) {
            this.routings = routings;
        }
    }
}
