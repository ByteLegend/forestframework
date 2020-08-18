package io.forestframework.core.http.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.blindpirate.annotationmagic.AnnotationMagic;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.PlainHttpContext;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import javax.inject.Singleton;

@Singleton
public class JsonResultProcessor implements RoutingResultProcessor {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String NOT_FOUND_JSON = "{\"message\":\"NOT_FOUND\"";

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

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public Object processResponse(WebContext webContext, Routing routing, Object returnValue) {
        PlainHttpContext context = (PlainHttpContext) webContext;
        JsonResponseBody anno = AnnotationMagic.getOneAnnotationOnMethodOrNull(routing.getHandlerMethod(), JsonResponseBody.class);
        HttpServerResponse response = context.response();
        response.putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON);
        if (returnValue instanceof Buffer) {
            return response.write((Buffer) returnValue);
        } else if (returnValue == null && anno.respond404IfNull()) {
            return response.setStatusCode(HttpStatusCode.NOT_FOUND.getCode()).write(NOT_FOUND_JSON);
        } else {
            return response.write(jsonify(anno, returnValue));
        }
    }
}
