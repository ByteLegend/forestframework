package io.forestframework.core.http.routing;

import io.forestframework.annotationmagic.AliasFor;
import io.forestframework.annotationmagic.Extends;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Extends(Route.class)
@Route(methods = {HttpMethod.GET})
public @interface Get {
    @AliasFor("path")
    String value() default "";

    String path() default "";

    String regex() default "";
}
