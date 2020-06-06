package org.forestframework.annotation;

import org.forestframework.annotationmagic.Extends;

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

    String regex();
}
