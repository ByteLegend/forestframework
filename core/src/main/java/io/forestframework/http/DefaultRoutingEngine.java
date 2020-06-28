package io.forestframework.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.forestframework.KotlinSuspendFunctionBridge;
import io.forestframework.RoutingResultProcessor;
import io.forestframework.annotation.Blocking;
import io.forestframework.annotation.RoutingType;
import io.forestframework.config.Config;
import io.forestframework.utils.ReflectionUtils;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import kotlin.coroutines.Continuation;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.forestframework.annotation.RoutingType.AFTER_HANDLER_COMPLETION;
import static io.forestframework.annotation.RoutingType.AFTER_HANDLER_FAILURE;
import static io.forestframework.annotation.RoutingType.HANDLER;
import static io.forestframework.annotation.RoutingType.PRE_HANDLER;
import static io.forestframework.annotation.RoutingType.values;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Singleton
public class DefaultRoutingEngine implements RoutingEngine {
    private static final EnumSet<HttpMethod> METHODS_WITHOUT_BODY = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS);
    static final Logger LOGGER = LoggerFactory.getLogger(DefaultRoutingEngine.class);
    private static final String ENABLED_STATES_KEY = "FOREST_ROUTING_ENGINE_ENABLED_STATES";
    // a handler promises to invoke end() in the future so we don't need to do it in finalizing handler, e.g. static resource handler.
    private final Map<RoutingType, List<Routing>> routings;
    private final Injector injector;
    private final Vertx vertx;
    private final boolean devMode;

    @Inject
    public DefaultRoutingEngine(Injector injector,
                                Vertx vertx,
                                @Routings Map<RoutingType, List<Routing>> routings,
                                @Config("forest.environment") String environment) {
        this.injector = injector;
        this.routings = routings;
        this.vertx = vertx;
        this.devMode = "dev".equals(environment);
    }


    @Override
    public Handler<HttpServerRequest> createRouter() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
        router.errorHandler(500, context -> LOGGER.error("", context.failure()));

        configurePreHandlerRoute(router, routings.getOrDefault(PRE_HANDLER, emptyList()));
        configureHandlerRoute(router, routings.getOrDefault(HANDLER, emptyList()));
        configureFinalizingRoute(router);
        return router;
    }

    private void configureFinalizingRoute(io.vertx.ext.web.Router router) {
//        io.vertx.ext.web.Route route = router.route("/*");
//        for (HttpMethod method : HttpMethod.values()) {
//            route = route.method(method.toVertxHttpMethod());
//        }
//        route.handler(context -> {
//            if (!context.response().ended()) {
//                context.end();
//            }
//        });
    }

    private io.vertx.ext.web.Route configureRouter(Router router, Routing routing) {
        io.vertx.ext.web.Route route;
        String path = routing.getPath();
        String regexPath = routing.getRegexPath();
        if (StringUtils.isBlank(path) && StringUtils.isBlank(regexPath)) {
            throw new IllegalArgumentException("Can't configure router: path and regexPath are both empty");
        } else if (StringUtils.isNoneBlank(path) && StringUtils.isNoneBlank(regexPath)) {
            throw new IllegalArgumentException("Can't configure router: path and regexPath are both non-empty");
        } else if (StringUtils.isBlank(path)) {
            route = router.routeWithRegex(routing.getRegexPath());
        } else {
            route = router.route(routing.getPath());
        }

        for (HttpMethod method : routing.getMethods()) {
            route = route.method(method.toVertxHttpMethod());
        }
        if (hasBody(routing.getMethods())) {
            route.handler(BodyHandler.create());
        }
        return route;

    }

    private boolean hasBody(List<HttpMethod> methods) {
        List<HttpMethod> copy = new ArrayList<>(methods);
        copy.removeAll(METHODS_WITHOUT_BODY);
        return !copy.isEmpty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void configureHandlerRoute(io.vertx.ext.web.Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            configureRouter(router, routing)
                    .handler(ctx -> {
                        if (ctx.response().ended()) {
                            new RuntimeException().printStackTrace();
                            return;
                        }
                        RerouteNextAwareRoutingContextDecorator context = new RerouteNextAwareRoutingContextDecorator(ctx);
                        if (context.get(ENABLED_STATES_KEY) == null || handlerEnabled(context, HANDLER)) {
                            Object[] arguments = resolveArguments(routing, context);
                            CompletableFuture<Object> returnValueFuture = invokeHandler(routing, arguments);

                            returnValueFuture.thenAccept(returnValue -> {
                                if (!context.isRerouteInvoked()) {
                                    processResult(context, routing, returnValue).thenAccept(ret -> {
                                        nextIfNecessary(context);
                                    }).exceptionally((Throwable failure) -> {
                                        onHandlerFailure(context, failure);
                                        return null;
                                    });
                                } else {
                                    nextIfNecessary(context);
                                }
                            }).exceptionally((Throwable failure) -> {
                                onHandlerFailure(context, failure);
                                return null;
                            });
                        } else {
                            nextIfNecessary(context);
                        }
                    });
        }
    }

    private void nextIfNecessary(RerouteNextAwareRoutingContextDecorator context) {
        if (!context.response().ended()) {
            context.end();
        }
//        context.nextIfNotInvoked();
    }

    private void onHandlerFailure(RerouteNextAwareRoutingContextDecorator context, Throwable failure) {
        LOGGER.error("", failure);
        context.response().setStatusCode(500);
        if (devMode) {
            context.response().putHeader("Context-Type", "text/plain");
            context.response().write(ExceptionUtils.getStackTrace(failure));
        }
        context.nextIfNotInvoked();
        context.put(ENABLED_STATES_KEY, Arrays.asList(AFTER_HANDLER_FAILURE, AFTER_HANDLER_COMPLETION));
    }

    private static final Map<Class<? extends RoutingResultProcessor>, RoutingResultProcessor> cache = new HashMap<>();

    private RoutingResultProcessor getResultProcessor(Class<? extends RoutingResultProcessor> klass) {
        return cache.computeIfAbsent(klass, injector::getInstance);
    }

    CompletableFuture<Object> processResult(RoutingContext context, Routing routing, Object returnValue) {
        List<RoutingResultProcessor> resultProcessors = routing.getResultProcessors(returnValue)
                .stream()
                .map(this::getResultProcessor)
                .collect(Collectors.toList());
        if (resultProcessors.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Object> current = null;
        for (RoutingResultProcessor processor : resultProcessors) {
            if (current == null) {
                current = adapt(processor.processResponse(context, routing, returnValue));
            } else {
                current = current
                        .thenApply((processReturnValue) -> processor.processResponse(context, routing, processReturnValue))
                        .exceptionally((Throwable failure) -> {
                            failure.printStackTrace();
                            return failedFuture(failure);
                        });
            }
        }

        return current;
    }


    private void configurePreHandlerRoute(io.vertx.ext.web.Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            validPreHandler(routing);
            configureRouter(router, routing).handler(context -> {
                if (context.get(ENABLED_STATES_KEY) == null || handlerEnabled(context, PRE_HANDLER)) {
                    Object[] arguments = resolveArguments(routing, context);
                    CompletableFuture<Boolean> returnValue = invokeHandler(routing, arguments);

                    returnValue.thenAccept(shouldContinue -> {
                        if (shouldContinue) {
                            context.put(ENABLED_STATES_KEY, Arrays.asList(values()));
                        } else {
                            context.put(ENABLED_STATES_KEY, singletonList(AFTER_HANDLER_COMPLETION));
                        }

                        context.next();
                    }).exceptionally(failure -> {
                        failure.printStackTrace();
                        context.put(ENABLED_STATES_KEY, singletonList(AFTER_HANDLER_COMPLETION));
                        context.next();
                        return null;
                    });
                } else {
                    context.next();
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private boolean handlerEnabled(RoutingContext routingContext, RoutingType type) {
        return ((List<RoutingType>) routingContext.data().getOrDefault(ENABLED_STATES_KEY, emptyList())).contains(type);
    }

    private void validPreHandler(Routing routing) {
        // TODO A valid pre handler must returns boolean/Boolean/Future[Boolean]
    }


    <T> CompletableFuture<T> invokeHandler(Routing routing, Object[] arguments) {
        if (isKotlinSuspendFunction(routing)) {
            return invokeViaKotlinBridge(routing, arguments);
        } else if (isBlockingMethod(routing)) {
            return invokeBlockingViaJavaReflection(routing, arguments);
        } else {
            return invokeViaJavaReflection(routing, arguments);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<T> adapt(Object obj) {
        if (obj instanceof Future) {
            return VertxCompletableFuture.from((Future<T>) obj);
        } else if (obj instanceof CompletableFuture) {
            return (CompletableFuture<T>) obj;
        } else {
            return (CompletableFuture<T>) CompletableFuture.completedFuture(obj);
        }
    }

    private <T> CompletableFuture<T> invokeViaJavaReflection(Routing routing, Object[] arguments) {
        try {
            return adapt(ReflectionUtils.invoke(routing.getHandlerMethod(), routing.getHandlerInstance(), arguments));
        } catch (Throwable e) {
            return failedFuture(e);
        }
    }

    private <T> CompletableFuture<T> failedFuture(Throwable e) {
        CompletableFuture<T> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(e);
        return failedFuture;
    }

    private <T> CompletableFuture<T> invokeBlockingViaJavaReflection(Routing routing, Object[] arguments) {
        return VertxCompletableFuture.from(vertx.executeBlocking((Promise<T> promise) -> {
            try {
                promise.complete(ReflectionUtils.invoke(routing.getHandlerMethod(), routing.getHandlerInstance(), arguments));
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
                routing.getHandlerInstance(),
                Arrays.copyOfRange(arguments, 0, arguments.length - 1)
        );
    }

    private boolean isKotlinSuspendFunction(Routing routing) {
        Class<?>[] parameterTypes = routing.getHandlerMethod().getParameterTypes();
        return parameterTypes.length != 0 && isContinuation(parameterTypes[parameterTypes.length - 1]);
    }

    Object[] resolveArguments(Routing routing, RoutingContext context) {
        Class<?>[] argumentsType = routing.getHandlerMethod().getParameterTypes();
        Object[] arguments = new Object[argumentsType.length];
        for (int i = 0; i < argumentsType.length; ++i) {
            if (!isContinuation(argumentsType[i])) {
                arguments[i] = resolveArgument(routing, i, argumentsType[i], context);
            }
        }
        return arguments;
    }

    boolean isContinuation(Class<?> argumentType) {
        return argumentType == Continuation.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
     <T> T resolveArgument(Routing routing, int index, Class<T> argumentType, RoutingContext routingContext) {
        if (argumentType == RoutingContext.class) {
            return (T) routingContext;
        } else if (argumentType == HttpServerRequest.class) {
            return (T) routingContext.request();
        } else if (argumentType == HttpServerResponse.class) {
            return (T) routingContext.response();
        }

        return (T) injector.getInstance(routing.getParameterResolver(index))
                .resolveArgument(routing, routingContext, index);
    }
}
