package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.Before;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.WithExtensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Extends(WithExtensions.class)
@WithExtensions(extensions = ExtraConfig.ExtraConfigExtension.class)
public @interface ExtraConfig {
    String[] value() default {};

    @Before(classes = AutoComponentScanExtension.class)
    class ExtraConfigExtension implements Extension {
        private final ExtraConfig extraConfig;

        public ExtraConfigExtension(ExtraConfig extraConfig) {
            this.extraConfig = extraConfig;
        }

        @Override
        public void start(ApplicationContext applicationContext) {
            Stream.of(extraConfig.value())
                  .peek(config -> {
                      if (countMatches(config, '=') < 1) {
                          throw new RuntimeException("Invalid config: " + config);
                      }
                  })
                  .forEach(config -> applicationContext.getConfigProvider().addConfig(substringBefore(config, "="), substringAfter(config, "=")));
        }
    }
}
