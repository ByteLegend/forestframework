package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.PlainHttpRoutingMatchResult;
import io.forestframework.core.http.websocket.AbstractWebContext;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apiguardian.api.API;

import java.util.Map;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultPlainHttpContext extends AbstractWebContext implements PlainHttpContext {
    private final HttpServerRequest request;
    private final PlainHttpRoutingMatchResult matchResult;

    public DefaultPlainHttpContext(Injector injector, HttpServerRequest request, PlainHttpRoutingMatchResult matchResult) {
        super(injector);
        this.request = request;
        this.matchResult = matchResult;
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

    @Override
    public Map<String, String> pathParams() {
        return matchResult.getMatchResultByRouting(getRouting()).getPathParams();
    }
}
