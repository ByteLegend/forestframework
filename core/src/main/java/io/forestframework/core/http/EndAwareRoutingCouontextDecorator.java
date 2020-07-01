package io.forestframework.core.http;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

public class EndAwareRoutingCouontextDecorator extends AbstractRoutingContextDecorator {
    private Promise<Void> promise = Promise.promise();

    public EndAwareRoutingCouontextDecorator(RoutingContext delegate) {
        super(delegate);
    }

    public Future<Void> getFuture() {
        return promise.future();
    }

    @Override
    public Future<Void> end(String chunk) {
        promise.complete();
        return super.end(chunk);
    }

    @Override
    public Future<Void> end(Buffer buffer) {
        promise.complete();
        return super.end(buffer);
    }

    @Override
    public Future<Void> end() {
        promise.complete();
        return super.end();
    }
}

