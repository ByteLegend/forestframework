package io.forestframework.core.http;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public interface HttpContext extends WebContext {
    HttpServerRequest request();

    HttpServerResponse response();
}
