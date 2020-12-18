package io.forestframework.ext.api;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an {@link Extension} should be placed before another extension.
 *
 * For example, if you have:
 * <pre>
 *     {@literal @}Before(classes = AnotherExtension.class)
 *     class IShouldBeTheFirstExtension implements Extension {
 *     }
 *     class AnotherExtension implements Extension {
 *     }
 *
 *     {@literal @}EnableExtensions(AnotherExtension.class, IShouldBeTheFirstExtension.class)
 *     {@literal @}ForestApplication
 *     class App {
 *     }
 * </pre>
 *
 * Then the application startup will fail, complaining you've placed the extensions in wrong order.
 *
 * @see After
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Inherited
public @interface Before {
    Class<? extends Extension>[] classes() default {};
}
