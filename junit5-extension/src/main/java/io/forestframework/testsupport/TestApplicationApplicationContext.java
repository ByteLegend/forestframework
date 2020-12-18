package io.forestframework.testsupport;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.DefaultApplicationContext;
import io.forestframework.ext.api.WithExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.utils.StartupUtils;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestApplicationApplicationContext extends DefaultApplicationContext {
    private final List<WithExtensions> enableExtensionsAnnotations;

    public TestApplicationApplicationContext(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<WithExtensions> enableExtensionsAnnotations) {
        super(vertx, applicationClass, configProvider, instantiateExtensions(enableExtensionsAnnotations));
        this.enableExtensionsAnnotations = enableExtensionsAnnotations;
    }

    private static List<Extension> instantiateExtensions(List<WithExtensions> enableExtensionsAnnotations) {
        return enableExtensionsAnnotations.stream()
                .map(WithExtensions::extensions)
                .flatMap(Stream::of)
                .distinct()
                .map(klass -> (Extension) StartupUtils.instantiateWithDefaultConstructor(klass))
                .collect(Collectors.toList());
    }

//    @Override
//    public <T extends Annotation> List<T> getEnableExtensionsAnnotation(Class<T> annotationClass) {
//        return enableExtensionsAnnotations.stream()
//                .filter(it -> AnnotationMagic.instanceOf(it, annotationClass))
//                .map(it -> AnnotationMagic.cast(it, annotationClass))
//                .collect(Collectors.toList());
//    }
}
