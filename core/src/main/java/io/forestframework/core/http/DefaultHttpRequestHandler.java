package io.forestframework.core.http;

import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultHttpRequestHandler implements HttpRequestHandler {
    private final List<ChainedRequestHandler> handlers;

    @Inject
    public DefaultHttpRequestHandler(@ChainedHttpRequestHandlers List<ChainedRequestHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(HttpServerRequest request) {
        new DefaultRequestHandlerChain(handlers).handleNext(request);
    }
}
