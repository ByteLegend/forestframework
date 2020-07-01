package io.forestframework.core.http.routing;

import com.google.inject.ImplementedBy;
import io.forestframework.core.http.DefaultRoutingEngine;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.forestframework.core.injector.DefaultImplementedBy;

@DefaultImplementedBy(DefaultRoutingEngine.class)
@ImplementedBy(DefaultRoutingEngine.class)
public interface RoutingEngine {
    Handler<HttpServerRequest> createRouter();
}
