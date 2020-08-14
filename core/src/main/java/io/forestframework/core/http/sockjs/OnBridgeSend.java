package io.forestframework.core.http.sockjs;

import com.github.blindpirate.annotationmagic.Extends;
import io.vertx.ext.bridge.BridgeEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(SocketJSBridge.class)
@SocketJSBridge(eventTypes = {BridgeEventType.SEND})
public @interface OnBridgeSend {
    String value() default "";
}