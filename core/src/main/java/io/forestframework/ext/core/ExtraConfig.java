package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Extends(EnableExtensions.class)
@EnableExtensions(extensions = ExtraConfig.ExtraConfigExtension.class)
public @interface ExtraConfig {
    String[] value() default {};

    class ExtraConfigExtension implements Extension {
        @Override
        public void beforeInjector(StartupContext startupContext) {
            final List<ExtraConfig> enableExtensionsAnnotation = startupContext.getEnableExtensionsAnnotation(ExtraConfig.class);
            enableExtensionsAnnotation
                    .stream()
                    .map(ExtraConfig::value)
                    .flatMap(Stream::of)
                    .peek(config -> {
                        if (countMatches(config, '=') < 1) {
                            throw new RuntimeException("Invalid config: " + config);
                        }
                    })
                    .forEach(config -> startupContext.getConfigProvider().addConfig(substringBefore(config, "="), substringAfter(config, "=")));
        }
    }
}
