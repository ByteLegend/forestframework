package io.forestframework.core.http.websocket;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RoutingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Extends(Route.class)
@Route(type = RoutingType.WEB_SOCKET)
public @interface WebSocket {
    String value() default "";

    WebSocketEventType[] eventTypes() default {
            WebSocketEventType.CLOSE,
            WebSocketEventType.OPEN,
            WebSocketEventType.MESSAGE,
            WebSocketEventType.ERROR
    };
}
