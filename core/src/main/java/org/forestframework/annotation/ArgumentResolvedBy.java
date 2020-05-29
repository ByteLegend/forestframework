package org.forestframework.annotation;

import org.forestframework.RoutingHandlerArgumentResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface ArgumentResolvedBy {
    Class<? extends RoutingHandlerArgumentResolver<?, ?>> value();
}
