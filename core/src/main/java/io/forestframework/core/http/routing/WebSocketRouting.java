package io.forestframework.core.http.routing;

import io.forestframework.core.http.websocket.WebSocketEventType;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface WebSocketRouting extends Routing {
    List<WebSocketEventType> getWebSocketEventTypes();
}
