package io.forestframework.core.http.param;

import io.forestframework.annotationmagic.Extends;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Extends(ParameterResolver.class)
@ParameterResolver(by = PathParamResolver.class)
public @interface PathParam {
    String value();
}
