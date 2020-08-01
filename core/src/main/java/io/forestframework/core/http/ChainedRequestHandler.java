package io.forestframework.core.http;

import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface ChainedRequestHandler {
    void handle(HttpServerRequest request, RequestHandlerChain handlerChain);
}
