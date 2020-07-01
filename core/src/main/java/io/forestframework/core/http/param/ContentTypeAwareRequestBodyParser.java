package io.forestframework.core.http.param;


import com.google.common.collect.ImmutableMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.ParsableMIMEValue;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ContentTypeAwareRequestBodyParser implements RoutingParameterResolver<Object> {
    private final Map<String, RoutingParameterResolver> contentTypeToResolver = ImmutableMap.<String, RoutingParameterResolver>builder()
            .put("application/json", new JsonRequestBodyParser())
            .build();

    @Override
    public Object resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex) {
        ParsableMIMEValue mimeValue = new ParsableMIMEValue(routingContext.request().getHeader("Content-Type")).forceParse();
        RoutingParameterResolver resolver = contentTypeToResolver.get(mimeValue.value());
        if (resolver == null) {
            throw new UnsupportedOperationException("Unsupported content type: " + mimeValue.value());
        }
        return resolver.resolveArgument(routing, routingContext, paramIndex);
    }
}

