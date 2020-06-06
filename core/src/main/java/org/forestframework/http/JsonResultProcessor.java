package org.forestframework.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import org.forestframework.RoutingResultProcessor;
import org.forestframework.annotation.JsonResponseBody;
import org.forestframework.annotationmagic.AnnotationMagic;

import javax.inject.Singleton;

@Singleton
public class JsonResultProcessor implements RoutingResultProcessor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        JsonResponseBody anno = AnnotationMagic.getOneAnnotationOnMethod(routing.getHandlerMethod(), JsonResponseBody.class);
        // TODO charset
        return routingContext.response().end(getJson(anno, returnValue));
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
