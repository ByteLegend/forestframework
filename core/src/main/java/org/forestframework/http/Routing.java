package org.forestframework.http;

import org.forestframework.RoutingParameterResolver;
import org.forestframework.RoutingResultProcessor;
import org.forestframework.annotation.ParameterResolver;
import org.forestframework.annotation.ResultProcessor;
import org.forestframework.annotation.RoutingType;
import org.forestframework.annotationmagic.AnnotationMagic;

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

