package org.forestframework;

import io.vertx.ext.web.RoutingContext;
import org.forestframework.annotation.PathParam;
import org.forestframework.http.Routing;

public class PathParamResolver implements RoutingHandlerArgumentResolver<Object, PathParam> {
    @Override
    public Object resolveArgument(Routing routing, Class<?> argumentType, RoutingContext routingContext, PathParam pathParam) {
        return routingContext.request().getParam(pathParam.value());
    }
}
