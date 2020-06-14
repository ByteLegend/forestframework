package io.forestframework.core;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BindingAnnotation
public @interface ForestApplication {
    Class<?>[] include() default {};

    /**
     * The package/class names to be auto-scanned
     */
    String[] includeName() default {};

    Class<?>[] extensions() default {};
}


