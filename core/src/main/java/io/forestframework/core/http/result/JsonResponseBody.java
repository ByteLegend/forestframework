package io.forestframework.core.http.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.github.blindpirate.annotationmagic.Extends;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.forestframework.core.http.HttpContext;
import io.forestframework.core.http.HttpResponse;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;

import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Extends(ResultProcessor.class)
@ResultProcessor(by = JsonResponseBody.JsonResultProcessor.class)
public @interface JsonResponseBody {
    boolean pretty() default false;

    boolean respond404IfNull() default true;

    @Singleton
    class JsonResultProcessor implements RoutingResultProcessor {
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
        public Object processResponse(HttpContext context, Routing routing, Object returnValue) {
            JsonResponseBody anno = AnnotationMagic.getOneAnnotationOnMethodOrNull(routing.getHandlerMethod(), JsonResponseBody.class);
            HttpResponse response = (HttpResponse) context.response();
            response.putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON);
            if (returnValue instanceof String) {
                response.writeLater((String) returnValue);
            } else if (returnValue instanceof Buffer) {
                response.writeLater((Buffer) returnValue);
            } else if (returnValue == null && anno.respond404IfNull()) {
                response.setStatusCode(HttpStatusCode.NOT_FOUND.getCode()).writeLater(NOT_FOUND_JSON);
            } else {
                response.writeLater(jsonify(anno, returnValue));
            }
            return returnValue;
        }
    }

}
