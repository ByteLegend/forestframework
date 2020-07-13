package io.forestframework.core;

import com.google.inject.BindingAnnotation;
import io.forestframework.ext.api.Extension;

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
@BindingAnnotation
public @interface ForestApplication {
    Class<?>[] include() default {};

    String[] includeName() default {};

    Class<? extends Extension>[] extensions() default {};
}


