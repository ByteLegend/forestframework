package io.forestframework.testsupport;

import io.forestframework.ext.api.Extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells {@link ForestExtension} how to start a Forest application.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ForestTest {
    /**
     * The target Forest application class to test. Usually the test application is
     * started and some HTTP requests are made against it to test its functionalities.
     *
     * @see {@link io.forestframework.core.ForestApplication}
     * @return the target application class for test
     */
    Class<?> appClass();

    /**
     * Extra configs to pass to the test application, in the form of
     *
     * <pre>
     *     "forest.http.port=8080"
     *     "forest.redis.endpoints=[\"redis://localhost:6370\",\"redis://localhost:6380\"]"
     * </pre>
     *
     * @see {@link io.forestframework.core.config.ConfigProvider}
     * @return the extra configs
     */
    String[] extraConfigs() default {};

    /**
     * Extra extensions to apply to the test application. It can modify the test application,
     * add or remote components, provide extra configs, etc.
     *
     * @return the extra extensions to apply
     */
    Class<? extends Extension>[] extraExtensions() default {};
}
