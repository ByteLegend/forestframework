package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Extends(ParameterResolver.class)
@ParameterResolver(by = PathParamResolver.class)
public @interface PathParam {
    String value();
}
