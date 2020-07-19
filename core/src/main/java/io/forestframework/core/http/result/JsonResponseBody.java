package io.forestframework.core.http.result;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Extends(ResultProcessor.class)
@ResultProcessor(by = JsonResultProcessor.class)
public @interface JsonResponseBody {
    boolean pretty() default false;

    boolean respond404IfNull() default true;
}
