package io.forestframework.core.http.param;

import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import static com.github.blindpirate.annotationmagic.AnnotationMagic.getOneAnnotationOnMethodParameterOrNull;

public class CookieParameterResolver implements RoutingParameterResolver<WebContext> {
    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        Cookie anno = getOneAnnotationOnMethodParameterOrNull(routing.getHandlerMethod(), paramIndex, Cookie.class);
        io.vertx.core.http.Cookie cookie = context.request().getCookie(anno.value());
        return cookie == null ? null : cookie.getValue();
    }
}
