package io.forestframework.annotation;

import io.forestframework.ContentTypeAwareRequestBodyParser;
import io.forestframework.annotationmagic.Extends;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Extends(ParameterResolver.class)
@ParameterResolver(by = ContentTypeAwareRequestBodyParser.class)
public @interface RequestBody {
}
