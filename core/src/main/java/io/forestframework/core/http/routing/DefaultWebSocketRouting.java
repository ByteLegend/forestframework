package io.forestframework.core.http.routing;

import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.websocket.WebSocketEventType;

import java.lang.reflect.Method;
import java.util.List;

public class DefaultWebSocketRouting extends DefaultRouting implements WebSocketRouting {
    private final List<WebSocketEventType> eventTypes;

    public DefaultWebSocketRouting(boolean blocking, RoutingType type, String path, String regexPath, List<HttpMethod> methods, Method handlerMethod, List<WebSocketEventType> eventTypes) {
        super(blocking, type, path, regexPath, methods, handlerMethod);
        this.eventTypes = eventTypes;
    }

    @Override
    public List<WebSocketEventType> getWebSocketEventTypes() {
        return eventTypes;
    }
}
