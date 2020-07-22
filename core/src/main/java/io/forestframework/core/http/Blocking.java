package io.forestframework.core.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a routing handler contains blocking operations, e.g. database access,
 * blocking HTTP connections, etc.
 *
 * Try to use this annotation as le
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Blocking {
}
