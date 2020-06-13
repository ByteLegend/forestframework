package io.forestframework.annotation;

import io.forestframework.annotationmagic.Extends;
import io.forestframework.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Extends(Route.class)
@Route(methods = {HttpMethod.OPTIONS})
public @interface Options {
    String value() default "";

    String regex() default "";
}
