package org.forestframework.annotation;

import org.forestframework.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Extends(ParameterResolver.class)
public @interface QueryParam {
    String value();

    String defaultValue() default "";

    boolean optional() default false;
}
