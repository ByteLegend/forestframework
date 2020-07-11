package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Extends(ParameterResolver.class)
@ParameterResolver(by = QueryParamResolver.class)
public @interface QueryParam {
    String value();

    String defaultValue() default "";

    boolean optional() default false;
}
