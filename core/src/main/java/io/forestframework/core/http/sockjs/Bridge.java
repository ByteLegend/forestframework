package io.forestframework.core.http.sockjs;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RoutingType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Extends(Route.class)
@Route(type = RoutingType.BRIDGE)
public @interface Bridge {
    String value() default "";

    BridgeEventType[] eventTypes() default {
            BridgeEventType.SOCKET_CREATED,
            BridgeEventType.SOCKET_CLOSED,
            BridgeEventType.SEND,
            BridgeEventType.PUBLISH,
            BridgeEventType.PUBLISH,
            BridgeEventType.RECEIVE,
            BridgeEventType.UNREGISTER
    };
}
