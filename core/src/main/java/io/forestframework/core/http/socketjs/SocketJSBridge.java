package io.forestframework.core.http.socketjs;

import io.forestframework.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Extends(Route.class)
public @interface SocketJSBridge {
    String value();
}
