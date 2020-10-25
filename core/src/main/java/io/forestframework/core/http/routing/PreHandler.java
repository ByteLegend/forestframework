package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Extends(Route.class)
@Route(type = RoutingType.PRE_HANDLER)
public @interface PreHandler {
    int order() default 0;

    HttpMethod[] methods() default {HttpMethod.ALL};

    String value() default "";

    String regex() default "";
}
