package io.forestframework.core.http.socketjs;

import io.forestframework.annotationmagic.Extends;
import io.forestframework.core.http.routing.Route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Extends(Route.class)
public @interface SocketJS {
    String value();
}
