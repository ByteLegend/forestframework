package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpStatusCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Route.class)
@Route(type = RoutingType.ERROR_HANDLER)
public @interface ErrorHandler {
    HttpStatusCode statusCode() default HttpStatusCode.INTERNAL_SERVER_ERROR;

    int start() default -1;

    int end() default -1;

    int order() default 0;

    @AliasFor("path")
    String value() default "";

    String path() default "";

    String regex() default "";
}
