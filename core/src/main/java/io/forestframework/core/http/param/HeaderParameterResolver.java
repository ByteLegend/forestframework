package io.forestframework.core.http.param;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;

import static com.github.blindpirate.annotationmagic.AnnotationMagic.getOneAnnotationOnMethodParameterOrNull;

@Singleton
class HeaderParameterResolver implements RoutingParameterResolver<WebContext> {
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        Header anno = getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, Header.class);
        return context.request().getHeader(anno.value());
    }
}
