package io.forestframework.core.http;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's expensive to create a {@link io.vertx.ext.web.RoutingContext}.
 * If a class ({@link io.forestframework.core.http.param.RoutingParameterResolver}/{@link io.forestframework.core.http.result.RoutingResultProcessor})
 * doesn't need the expensive RoutingContext operations to finish its work, we say it's fast routing compatible.
 * We'll create a {@link io.forestframework.core.http.routing.FastRoutingContext} for these fast routing compatible classes.
 * This annotation is used to mark such fast routing compatible classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public @interface FastRoutingCompatible {
}
