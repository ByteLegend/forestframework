package io.forestframework.core.http.result;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpContext;
import io.forestframework.core.http.routing.Routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Extends(ResultProcessor.class)
@ResultProcessor(by = Redirect.RedirectResultProcessor.class)
public @interface Redirect {
    class RedirectResultProcessor implements RoutingResultProcessor {
        @Override
        public Object processResponse(HttpContext context, Routing routing, Object returnValue) {
            context.redirect(returnValue.toString());
            return returnValue;
        }
    }
}
