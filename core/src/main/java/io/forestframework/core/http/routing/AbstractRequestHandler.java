package io.forestframework.core.http.routing;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.inject.Injector;
import io.forestframework.KotlinSuspendFunctionBridge;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.Blocking;
import io.forestframework.core.http.InjectableParameters;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.utils.ReflectionUtils;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import kotlin.coroutines.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestHandler.class);
    private static final Map<Class<? extends RoutingResultProcessor>, RoutingResultProcessor> cache = new HashMap<>();

    protected Vertx vertx;
    protected Injector injector;
    protected boolean devMode;

    public AbstractRequestHandler(Vertx vertx, Injector injector, ConfigProvider configProvider) {
        this.vertx = vertx;
        this.injector = injector;
        this.devMode = "dev".equalsIgnoreCase(configProvider.getInstance("forest.environment", String.class));
    }

    @SuppressWarnings({"unchecked"})
    protected <T> T resolveArgument(Routing routing, int index, Class<T> argumentType, InjectableParameters injectableParameters) {
        RoutingParameterResolver resolver = routing.getParameterResolver(injector, index);
        if (resolver == null) {
            return injectableParameters.resolve(argumentType, () -> "Don't know how to resolve param " + index + " of " + routing.getHandlerMethod());
        } else {
            return (T) resolver.resolveArgument(routing, injectableParameters.resolve(RoutingContext.class, null), index);
        }
    }

    protected Object[] resolveArguments(Routing routing, InjectableParameters injectableParameters) {
        Class<?>[] argumentsType = routing.getHandlerMethod().getParameterTypes();
        Object[] arguments = new Object[argumentsType.length];
        for (int i = 0; i < argumentsType.length; ++i) {
            if (!isContinuation(argumentsType[i])) {
                arguments[i] = resolveArgument(routing, i, argumentsType[i], injectableParameters);
            }
        }
        return arguments;
    }

    CompletableFuture<Object> invokeResultProcessors(RoutingContext context, Routing routing, Object returnValue) {
        List<RoutingResultProcessor> resultProcessors = routing.getResultProcessors(injector, returnValue);
        if (resultProcessors.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Object> current = null;
        for (RoutingResultProcessor processor : resultProcessors) {
            if (current == null) {
                current = adapt(processor.processResponse(context, routing, returnValue));
            } else {
                current = current.thenApply((processReturnValue) -> processor.processResponse(context, routing, processReturnValue))
                        .exceptionally((Throwable failure) -> {
                            LOGGER.error("", failure);
                            return failedFuture(failure);
                        });
            }
        }

        return current;
    }


    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<T> adapt(Object obj) {
        if (obj instanceof Future) {
            return VertxCompletableFuture.from(vertx.getOrCreateContext(), (Future<T>) obj);
        } else if (obj instanceof CompletableFuture) {
            return (CompletableFuture<T>) obj;
        } else {
            return (CompletableFuture<T>) CompletableFuture.completedFuture(obj);
        }
    }

    private <T> CompletableFuture<T> invokeViaJavaReflection(Routing routing, Object[] arguments) {
        try {
            return adapt(ReflectionUtils.invoke(routing.getHandlerMethod(), routing.getHandlerInstance(injector), arguments));
        } catch (Throwable e) {
            return failedFuture(e);
        }
    }

    private <T> CompletableFuture<T> failedFuture(Throwable e) {
        CompletableFuture<T> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(e);
        return failedFuture;
    }

    private RoutingResultProcessor getResultProcessor(Class<? extends RoutingResultProcessor> klass) {
        return cache.computeIfAbsent(klass, injector::getInstance);
    }


    private boolean isKotlinSuspendFunction(Routing routing) {
        Class<?>[] parameterTypes = routing.getHandlerMethod().getParameterTypes();
        return parameterTypes.length != 0 && isContinuation(parameterTypes[parameterTypes.length - 1]);
    }


    boolean isContinuation(Class<?> argumentType) {
        return argumentType == Continuation.class;
    }

    private <T> CompletableFuture<T> invokeBlockingViaJavaReflection(Routing routing, Object[] arguments) {
        return VertxCompletableFuture.from(vertx.executeBlocking((Promise<T> promise) -> {
            try {
                promise.complete(ReflectionUtils.invoke(routing.getHandlerMethod(), routing.getHandlerInstance(injector), arguments));
            } catch (Throwable e) {
                promise.fail(e);
            }
        }));
    }

    private boolean isBlockingMethod(Routing routing) {
        return AnnotationMagic.isAnnotationPresent(routing.getHandlerMethod(), Blocking.class)
                || AnnotationMagic.isAnnotationPresent(routing.getHandlerMethod().getDeclaringClass(), Blocking.class);
    }

    private <T> CompletableFuture<T> invokeViaKotlinBridge(Routing routing, Object[] arguments) {
        return KotlinSuspendFunctionBridge.Companion.invoke(
                vertx,
                routing.getHandlerMethod(),
                routing.getHandlerInstance(injector),
                Arrays.copyOfRange(arguments, 0, arguments.length - 1)
        );
    }

    protected <T> CompletableFuture<T> resolveArgumentsAndInvokeHandler(Routing routing, RoutingContext context) {
        return resolveArgumentsAndInvokeHandler(routing, new InjectableParameters(injector).with(context));
    }

    protected <T> CompletableFuture<T> resolveArgumentsAndInvokeHandler(Routing routing, InjectableParameters injectableParameters) {
        Object[] arguments = resolveArguments(routing, injectableParameters);
        return invokeHandler(routing, arguments);
    }

    protected <T> CompletableFuture<T> invokeHandler(Routing routing, Object[] arguments) {
        if (isKotlinSuspendFunction(routing)) {
            return invokeViaKotlinBridge(routing, arguments);
        } else if (isBlockingMethod(routing)) {
            return invokeBlockingViaJavaReflection(routing, arguments);
        } else {
            return invokeViaJavaReflection(routing, arguments);
        }
    }
}
