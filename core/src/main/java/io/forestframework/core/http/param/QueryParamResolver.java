package io.forestframework.core.http.param;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;

@Singleton
@FastRoutingCompatible
public class QueryParamResolver implements RoutingParameterResolver<Object> {
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        return null;
    }
//    @Override
//    public Object resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex) {
//        QueryParam anno = AnnotationMagic.getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, QueryParam.class);
//        Class<?> paramType = routing.getHandlerMethod().getParameterTypes()[paramIndex];
//
//        String param = routingContext.request().getParam(anno.value());
//        if (param == null) {
//            if (anno.optional()) {
//                return Converter.getDefaultConverter().convert(anno.defaultValue(), String.class, paramType);
//            } else {
//                throw new IllegalArgumentException("Param " + paramIndex + " of " + routing.getHandlerMethod() + " not found!");
//            }
//        } else {
//            return Converter.getDefaultConverter().convert(param, String.class, paramType);
//        }
//    }
}
