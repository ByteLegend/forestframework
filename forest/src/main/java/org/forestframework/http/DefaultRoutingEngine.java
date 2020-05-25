package org.forestframework.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import kotlin.coroutines.Continuation;
import org.apache.commons.math3.util.Pair;
import org.forestframework.KotlinSuspendFunctionBridge;
import org.forestframework.RoutingHandlerArgumentResolver;
import org.forestframework.annotation.ArgumentResolvedBy;
import org.forestframework.annotation.Blocking;
import org.forestframework.annotation.ComponentClasses;
import org.forestframework.annotation.Intercept;
import org.forestframework.annotation.Route;
import org.forestframework.annotation.Router;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class DefaultRoutingEngine implements RoutingEngine {
    private final List<Routing> routings;
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

    protected List<Routing> createRoutings(List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(this::isRouterClass)
                .flatMap(this::findRoutingHandlers)
                .collect(Collectors.toList());
    }

    private boolean isRouterClass(Class<?> klass) {
        return klass.getAnnotation(Router.class) != null;
    }

    @Override
    public Handler<HttpServerRequest> createRouter() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);
        routings.forEach(routing -> configureRouter(router, routing));
        return router;
    }

    private void configureRouter(io.vertx.ext.web.Router router, Routing routing) {
        routing.configure(router).handler(context -> {
            Object[] arguments = resolveArguments(routing, context);
            Object returnValue = invoke(routing, arguments);
            if (returnValue instanceof Future) {
                ((Future) returnValue).onSuccess(ret -> {
                    context.next();
                    context.response().end();
                }).onFailure((failure) -> {
                    ((Throwable) failure).printStackTrace();
                });
            } else {

            }
        });
    }

    private Object invoke(Routing routing, Object[] arguments) {
        if (isKotlinSuspendFunction(routing)) {
            return invokeViaKotlinBridge(routing, arguments);
        } else if (isBlockingMethod(routing)) {
            return invokeBlockingViaJavaReflection(routing, arguments);
        } else {
            return invokeViaJavaReflection(routing, arguments);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Future<T> invokeViaJavaReflection(Routing routing, Object[] arguments) {
        try {
            Object ret = routing.getHandlerMethod().invoke(routing.getHandlerInstance(), arguments);
            if (ret instanceof Future) {
                return (Future<T>) ret;
            } else {
                return (Future<T>) Future.succeededFuture(ret);
            }
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    private <T> Future<T> invokeBlockingViaJavaReflection(Routing routing, Object[] arguments) {
        return vertx.executeBlocking((Promise<T> promise) -> {
            try {
                promise.complete(routing.getHandlerMethod().invoke(routing.getHandlerInstance(), arguments);
            } catch (Throwable e) {
                promise.fail(e);
            }
        });
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
    }

    private boolean isContinuation(Class<?> argumentType) {
        return argumentType == Continuation.class;
    }

    @SuppressWarnings({"rawtypes"})
    private List<Pair<? extends Annotation, Class<? extends RoutingHandlerArgumentResolver>>> findAnnotationWithResolver(Annotation[] annotations) {
        return Stream.of(annotations)
                .map(anno -> {
                    ArgumentResolvedBy argumentResolvedBy = anno.getClass().getAnnotation(ArgumentResolvedBy.class);
                    if (argumentResolvedBy == null) {
                        return null;
                    } else {
                        Class<? extends RoutingHandlerArgumentResolver> resolverClass = argumentResolvedBy.value();
                        Pair<? extends Annotation, Class<? extends RoutingHandlerArgumentResolver>> pair = Pair.create(anno, resolverClass);
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

        List<Pair<? extends Annotation, Class<? extends RoutingHandlerArgumentResolver>>> annoAndResolverClass = findAnnotationWithResolver(annotations);

        if (annoAndResolverClass.isEmpty()) {
            // TODO a fallback resolver
            throw new RuntimeException("resolver not found");
        }
        if (annoAndResolverClass.size() > 1) {
            throw new RuntimeException("Found multiple resolvers!");
        }

        Annotation annotation = annoAndResolverClass.get(0).getFirst();
        Class<? extends RoutingHandlerArgumentResolver> resolverClass = annoAndResolverClass.get(0).getSecond();

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
            if (annotation.getClass() == Route.class) {
                return Pair.create(method, annotation);
            }

            if (annotation.getClass().getAnnotation(Route.class) != null) {
                return Pair.create(method, annotation);
            }
        }
        return null;
    }

    private Routing toRouting(Class<?> klass, Object handlerInstance, Method method, Annotation annotation) {
        String classRouterPath = klass.getAnnotation(Router.class).value();
        String methodRoutePath = getPath(annotation);
        String path = classRouterPath + methodRoutePath;
        List<HttpMethod> httpMethods = getHttpMethods(annotation);
        return new PathRouting(path, httpMethods, klass, method, handlerInstance);
    }

    private String getPath(Annotation annotation) {
        if (annotation.getClass() == Route.class) {
            return ((Route) annotation).value();
        } else if (annotation.getClass() == Intercept.class) {
            return ((Intercept) annotation).value();
        }
        throw new IllegalStateException();
    }

    private List<HttpMethod> getHttpMethods(Annotation annotation) {
        if (annotation.getClass() == Route.class) {
            HttpMethod[] methods = ((Route) annotation).methods();
            return Arrays.asList(methods);
        } else if (annotation.getClass() == Intercept.class) {
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
