package io.forestframework.config;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an annotated element can be injected fromm configuration files.
 * See {@link io.forestframework.config.ConfigProvider}
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface Config {
    String value();
}
