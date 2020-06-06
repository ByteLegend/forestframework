package org.forestframework.http.staticresource;

import org.forestframework.annotation.ResultProcessor;
import org.forestframework.annotationmagic.Extends;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(ResultProcessor.class)
@ResultProcessor(by = StaticResourceProcessor.class)
public @interface StaticResource {
    String webroot() default "";
}

