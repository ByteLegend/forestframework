package io.forestframework.core.http.websocket;

import com.google.inject.Injector;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;

import java.util.Map;

import static io.forestframework.core.http.routing.RoutingMatchResult.WebSocketRoutingMatchResult;

public class DefaultWebSocketContext extends AbstractWebContext implements WebSocketContext {
    private final HttpServerRequest request;
    private final ServerWebSocket webSocket;
    private final WebSocketRoutingMatchResult matchResult;

    public DefaultWebSocketContext(Injector injector,
                                   HttpServerRequest request,
                                   ServerWebSocket webSocket,
                                   WebSocketRoutingMatchResult matchResult) {
        super(injector);
        this.webSocket = webSocket;
        this.matchResult = matchResult;
        this.request = request;
        getArgumentInjector().with(this);
    }

    @Override
    public ServerWebSocket webSocket() {
        return webSocket;
    }

    @Override
    public HttpServerRequest request() {
        return request;
    }

    @Override
    public HttpServerResponse response() {
        return request.response();
    }

    @Override
    public Map<String, String> pathParams() {
        return matchResult.getPathParams();
    }
}
