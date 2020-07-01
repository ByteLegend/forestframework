package io.forestframework.core.http.param;

import io.vertx.ext.web.RoutingContext;

public class FormRequestBodyParser implements RequestBodyParser<Form<?>> {
    @Override
    public Form<?> readRequestBody(RoutingContext context, Class<?> argumentClass) {
        return null;
    }
}
