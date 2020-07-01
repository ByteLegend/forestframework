package io.forestframework.core.http.param;

import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.core.config.Converter;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.utils.Assert;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class QueryParamResolver implements RoutingParameterResolver<Object> {
    @Override
    public Object resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex) {
        QueryParam anno = AnnotationMagic.getOneAnnotationOnMethodParameter(routing.getHandlerMethod(), paramIndex, QueryParam.class);
        Class<?> paramType = routing.getHandlerMethod().getParameterTypes()[paramIndex];

        List<String> param = routingContext.queryParam(anno.value());
        Assert.isTrue(param.size() < 2);
        if (param.isEmpty()) {
            if (anno.optional()) {
                return Converter.getDefaultConverter().convert(anno.defaultValue(), String.class, paramType);
            } else {
                throw new IllegalArgumentException("Param " + paramIndex + " of " + routing.getHandlerMethod() + " not found!");
            }
        } else {
            return Converter.getDefaultConverter().convert(param.get(0), String.class, paramType);
        }
    }
}
