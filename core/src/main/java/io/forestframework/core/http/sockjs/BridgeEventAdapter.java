package io.forestframework.core.http.sockjs;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

public class BridgeEventAdapter implements BridgeEvent {
    private io.vertx.ext.web.handler.sockjs.BridgeEvent delegate;
    private BridgeEventType eventType;

    public BridgeEventAdapter(io.vertx.ext.web.handler.sockjs.BridgeEvent delegate) {
        this.delegate = delegate;
        this.eventType = BridgeEventType.fromVertxType(delegate.type());
    }

    @Override
    public BridgeEventType type() {
        return eventType;
    }

    @Override
    public JsonObject getRawMessage() {
        return delegate.getRawMessage();
    }

    @Override
    public BridgeEvent setRawMessage(JsonObject message) {
        delegate.setRawMessage(message);
        return this;
    }

    @Override
    public SockJSSocket socket() {
        return delegate.socket();
    }

    @Override
    public boolean tryComplete(Boolean result) {
        return delegate.tryComplete(result);
    }

    @Override
    public boolean tryFail(Throwable cause) {
        return delegate.tryFail(cause);
    }

    @Override
    public Future<Boolean> future() {
        return delegate.future();
    }
}
