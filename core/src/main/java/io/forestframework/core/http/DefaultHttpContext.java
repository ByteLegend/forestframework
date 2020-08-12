package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.websocket.AbstractWebContext;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apiguardian.api.API;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultHttpContext extends AbstractWebContext implements HttpContext {
    private final HttpServerRequest request;

    public DefaultHttpContext(Injector injector, HttpServerRequest request, RoutingMatchResults routingMatchResults) {
        super(injector, routingMatchResults);
        this.request = new HttpServerRequestWrapper(request);
        this.getArgumentInjector().with(this);
    }

    @Override
    public HttpServerRequest request() {
        return request;
    }

    @Override
    public HttpServerResponse response() {
        return request.response();
    }
}
