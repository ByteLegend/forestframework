package io.forestframework.core.http.websocket;

import io.forestframework.core.http.WebContext;
import io.vertx.core.http.ServerWebSocket;
import org.apiguardian.api.API;

/**
 * For internal use only.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface WebSocketContext extends WebContext {
    ServerWebSocket webSocket();
}
