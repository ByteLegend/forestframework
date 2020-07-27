package io.forestframework.core.http.socketjs;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RoutingType;
import io.vertx.ext.bridge.BridgeEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Extends(Route.class)
@Route(type = RoutingType.SOCK_JS_BRIDGE)
public @interface SocketJSBridge {
    String value() default "";

    BridgeEventType[] eventTypes() default {
            BridgeEventType.SOCKET_CREATED,
            BridgeEventType.SOCKET_CLOSED,
            BridgeEventType.SEND,
            BridgeEventType.PUBLISH,
            BridgeEventType.RECEIVE,
            BridgeEventType.REGISTER,
            BridgeEventType.UNREGISTER
    };
}
