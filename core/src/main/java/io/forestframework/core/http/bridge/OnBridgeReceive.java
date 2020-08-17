package io.forestframework.core.http.bridge;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(Bridge.class)
@Bridge(eventTypes = {BridgeEventType.RECEIVE})
public @interface OnBridgeReceive {
    String value() default "";
}
