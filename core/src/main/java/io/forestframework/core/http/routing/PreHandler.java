package io.forestframework.core.http.routing;

import io.forestframework.annotationmagic.Extends;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.routing.Intercept;
import io.forestframework.core.http.routing.RoutingType;

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
@Extends(Intercept.class)
@Intercept(type = RoutingType.PRE_HANDLER)
public @interface PreHandler {
    HttpMethod[] methods() default {HttpMethod.GET};

    String value() default "";

    String regex() default "";
}
