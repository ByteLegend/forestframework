package org.forestframework.annotation;

import org.forestframework.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Route(methods = {HttpMethod.GET})
public @interface Get {
    String value() default "";

    String regex() default "";
}
