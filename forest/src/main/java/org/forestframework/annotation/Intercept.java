package org.forestframework.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Route
public @interface Intercept {
    RouteType type() default RouteType.PRE_HANDLER;

    HttpMethod[] methods() default {HttpMethod.GET};

    String value() default "";

    String regex() default "";
}
