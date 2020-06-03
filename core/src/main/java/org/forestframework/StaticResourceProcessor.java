package org.forestframework;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.forestframework.annotation.StaticResource;
import org.forestframework.http.EndAwareRoutingCouontextDecorator;
import org.forestframework.http.Routing;

public class StaticResourceProcessor implements ResponseProcessor<StaticResource> {
    private final StaticHandler staticHandler = StaticHandler.create();

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue, StaticResource annotation) {
        EndAwareRoutingCouontextDecorator endAwareRoutingContext = new EndAwareRoutingCouontextDecorator(routingContext);
        staticHandler.handle(routingContext);
        return endAwareRoutingContext.getFuture();
    }

    public static String webroot(String absolutePath) {
        return absolutePath;
    }
}

