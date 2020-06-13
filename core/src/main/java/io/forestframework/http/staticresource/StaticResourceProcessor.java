package io.forestframework.http.staticresource;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import io.forestframework.RoutingResultProcessor;
import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.config.ConfigProvider;
import io.forestframework.http.EndAwareRoutingCouontextDecorator;
import io.forestframework.http.Routing;

import javax.inject.Inject;

public class StaticResourceProcessor implements RoutingResultProcessor {
    private final ForkedStaticHandlerImpl staticHandler;
    private final String webroot;

    @Inject
    public StaticResourceProcessor(ConfigProvider configProvider) {
        staticHandler = new ForkedStaticHandlerImpl();
        webroot = configProvider.getInstance("forest.static.webroot", String.class);
    }

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        EndAwareRoutingCouontextDecorator endAwareRoutingContext = new EndAwareRoutingCouontextDecorator(routingContext);
        StaticResource staticResource = AnnotationMagic.getAnnotationsOnMethod(routing.getHandlerMethod(), StaticResource.class).get(0);
        String resourcePath = (String) returnValue;
        resourcePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        if (StringUtils.isBlank(staticResource.webroot())) {
            staticHandler.sendStatic(routingContext, webroot + resourcePath);
        } else {
            staticHandler.sendStatic(routingContext, staticResource.webroot() + resourcePath);
        }
        return endAwareRoutingContext.getFuture();
    }
}

