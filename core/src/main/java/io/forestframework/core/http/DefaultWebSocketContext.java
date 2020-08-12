package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.websocket.AbstractWebContext;
import io.vertx.core.http.ServerWebSocket;

public class DefaultWebSocketContext extends AbstractWebContext implements WebSocketContext {
    private final ServerWebSocket webSocket;

    public DefaultWebSocketContext(Injector injector, ServerWebSocket webSocket, RoutingMatchResults routingMatchResults) {
        super(injector, routingMatchResults);
        this.webSocket = webSocket;
        getArgumentInjector().with(this);
    }

    @Override
    public ServerWebSocket webSocket() {
        return webSocket;
    }
}
