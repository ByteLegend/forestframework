package io.forestframework.core.http.bridge;

import com.google.inject.Injector;
import io.forestframework.core.http.websocket.AbstractWebContext;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.Map;

public class BridgeContext extends AbstractWebContext {
    private final HttpServerRequest request;

    public BridgeContext(Injector injector, SockJSSocket socket) {
        super(injector);
        this.request = socket.routingContext().request();
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
        throw new UnsupportedOperationException();
    }
}
