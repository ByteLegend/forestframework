package org.forestframework.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import kotlin.coroutines.Continuation;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;
import org.apache.commons.math3.util.Pair;
import org.forestframework.KotlinSuspendFunctionBridge;
import org.forestframework.ResponseProcessor;
import org.forestframework.RoutingHandlerArgumentResolver;
import org.forestframework.annotation.ArgumentResolvedBy;
import org.forestframework.annotation.Blocking;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.Get;
import org.forestframework.annotation.Intercept;
import org.forestframework.annotation.Post;
import org.forestframework.annotation.ReturnValueProcessedBy;
import org.forestframework.annotation.Route;
import org.forestframework.annotation.RouteType;
import org.forestframework.utils.ComponentScanUtils;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.forestframework.annotation.RouteType.AFTER_HANDLER_COMPLETION;
import static org.forestframework.annotation.RouteType.AFTER_HANDLER_SUCCESS;
import static org.forestframework.annotation.RouteType.HANDLER;
import static org.forestframework.annotation.RouteType.PRE_HANDLER;
import static org.forestframework.annotation.RouteType.values;

@Singleton
public class DefaultRoutingEngine implements RoutingEngine {
    private static final String ENABLED_STATES_KEY = "FOREST_ROUTING_ENGINE_ENABLED_STATES";
    private final Map<RouteType, List<Routing>> routings;
    private final Injector injector;
    private final Vertx vertx;

    @Inject
    public DefaultRoutingEngine(Injector injector,
                                Vertx vertx,
                                @ComponentClasses List<Class<?>> componentClasses) {
        this.injector = injector;
        this.vertx = vertx;
        this.routings = createRoutings(componentClasses);
    }

    protected Map<RouteType, List<Routing>> createRoutings(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(ComponentScanUtils::isRouter)
                .flatMap(this::findRoutingHandlers)
                .collect(Collectors.groupingBy(Routing::getRouteType));
    }

    @Override
    public Handler<HttpServerRequest> createRouter() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);

        configurePreHandlerRoute(router, routings.getOrDefault(PRE_HANDLER, emptyList()));
        configureHandlerRoute(router, routings.getOrDefault(HANDLER, emptyList()));
        configureFinalizingRoute(router);
        return router;
    }

    private void configureFinalizingRoute(io.vertx.ext.web.Router router) {
        io.vertx.ext.web.Route route = router.route("/*");
        for (HttpMethod method : HttpMethod.values()) {
            route = route.method(method.toVertxHttpMethod());
        }
        route.handler(RoutingContext::end);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void configureHandlerRoute(io.vertx.ext.web.Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            routing.configure(router).handler(ctx -> {
                RoutingContextDecorator context = new RoutingContextDecorator(ctx);
                if (context.get(ENABLED_STATES_KEY) == null || handlerEnabled(context, HANDLER)) {
                    Object[] arguments = resolveArguments(routing, context);
                    CompletableFuture<Object> returnValueFuture = invokeHandler(routing, arguments);

                    returnValueFuture.thenAccept(returnValue -> {
                        if (!context.isRerouteInvoked()) {
                            processResult(context, routing, returnValue).thenAccept(ret -> {
                                context.next();
                            }).exceptionally((Throwable failure) -> {
                                failure.printStackTrace();
                                context.put(ENABLED_STATES_KEY, Arrays.asList(AFTER_HANDLER_SUCCESS, AFTER_HANDLER_COMPLETION));
                                return null;
                            });
                        } else {
                            context.next();
                        }
                    }).exceptionally((Throwable failure) -> {
                        failure.printStackTrace();
                        context.put(ENABLED_STATES_KEY, Arrays.asList(AFTER_HANDLER_SUCCESS, AFTER_HANDLER_COMPLETION));
                        return null;
                    });
                } else {
                    context.next();
                }
            });
        }
    }

    private CompletableFuture<Object> processResult(RoutingContextDecorator context, Routing routing, Object returnValue) {
        List<Pair<ReturnValueProcessedBy, Class<?>>> annoAndResolverClass
                = findAnnotationWithType(routing.getHandlerMethod().getDeclaredAnnotations(), ReturnValueProcessedBy.class, ReturnValueProcessedBy::value);

        if (annoAndResolverClass.isEmpty()) {
            // TODO a fallback resolver
            throw new RuntimeException("processor not found");
        }
        if (annoAndResolverClass.size() > 1) {
            throw new RuntimeException("Found multiple resolvers!");
        }

        Annotation annotation = annoAndResolverClass.get(0).getFirst();
        Class<? extends ResponseProcessor> resolverClass = (Class<? extends ResponseProcessor>) annoAndResolverClass.get(0).getSecond();

        return adapt(injector.getInstance(resolverClass).processResponse(context, routing, returnValue, annotation));
    }


    private void configurePreHandlerRoute(io.vertx.ext.web.Router router, List<Routing> routings) {
        for (Routing routing : routings) {
            validPreHandler(routing);
            routing.configure(router).handler(context -> {
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
    private boolean handlerEnabled(RoutingContext routingContext, RouteType type) {
        return ((List<RouteType>) routingContext.data().getOrDefault(ENABLED_STATES_KEY, emptyList())).contains(type);
    }

    private void validPreHandler(Routing routing) {
        // TODO A valid pre handler must returns boolean/Boolean/Future[Boolean]
    }


    private <T> CompletableFuture<T> invokeHandler(Routing routing, Object[] arguments) {
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
            return adapt(routing.getHandlerMethod().invoke(routing.getHandlerInstance(), arguments));
        } catch (Throwable e) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    private <T> CompletableFuture<T> invokeBlockingViaJavaReflection(Routing routing, Object[] arguments) {
        return VertxCompletableFuture.from(vertx.executeBlocking((Promise<T> promise) -> {
            try {
                promise.complete((T) routing.getHandlerMethod().invoke(routing.getHandlerInstance(), arguments));
            } catch (Throwable e) {
                promise.fail(e);
            }
        }));
    }

    private boolean isBlockingMethod(Routing routing) {
        return routing.getHandlerMethod().getAnnotation(Blocking.class) != null;
    }

    private <T> CompletableFuture<T> invokeViaKotlinBridge(Routing routing, Object[] arguments) {
        return KotlinSuspendFunctionBridge.Companion.invoke(vertx, routing.getHandlerMethod(), routing.getHandlerInstance(), arguments);
    }

    private boolean isKotlinSuspendFunction(Routing routing) {
        Class<?>[] parameterTypes = routing.getHandlerMethod().getParameterTypes();
        return parameterTypes.length != 0 && isContinuation(parameterTypes[parameterTypes.length - 1]);
    }

    private Object[] resolveArguments(Routing routing, RoutingContext context) {
        Class<?>[] argumentsType = routing.getHandlerMethod().getParameterTypes();
        Object[] arguments = new Object[argumentsType.length];
        Annotation[][] argumentAnnontations = routing.getHandlerMethod().getParameterAnnotations();
        for (int i = 0; i < argumentsType.length; ++i) {
            if (!isContinuation(argumentsType[i])) {
                arguments[i] = resolveArgument(routing, argumentAnnontations[i], argumentsType[i], context);
            }
        }
        return arguments;
    }

    private boolean isContinuation(Class<?> argumentType) {
        return argumentType == Continuation.class;
    }

    /*
     * There're two annotation types involved. Suppose the handler is annontated by @A:
     *
     * @A
     * public void handlerMethod() {}
     *
     * And class A is annotated by B:
     *
     * @Retention(RUNTIME)
     * @B(value=XXX.class)
     * @interface A {}
     *
     */
    @SuppressWarnings({"rawtypes"})
    private <A extends Annotation, B extends Annotation> List<Pair<A, Class<?>>> findAnnotationWithType(Annotation[] annotations,
                                                                                                        Class<B> annotationClass,
                                                                                                        Function<B, Class<?>> function) {
        return Stream.of(annotations)
                .map(anno -> {
                    B targetAnnotation = anno.annotationType().getAnnotation(annotationClass);
                    if (targetAnnotation == null) {
                        return null;
                    } else {
                        Class<?> resolverClass = function.apply(targetAnnotation);
                        Pair<A, Class<?>> pair = Pair.create((A) anno, resolverClass);
                        return pair;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T resolveArgument(Routing routing, Annotation[] annotations, Class<T> argumentType, RoutingContext routingContext) {
        if (argumentType == RoutingContext.class) {
            return (T) routingContext;
        }

        List<Pair<ArgumentResolvedBy, Class<?>>> annoAndResolverClass = findAnnotationWithType(annotations, ArgumentResolvedBy.class, ArgumentResolvedBy::value);

        if (annoAndResolverClass.isEmpty()) {
            // TODO a fallback resolver
            throw new RuntimeException("resolver not found");
        }
        if (annoAndResolverClass.size() > 1) {
            throw new RuntimeException("Found multiple resolvers!");
        }

        Annotation annotation = annoAndResolverClass.get(0).getFirst();
        Class<? extends RoutingHandlerArgumentResolver> resolverClass = (Class<? extends RoutingHandlerArgumentResolver>) annoAndResolverClass.get(0).getSecond();

        return (T) injector.getInstance(resolverClass).resolveArgument(routing, argumentType, routingContext, annotation);
    }

    private Stream<Routing> findRoutingHandlers(Class<?> klass) {
        List<Pair<Method, Annotation>> routingHandlers = Stream.of(klass.getMethods())
                .map(this::toMethodAnnotationPair)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!routingHandlers.isEmpty()) {
            Object handlerInstance = injector.getInstance(klass);
            return routingHandlers.stream()
                    .map(methodAnnotationPair -> toRouting(klass, handlerInstance, methodAnnotationPair.getFirst(), methodAnnotationPair.getSecond()));
        } else {
            return Stream.empty();
        }
    }

    private Pair<Method, Annotation> toMethodAnnotationPair(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Route.class) {
                return Pair.create(method, annotation);
            }

            if (annotation.annotationType().getAnnotation(Route.class) != null) {
                return Pair.create(method, annotation);
            }
        }
        return null;
    }

    private Routing toRouting(Class<?> klass, Object handlerInstance, Method method, Annotation annotation) {
        String classRouterPath = klass.isAnnotationPresent(Route.class) ? klass.getAnnotation(Route.class).value() : "";
        String methodRoutePath = getPath(annotation);
        String path = classRouterPath + methodRoutePath;
        List<HttpMethod> httpMethods = getHttpMethods(annotation);
        return new PathRouting(path, httpMethods, klass, method, handlerInstance);
    }

    private String getPath(Annotation annotation) {
        if (annotation.annotationType() == Get.class) {
            return ((Get) annotation).value();
        } else if (annotation.annotationType() == Post.class) {
            return ((Post) annotation).value();
        } else if (annotation.annotationType() == Route.class) {
            return ((Route) annotation).value();
        } else if (annotation.annotationType() == Intercept.class) {
            return ((Intercept) annotation).value();
        }
        throw new IllegalStateException();
    }

    private List<HttpMethod> getHttpMethods(Annotation annotation) {
        if (annotation.annotationType() == Get.class) {
            return singletonList(HttpMethod.GET);
        } else if (annotation.annotationType() == Post.class) {
            return singletonList(HttpMethod.POST);
        } else if (annotation.annotationType() == Route.class) {
            HttpMethod[] methods = ((Route) annotation).methods();
            return Arrays.asList(methods);
        } else if (annotation.annotationType() == Intercept.class) {
            HttpMethod[] methods = ((Intercept) annotation).methods();
            return Arrays.asList(methods);
        }
        throw new IllegalStateException();
    }

//    private <T extends Annotation> Optional<T> getRouteAnnotation(Method method) {
//        Annotation[] annotations = method.getAnnotations();
//        for (Annotation annotation : annotations) {
//            if (annotation.getClass() == Route.class) {
//                return (T)annotation;
//            }
//
//            if (annotation.getClass().getAnnotation(Route.class) != null) {
//                return (T)annotation;
//            }
//        }
//        return false;
//    }

//    private boolean isRoutingHandler(Method method) {
//        Annotation[] annotations = method.getAnnotations();
//        for (Annotation annotation : annotations) {
//            if (annotation.getClass() == Route.class) {
//                return true;
//            }
//
//            if (annotation.getClass().getAnnotation(Route.class) != null) {
//                return true;
//            }
//        }
//        return false;
//    }
}
