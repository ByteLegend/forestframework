package io.forestframework.core.http.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marking annotation which indicates the marked class ({@link io.forestframework.core.http.param.RoutingParameterResolver} or
 * {@link io.forestframework.core.http.result.RoutingResultProcessor}) can be used in "fast routing", i.e. the marked class
 * doesn't invoke expensive {@link io.vertx.ext.web.RoutingContext} methods such as path matching, header parsing, etc.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FastRouting {
}
