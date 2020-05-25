package org.forestframework.http;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.forestframework.annotation.DefaultImplementedBy;

@DefaultImplementedBy(DefaultRoutingEngine.class)
public interface RoutingEngine {
    Handler<HttpServerRequest> createRouter();
}
