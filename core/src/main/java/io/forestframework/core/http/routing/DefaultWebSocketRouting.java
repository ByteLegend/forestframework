package io.forestframework.core.http.routing;

import io.forestframework.core.http.DefaultRouting;
import io.forestframework.core.http.websocket.WebSocket;
import io.forestframework.core.http.websocket.WebSocketEventType;
import io.forestframework.core.http.websocket.WebSocketRouting;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultWebSocketRouting extends DefaultRouting implements WebSocketRouting {
    private final List<WebSocketEventType> eventTypes;

    public DefaultWebSocketRouting(WebSocket ws, String path, Method handlerMethod) {
        super(RoutingType.WEB_SOCKET, path, "", Collections.emptyList(), handlerMethod);
        eventTypes = Arrays.asList(ws.eventTypes());
    }

    @Override
    public List<WebSocketEventType> getWebSocketEventTypes() {
        return eventTypes;
    }
}
