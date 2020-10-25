package io.forestframework.core.http.result;

import com.github.blindpirate.annotationmagic.Extends;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.forestframework.core.http.HttpContext;
import io.forestframework.core.http.HttpResponse;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.buffer.Buffer;

import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.forestframework.core.http.OptimizedHeaders.CONTENT_TYPE_TEXT_PLAIN;
import static io.forestframework.core.http.OptimizedHeaders.HEADER_CONTENT_TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Extends(ResultProcessor.class)
@ResultProcessor(by = PlainText.PlainTextResultProcessor.class)
public @interface PlainText {
    @Singleton
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    class PlainTextResultProcessor implements RoutingResultProcessor {
        @Override
        public Object processResponse(HttpContext context, Routing routing, Object returnValue) {
            HttpResponse response = (HttpResponse) context.response();
            response.putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_TEXT_PLAIN);
            if (returnValue instanceof Buffer) {
                response.writeLater((Buffer) returnValue);
            } else {
                response.writeLater(returnValue.toString());
            }
            return returnValue;
        }
    }

}
