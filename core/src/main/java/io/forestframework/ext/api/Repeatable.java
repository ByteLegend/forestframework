package io.forestframework.ext.api;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an {@link Extension} can be repeated multiple times.
 *
 * By default, the extensions will be deduplicated before being instantiated.
 * However, some extensions are designed to be repetitive, like {@link io.forestframework.ext.core.IncludeComponents}.
 *
 * Extensions with this annotation are not deduplicated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Inherited
public @interface Repeatable {
}
