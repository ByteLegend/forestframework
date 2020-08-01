package io.forestframework.core.http.websocket;

import io.forestframework.core.injector.DefaultImplementedBy;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@DefaultImplementedBy(DefaultWebSocketRequestHandler.class)
public interface WebSocketRequestHandler extends Handler<ServerWebSocket> {
}
