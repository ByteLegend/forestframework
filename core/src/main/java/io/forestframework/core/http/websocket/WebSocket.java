package io.forestframework.core.http.websocket;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RoutingType;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Extends(Route.class)
@Route(type = RoutingType.WEB_SOCKET)
public @interface WebSocket {
    @AliasFor(target = Route.class, value = "path")
    String value() default "";

    WebSocketEventType[] eventTypes() default {
            WebSocketEventType.OPEN,
            WebSocketEventType.CLOSE,
            WebSocketEventType.MESSAGE,
            WebSocketEventType.ERROR
    };
}
