package io.forestframework.core.http.socketjs;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(SocketJS.class)
@SocketJS(eventTypes = {SocketJSEventType.ERROR})
public @interface OnError {
    String value() default "";
}
