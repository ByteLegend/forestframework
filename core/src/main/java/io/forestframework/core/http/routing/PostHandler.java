package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Extends(Intercept.class)
@Intercept(type = RoutingType.POST_HANDLER)
public @interface PostHandler {
    HttpMethod[] methods() default {HttpMethod.GET};

    String value() default "";

    String regex() default "";
}
