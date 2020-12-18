package io.forestframework.testsupport;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.WithExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells {@link ForestExtension} how to start a Forest application for test.
 */
@SuppressWarnings("JavaDoc")
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Extends(WithExtensions.class)
@WithExtensions(extensions = {BindFreePortExtension.class})
public @interface ForestIntegrationTest {
    /**
     * The target Forest application class to test. Usually the test application is
     * started and some HTTP requests are made against it to test its functionalities.
     *
     * See {@link io.forestframework.core.ForestApplication}
     * @return the target application class for test
     */
    Class<?> appClass();
}
