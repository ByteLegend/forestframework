package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.config.Converter;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;

/**
 * Resolve parameters from request url path. Three kins of path parameters are supported:
 * 1. /user/:id -&gt; @PathParam("id")
 * 2. Regex path: \/js\/(?&lt;file&gt;.+) -&gt; @PathParam("file")
 * 3. Wildcard path: /static/* -&gt; @PathParam("*")
 */
@Singleton
public class PathParamResolver implements RoutingParameterResolver<Object> {
    @SuppressWarnings("unchecked")
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        PathParam pathParam = AnnotationMagic.getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, PathParam.class);

        Class<?> paramType = routing.getHandlerMethod().getParameterTypes()[paramIndex];
        return Converter.getDefaultConverter().convert(context.pathParam(pathParam.value()), String.class, paramType);
    }
}
