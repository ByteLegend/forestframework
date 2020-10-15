package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.PlainHttpRoutingMatchResult;
import io.forestframework.core.http.websocket.AbstractWebContext;
import org.apiguardian.api.API;

import java.util.Map;

/**
 * For internal use only.
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public class DefaultHttpContext extends AbstractWebContext implements HttpContext {
    private final HttpRequest request;
    private final PlainHttpRoutingMatchResult matchResult;

    public DefaultHttpContext(Injector injector, HttpRequest request, PlainHttpRoutingMatchResult matchResult) {
        super(injector);
        this.request = request;
        this.matchResult = matchResult;
        this.getArgumentInjector().with(this);
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public HttpResponse response() {
        return (HttpResponse) request.response();
    }

    @Override
    public Map<String, String> pathParams() {
        return matchResult.getMatchResultByRouting(getRouting()).getPathParams();
    }
}
