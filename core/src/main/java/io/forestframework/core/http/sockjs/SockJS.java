package io.forestframework.core.http.sockjs;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RoutingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Route.class)
@Route(type = RoutingType.SOCK_JS)
public @interface SockJS {
    String value() default "";

    SockJSEventType[] eventTypes() default {
            SockJSEventType.OPEN,
            SockJSEventType.CLOSE,
            SockJSEventType.MESSAGE,
            SockJSEventType.ERROR
    };
}
