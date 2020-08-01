package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Extends(EnableExtensions.class)
@EnableExtensions(extensions = IncludeComponents.IncludeComponentExtension.class)
public @interface IncludeComponents {
    Class<?>[] classes() default {};

    class IncludeComponentExtension implements Extension {
        @Override
        public void beforeInjector(StartupContext startupContext) {
            startupContext.getEnableExtensionsAnnotation(IncludeComponents.class)
                    .stream()
                    .map(IncludeComponents::classes)
                    .flatMap(Stream::of)
                    .forEach(startupContext.getComponentClasses()::add);
        }
    }
}
