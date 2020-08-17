package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;

/**
 * Resolve parameters from request url path. Three kins of path parameters are supported:
 * 1. /user/:id -> @PathParam("id")
 * 2. Regex path: \/js\/(?<file>.+) -> @PathParam("file")
 * 3. Wildcard path: /static/* -> @PathParam("*")
 */
@Singleton
public class PathParamResolver implements RoutingParameterResolver<Object> {
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        PathParam pathParam = AnnotationMagic.getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, PathParam.class);
        return context.pathParam(pathParam.value());
    }
}
