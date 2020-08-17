package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import io.vertx.core.http.ServerWebSocket;

import java.util.Map;

import static io.forestframework.core.http.routing.RoutingMatchResult.WebSocketRoutingMatchResult;

public class DefaultWebSocketContext extends AbstractWebContext implements WebSocketContext {
    private final ServerWebSocket webSocket;
    private final WebSocketRoutingMatchResult matchResult;

    public DefaultWebSocketContext(Injector injector, ServerWebSocket webSocket, WebSocketRoutingMatchResult matchResult) {
        super(injector);
        this.webSocket = webSocket;
        this.matchResult = matchResult;
        getArgumentInjector().with(this);
    }

    @Override
    public ServerWebSocket webSocket() {
        return webSocket;
    }

    @Override
    public Map<String, String> pathParams() {
        return matchResult.getPathParams();
    }
}
