package io.forestframework.http;

import io.forestframework.RoutingParameterResolver;
import io.forestframework.RoutingResultProcessor;
import io.forestframework.annotation.ParameterResolver;
import io.forestframework.annotation.ResultProcessor;
import io.forestframework.annotation.RoutingType;
import io.forestframework.annotationmagic.AnnotationMagic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public interface Routing {
    default RoutingType getType() {
        return RoutingType.HANDLER;
    }

    List<HttpMethod> getMethods();

    Class<?> getHandlerClass();

    Method getHandlerMethod();

    Object getHandlerInstance();

    default String getPath() {
        return "";
    }

    default String getRegexPath() {
        return "";
    }

    default Class<? extends RoutingParameterResolver<?>> getParameterResolver(int index) {
        ParameterResolver resolver = AnnotationMagic.getOneAnnotationOnMethodParameter(getHandlerMethod(), index, ParameterResolver.class);
        if (resolver == null) {
            throw new IllegalArgumentException("Don't know how to resolve param " + index + " of " + getHandlerMethod());
        }
        return resolver.by();
    }

    default List<Class<? extends RoutingResultProcessor>> getResultProcessors(Object returnValue) {
        return AnnotationMagic.getAnnotationsOnMethod(getHandlerMethod(), ResultProcessor.class)
                .stream()
                .map(ResultProcessor::by)
                .collect(Collectors.toList());
    }
}

