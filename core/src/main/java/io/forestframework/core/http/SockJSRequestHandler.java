package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.core.http.routing.RoutingManager;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import org.apiguardian.api.API;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * For internal use only.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Singleton
public class SockJSRequestHandler extends AbstractWebRequestHandler implements HttpRequestHandler {
//    private final Router router;

    @Inject
    public SockJSRequestHandler(Vertx vertx, Injector injector, RoutingManager routingManager) {
        super(vertx, injector);
    }

    @Override
    public void handle(HttpServerRequest request) {
    }
}
