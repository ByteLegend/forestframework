package io.forestframework.core.http.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;

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
        } else if (returnValue == null && anno.respond404IfNull()) {
            return routingContext.response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode()).end();
        } else {
            return routingContext.response().end(jsonify(anno, returnValue));
        }
    }

    private String jsonify(JsonResponseBody anno, Object returnValue) {
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
