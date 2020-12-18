package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.WithExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Extends(WithExtensions.class)
@WithExtensions(extensions = StaticResourceExtension.class)
public @interface WithStaticResource {
    String webroot() default "static";

    String[] webroots() default {};
}
