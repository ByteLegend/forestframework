package org.forestframework.annotation;

import org.forestframework.annotationmagic.AliasFor;
import org.forestframework.annotationmagic.Extends;
import org.forestframework.http.HttpMethod;

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
