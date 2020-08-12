package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpStatusCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Extends(ErrorHandler.class)
@ErrorHandler(statusCode = HttpStatusCode.NOT_FOUND)
public @interface On404 {
    int order() default 0;

    @AliasFor("path")
    String value() default "";

    String path() default "";

    String regex() default "";
}
