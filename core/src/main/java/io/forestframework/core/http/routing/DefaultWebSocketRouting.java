package io.forestframework.core.http.routing;

import io.forestframework.core.http.websocket.WebSocketEventType;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.emptyList;

public class DefaultWebSocketRouting extends DefaultRouting implements WebSocketRouting {
    private final List<WebSocketEventType> eventTypes;

    public DefaultWebSocketRouting(boolean blocking, RoutingType type, String path, String regexPath, Method handlerMethod, List<WebSocketEventType> eventTypes) {
        super(blocking, type, path, regexPath, emptyList(), handlerMethod, -1, emptyList(), emptyList());
        this.eventTypes = eventTypes;
    }

    @Override
    public List<WebSocketEventType> getWebSocketEventTypes() {
        return eventTypes;
    }
}
