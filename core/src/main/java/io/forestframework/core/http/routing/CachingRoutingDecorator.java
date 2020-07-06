package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.param.ParameterResolver;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.result.ResultProcessor;
import io.forestframework.core.http.result.RoutingResultProcessor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A decorator for {@link Routing}s, mainly for caching purpose.
 */
public class CachingRoutingDecorator implements Routing {
    private static final Object[] UNINITIALIZED = {};
    private final Routing delegate;
    private Object singletonHandlerInstance = UNINITIALIZED;
    private Object[] cachedParameterResolvers = UNINITIALIZED;
    private Object[] cachedResultProcessors = UNINITIALIZED;

    CachingRoutingDecorator(Routing delegate) {
        this.delegate = delegate;
    }

    @Override
    public Method getHandlerMethod() {
        return delegate.getHandlerMethod();
    }

    @Override
    public RoutingType getType() {
        return delegate.getType();
    }

    @Override
    public List<HttpMethod> getMethods() {
        return delegate.getMethods();
    }

    @Override
    public Object getHandlerInstance(Injector injector) {
        if (singletonHandlerInstance == UNINITIALIZED) {
            // No synchronized required, guaranteed by Guice
            singletonHandlerInstance = initSingletonHandlerInstance(injector);
        }

        if (singletonHandlerInstance != null) {
            return singletonHandlerInstance;
        } else {
            return delegate.getHandlerInstance(injector);
        }
    }

    private boolean isSingleton(Class<?> klass) {
        return klass.isAnnotationPresent(Singleton.class) || klass.isAnnotationPresent(javax.inject.Singleton.class);
    }

    private Object initSingletonHandlerInstance(Injector injector) {
        Object handlerInstance = delegate.getHandlerInstance(injector);
        if (isSingleton(handlerInstance.getClass())) {
            return handlerInstance;
        }
        return null;
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public String getRegexPath() {
        return delegate.getRegexPath();
    }

    @Override
    public RoutingParameterResolver<?> getParameterResolver(Injector injector, int index) {
        if (cachedParameterResolvers == UNINITIALIZED) {
            cachedParameterResolvers = initCachedParameterResolvers(injector);
        }

        if (cachedParameterResolvers[index] instanceof RoutingParameterResolver) {
            return (RoutingParameterResolver) cachedParameterResolvers[index];
        } else {
            return (RoutingParameterResolver) injector.getInstance((Class<?>) cachedParameterResolvers[index]);
        }
    }

    private Object[] initCachedParameterResolvers(Injector injector) {
        Object[] ret = new Object[delegate.getHandlerMethod().getParameters().length];
        for (int i = 0; i < ret.length; ++i) {
            ParameterResolver resolver = AnnotationMagic.getOneAnnotationOnMethodParameter(delegate.getHandlerMethod(), i, ParameterResolver.class);
            if (resolver != null) {
                Class<? extends RoutingParameterResolver<?>> resolverClass = resolver.by();
                ret[i] = isSingleton(resolverClass) ? injector.getInstance(resolverClass) : resolverClass;
            }
        }
        return ret;
    }

    @Override
    public List<RoutingResultProcessor> getResultProcessors(Injector injector, Object returnValue) {
        if (cachedResultProcessors == UNINITIALIZED) {
            cachedResultProcessors = initCachedResultProcessors(injector);
        }
        return Stream.of(cachedParameterResolvers)
                .map(obj -> (obj instanceof RoutingResultProcessor) ? (RoutingResultProcessor) obj : (RoutingResultProcessor) injector.getInstance((Class<?>) obj))
                .collect(Collectors.toList());
    }

    private Object[] initCachedResultProcessors(Injector injector) {
        List<ResultProcessor> resultProcessAnnotations = AnnotationMagic.getAnnotationsOnMethod(delegate.getHandlerMethod(), ResultProcessor.class);
        Object[] ret = new Object[resultProcessAnnotations.size()];
        for (int i = 0; i < ret.length; ++i) {
            Class<? extends RoutingResultProcessor> processorClass = resultProcessAnnotations.get(i).by();
            ret[i] = isSingleton(processorClass) ? injector.getInstance(processorClass) : processorClass;
        }
        return ret;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
