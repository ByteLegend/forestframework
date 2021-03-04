package io.forestframework.core.http;

import com.google.inject.Injector;
import io.forestframework.KotlinSuspendFunctionBridge;
import io.forestframework.core.http.param.RoutingParameterResolver;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.websocket.AbstractWebContext;
import io.forestframework.utils.ReflectionUtils;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import kotlin.coroutines.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractWebRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebRequestHandler.class);
    protected static final Object COMPLETABLE_FUTURE_NIL = new Object();
    protected static final CompletableFuture<Object> NIL_FUTURE = CompletableFuture.completedFuture(COMPLETABLE_FUTURE_NIL);
    protected final Vertx vertx;
    protected final Injector injector;

    public AbstractWebRequestHandler(Vertx vertx, Injector injector) {
        this.vertx = vertx;
        this.injector = injector;
    }

    protected <T> CompletableFuture<T> invokeRouting(Routing routing, AbstractWebContext context) {
        context.setRouting(routing);
        return resolveParameters(routing, context)
            .thenCompose(arguments -> invokeMethod(routing, arguments))
            .thenCompose(returnValue -> processResult(routing, (HttpContext) context, returnValue));
    }

    protected <T> CompletableFuture<T> invokeRoutingWithoutProcessingResult(Routing routing, AbstractWebContext context) {
        context.setRouting(routing);
        return resolveParameters(routing, context)
            .thenCompose(arguments -> invokeMethod(routing, arguments));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> CompletableFuture<T> processResult(Routing routing, HttpContext context, Object returnValue) {
        List<RoutingResultProcessor> resultProcessors = routing.getResultProcessors(injector, returnValue);
        CompletableFuture<Object> current = CompletableFuture.completedFuture(returnValue);
        for (RoutingResultProcessor processor : resultProcessors) {
            current = current.thenCompose(processReturnValue -> adapt(processor.processResponse(context, routing, processReturnValue)));
        }

        return (CompletableFuture) current;
    }

    protected <T> CompletableFuture<T> invokeMethod(Object instance, Method method, Object[] arguments, boolean blocking) {
        if (isKotlinSuspendFunction(method)) {
            return invokeViaKotlinBridge(instance, method, arguments);
        } else if (blocking) {
            return invokeBlockingViaJavaReflection(instance, method, arguments);
        } else {
            return invokeViaJavaReflection(instance, method, arguments);
        }
    }

    private <T> CompletableFuture<T> invokeMethod(Routing routing, Object[] arguments) {
        return invokeMethod(routing.getHandlerInstance(injector), routing.getHandlerMethod(), arguments, routing.isBlocking());
    }

    private <T> CompletableFuture<T> invokeViaJavaReflection(Object instance, Method method, Object[] arguments) {
        try {
            return adapt(ReflectionUtils.invoke(method, instance, arguments));
        } catch (Throwable e) {
            return failedFuture(e);
        }
    }

    protected <T> CompletableFuture<T> failedFuture(Throwable e) {
        CompletableFuture<T> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(e);
        return failedFuture;
    }

    private <T> CompletableFuture<T> invokeBlockingViaJavaReflection(Object instance, Method method, Object[] arguments) {
        return VertxCompletableFuture.from(vertx.executeBlocking((Promise<T> promise) -> {
            try {
                promise.complete(ReflectionUtils.invoke(method, instance, arguments));
            } catch (Throwable e) {
                promise.fail(e);
            }
        }));
    }

    private boolean isKotlinSuspendFunction(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length != 0 && isContinuation(parameterTypes[parameterTypes.length - 1]);
    }

    private CompletableFuture<Object[]> resolveParameters(Routing routing, AbstractWebContext context) {
        try {
            Class<?>[] parameterTypes = routing.getHandlerMethod().getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            CompletableFuture<?>[] futures = new CompletableFuture[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; ++i) {
                int iCopy = i;
                futures[i] = adapt(resolveParameter(routing, i, parameterTypes[i], context))
                    .thenAccept(result -> arguments[iCopy] = result);
            }
            return CompletableFuture.allOf(futures).thenApply(v -> arguments);
        } catch (Throwable t) {
            return failedFuture(t);
        }
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


    @SuppressWarnings("rawtypes")
    private Object resolveParameter(Routing routing, int index, Class<?> argumentType, AbstractWebContext context) {
        if (isContinuation(argumentType)) {
            return COMPLETABLE_FUTURE_NIL;
        }
        RoutingParameterResolver resolver = routing.getParameterResolver(injector, index);
        if (resolver == null) {
            return context.getArgumentInjector().resolve(argumentType, () -> "Don't know how to resolve param " + index + " of " + routing.getHandlerMethod());
        } else {
            return resolver.resolveParameter(context, routing, index);
        }
    }

    protected boolean isContinuation(Class<?> argumentType) {
        return argumentType == Continuation.class;
    }

    private <T> CompletableFuture<T> invokeViaKotlinBridge(Object instance, Method method, Object[] arguments) {
        return KotlinSuspendFunctionBridge.Companion.invoke(
            vertx,
            method,
            instance,
            arguments
        );
    }
}
