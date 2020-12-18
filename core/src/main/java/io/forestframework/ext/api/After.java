package io.forestframework.ext.api;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an {@link Extension} should be placed after another extension.
 *
 * For example, if you have:
 * <pre>
 *     {@literal @}After(classes = AnotherExtension.class)
 *     class IShouldBeTheLastExtension implements Extension {
 *     }
 *     class AnotherExtension implements Extension {
 *     }
 *
 *     {@literal @}EnableExtensions(IShouldBeTheLastExtension.class, AnotherExtension.class)
 *     {@literal @}ForestApplication
 *     class App {
 *     }
 * </pre>
 *
 * Then the application startup will fail, complaining you've placed the extensions in wrong order.
 *
 * @see Before
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Inherited
public @interface After {
    Class<? extends Extension>[] classes() default {};
}
