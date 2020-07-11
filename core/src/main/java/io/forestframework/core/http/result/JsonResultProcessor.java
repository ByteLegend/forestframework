package io.forestframework.core.http.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import com.github.blindpirate.annotationmagic.AnnotationMagic;

import javax.inject.Singleton;

@Singleton
@FastRoutingCompatible
public class JsonResultProcessor implements RoutingResultProcessor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        JsonResponseBody anno = AnnotationMagic.getOneAnnotationOnMethodOrNull(routing.getHandlerMethod(), JsonResponseBody.class);
        // TODO charset
        routingContext.response().putHeader("Content-Type", "application/json");
        if (returnValue instanceof Buffer) {
            return routingContext.response().end((Buffer) returnValue);
        } else {
            return routingContext.response().end(getJson(anno, returnValue));
        }
    }

    private String getJson(JsonResponseBody anno, Object returnValue) {
        try {
            if (anno != null && anno.pretty()) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(returnValue);
            } else {
                return objectMapper.writeValueAsString(returnValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
