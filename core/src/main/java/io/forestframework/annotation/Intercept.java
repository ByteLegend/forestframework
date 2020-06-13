package io.forestframework.annotation;

import io.forestframework.annotationmagic.Extends;
import io.forestframework.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Extends(Route.class)
public @interface Intercept {
    RoutingType type() default RoutingType.PRE_HANDLER;

    HttpMethod[] methods() default {HttpMethod.GET};

    String value() default "";

    String regex() default "";
}
