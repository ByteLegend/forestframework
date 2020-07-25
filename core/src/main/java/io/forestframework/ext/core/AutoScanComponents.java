package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.EnableExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Extends(EnableExtensions.class)
@EnableExtensions(extensions = AutoScanComponentsExtension.class)
public @interface AutoScanComponents {
    String APPLICATION_PACKAGE = "APPLICATION_PACKAGE";

    String[] basePackages() default {APPLICATION_PACKAGE};
}
