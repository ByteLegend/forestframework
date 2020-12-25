package io.forestframework.core.http.param;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;

import static com.github.blindpirate.annotationmagic.AnnotationMagic.getOneAnnotationOnMethodParameterOrNull;

@Singleton
public class ContextDataParameterResolver implements RoutingParameterResolver<WebContext> {
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        ContextData anno = getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, ContextData.class);
        Class<?> parameterType = routing.getHandlerMethod().getParameterTypes()[paramIndex];
        return parameterType.cast(context.get(anno.value()));
    }
}
