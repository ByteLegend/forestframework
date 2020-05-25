package org.forestframework.annotation;

import org.forestframework.bootstrap.DefaultInjectorCreator;
import org.forestframework.bootstrap.InjectorCreator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ForestApplication {
    Class<?>[] include() default {};

    /**
     * The package/class names to be auto-scanned
     */
    String[] includeName() default {};

    Class<? extends InjectorCreator> injectorCreatedBy() default DefaultInjectorCreator.class;
}


