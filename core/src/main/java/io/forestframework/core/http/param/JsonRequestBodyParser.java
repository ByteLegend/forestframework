package io.forestframework.core.http.param;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.forestframework.core.http.routing.Routing;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

@Singleton
public class JsonRequestBodyParser implements ContentTypeAwareRoutingParameterResolver<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex) {
        Class<?> paramType = routing.getHandlerMethod().getParameterTypes()[paramIndex];
        if (paramType == String.class) {
            // TODO respect application/json;charset=XXX
            return routingContext.getBodyAsString();
        }

        try {
            return objectMapper.readValue((InputStream) new ByteBufInputStream(routingContext.getBody().getByteBuf()), paramType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String contentType() {
        return "application/json";
    }
}
