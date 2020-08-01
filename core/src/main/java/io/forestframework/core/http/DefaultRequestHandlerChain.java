package io.forestframework.core.http;

import io.vertx.core.http.HttpServerRequest;

import java.util.List;

class DefaultRequestHandlerChain implements RequestHandlerChain {
    private final List<ChainedRequestHandler> handlers;
    private int index;

    public DefaultRequestHandlerChain(List<ChainedRequestHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleNext(HttpServerRequest request) {
        int nextIndex = index++;
        if (nextIndex < handlers.size()) {
            handlers.get(nextIndex).handle(request, this);
        }
    }
}
