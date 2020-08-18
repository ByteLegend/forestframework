package io.forestframework.core.http.websocket;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(WebSocket.class)
@WebSocket(eventTypes = WebSocketEventType.CLOSE)
public @interface OnWSClose {
    String value() default "";
}
