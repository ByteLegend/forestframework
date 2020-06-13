package io.forestframework;

import io.vertx.ext.web.RoutingContext;


public interface RequestBodyParser<T> {
    T readRequestBody(RoutingContext context, Class<?> argumentClass);
}
