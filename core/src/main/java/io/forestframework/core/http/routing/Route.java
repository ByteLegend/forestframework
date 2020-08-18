package io.forestframework.core.http.routing;


import com.github.blindpirate.annotationmagic.AliasFor;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When Route is annotated on a class, only its path and regexpath can be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface Route {
    int order() default 0;

    RoutingType type() default RoutingType.HANDLER;

    HttpMethod[] methods() default {HttpMethod.ALL};

    @AliasFor("path")
    String value() default "";

    String path() default "";

    String regex() default "";
}
