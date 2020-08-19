package io.forestframework.testsupport;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.DefaultStartupContext;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.utils.StartupUtils;
import io.vertx.core.Vertx;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestApplicationStartupContext extends DefaultStartupContext {
    private final List<EnableExtensions> enableExtensionsAnnotations;

    public TestApplicationStartupContext(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<EnableExtensions> enableExtensionsAnnotations) {
        super(vertx, applicationClass, configProvider, instantiateExtensions(enableExtensionsAnnotations));
        this.enableExtensionsAnnotations = enableExtensionsAnnotations;
    }

    private static List<Extension> instantiateExtensions(List<EnableExtensions> enableExtensionsAnnotations) {
        return enableExtensionsAnnotations.stream()
                .map(EnableExtensions::extensions)
                .flatMap(Stream::of)
                .distinct()
                .map(klass -> (Extension) StartupUtils.instantiateWithDefaultConstructor(klass))
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Annotation> List<T> getEnableExtensionsAnnotation(Class<T> annotationClass) {
        return enableExtensionsAnnotations.stream()
                .filter(it -> AnnotationMagic.instanceOf(it, annotationClass))
                .map(it -> AnnotationMagic.cast(it, annotationClass))
                .collect(Collectors.toList());
    }
}
