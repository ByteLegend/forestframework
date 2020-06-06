package org.forestframework.annotation;

import org.forestframework.annotationmagic.Extends;
import org.forestframework.http.JsonResultProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
@Extends(ResultProcessor.class)
@ResultProcessor(by = JsonResultProcessor.class)
public @interface JsonResponseBody {
    boolean pretty() default false;
}
