package org.forestframework.annotation;

import org.forestframework.StaticResourceProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ReturnValueProcessedBy(StaticResourceProcessor.class)
public @interface StaticResource {
}
