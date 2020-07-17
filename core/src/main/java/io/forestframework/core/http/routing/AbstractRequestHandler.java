package io.forestframework.core.http.routing;

import com.google.inject.Injector;
import io.forestframework.KotlinSuspendFunctionBridge;
import io.forestframework.core.http.Blocking;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.utils.ReflectionUtils;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import kotlin.coroutines.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRequestHandler implements RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestHandler.class);
    private static final Map<Class<? extends RoutingResultProcessor>, RoutingResultProcessor> cache = new HashMap<>();

    protected Vertx vertx;
    protected Injector injector;
    protected Routings routings;
    protected boolean devMode;
    private RequestHandler next;

    public AbstractRequestHandler(Vertx vertx, Injector injector, Routings routings, String environment) {
        this.vertx = vertx;
        this.injector = injector;
        this.routings = routings;
        this.devMode = "dev".equalsIgnoreCase(environment);
    }

    void setNext(RequestHandler next) {
        if (this.next != null) {
            throw new UnsupportedOperationException();
        }
        this.next = next;
    }

    protected void next(HttpServerRequest request) {
        if (next != null) {
            next.handle(request);
        }
    }

    @SuppressWarnings({"unchecked"})
    protected <T> T resolveArgument(Routing routing, int index, Class<T> argumentType, RoutingContext routingContext) {
        if (argumentType == RoutingContext.class) {
            return (T) routingContext;
        } else if (argumentType == HttpServerRequest.class) {
            return (T) routingContext.request();
        } else if (argumentType == HttpServerResponse.class) {
            return (T) routingContext.response();
        }

        return (T) routing.getParameterResolver(injector, index).resolveArgument(routing, routingContext, index);
    }

    protected Object[] resolveArguments(Routing routing, RoutingContext context) {
        Class<?>[] argumentsType = routing.getHandlerMethod().getParameterTypes();
        Object[] arguments = new Object[argumentsType.length];
        for (int i = 0; i < argumentsType.length; ++i) {
            if (!isContinuation(argumentsType[i])) {
                arguments[i] = resolveArgument(routing, i, argumentsType[i], context);
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
        return routing.getHandlerMethod().getAnnotation(Blocking.class) != null;
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
        Object[] arguments = resolveArguments(routing, context);
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
