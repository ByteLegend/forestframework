package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.common.net.MediaType;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.forestframework.core.ComponentClasses;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.Router;
import io.forestframework.core.http.bridge.Bridge;
import io.forestframework.core.http.bridge.BridgeEventType;
import io.forestframework.core.http.routing.DefaultBridgeRouting;
import io.forestframework.core.http.routing.DefaultRouting;
import io.forestframework.core.http.routing.DefaultWebSocketRouting;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.http.routing.RoutingType;
import io.forestframework.core.http.websocket.WebSocket;
import io.forestframework.core.http.websocket.WebSocketEventType;
import io.forestframework.core.modules.WebRequestHandlingModule;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.vertx.core.Future;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.forestframework.utils.StartupUtils.isBlockingMethod;
import static java.util.Collections.singletonList;

/**
 * Manage routing-related work at startup.
 *
 * <ol>
 *     <li>1. Register {@link HttpRequestHandler}s</li>
 *     <li>2. Scan all {@link io.forestframework.core.http.routing.Routing}s from component classes.</li>
 * </ol>
 */
@API(status = API.Status.INTERNAL)
public class AutoRoutingScanExtension implements Extension {
    private static final Pattern BRIDGE_ROUTING_PATH_PATTERN = Pattern.compile("(/|[^*:])*/?");

    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getComponentClasses().add(WebRequestHandlingModule.class);
    }

    @Override
    public void afterInjector(Injector injector) {
        RoutingManager routings = injector.getInstance(RoutingManager.class);

        // @formatter:off
        List<Class<?>> componentClasses = injector.getInstance(Key.get(new TypeLiteral<List<Class<?>>>() { }, ComponentClasses.class));
        // @formatter:on

        componentClasses.stream()
                .filter(AutoRoutingScanExtension::isRouter)
                .flatMap(this::findRoutingHandlers)
                .peek(this::validate)
                .peek(routing -> deleteExistingRootStaticResourceRoutingIfNecessary(routing, routings))
                .forEach(routing -> routings.getRouting(routing.getType()).add(routing));
    }

    // Ensure pre-handler return type is:
    //     void / boolean / Boolean / Future<Void> / CompletableFuture<Void> / Future<Boolean> / CompletableFuture<Boolean>
    // Throw exception otherwise.
    private void validate(Routing routing) {
        if (isPreHandlerReturnTypeNotValid(routing)) {
            throw new RuntimeException("PreHandler return type is not valid: " + routing.getHandlerMethod());
        }
    }

    private boolean isPreHandlerReturnTypeNotValid(Routing routing) {
        return routing.getType() == RoutingType.PRE_HANDLER && !isPreHandlerReturnTypeValid(routing);
    }

    private boolean isPreHandlerReturnTypeValid(Routing routing) {
        Class<?> returnType = routing.getHandlerMethod().getReturnType();

        Type genericReturnType = routing.getHandlerMethod().getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType) {
            return isGenericReturnTypeValid(genericReturnType);
        }
        return isReturnTypeValid(returnType);
    }

    private boolean isGenericReturnTypeValid(Type genericReturnType) {
        ParameterizedType paramType = (ParameterizedType) genericReturnType;
        Type rawType = paramType.getRawType();
        Type actualTypeArgument = paramType.getActualTypeArguments()[0];

        return (rawType == Future.class && actualTypeArgument == Void.class)
                || (rawType == CompletableFuture.class && actualTypeArgument == Void.class)
                || (rawType == Future.class && actualTypeArgument == Boolean.class)
                || (rawType == CompletableFuture.class && actualTypeArgument == Boolean.class);
    }

    private boolean isReturnTypeValid(Class<?> returnType) {
        return returnType == boolean.class
                || returnType == void.class
                || returnType == Boolean.class
                || returnType == Object.class; // Kotlin Unit, let's be lenient
    }

    // If user defined / mapping, delete the /index.html mapping, if necessary.
    private void deleteExistingRootStaticResourceRoutingIfNecessary(Routing routing, RoutingManager routings) {
        if (routing.getType() == RoutingType.HANDLER && "/".equals(routing.getPath())) {
            routings.getRouting(RoutingType.HANDLER).removeIf(item -> item instanceof StaticResourceExtension.RootPathRouting);
        }
    }

    private static boolean isRouter(Class<?> klass) {
        return AnnotationMagic.isAnnotationPresent(klass, Router.class)
                || Arrays.stream(klass.getMethods()).anyMatch(AutoRoutingScanExtension::isRouteMethod);
    }

    private Stream<Routing> findRoutingHandlers(Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(AutoRoutingScanExtension::isRouteMethod)
                .map(method -> toRouting(klass, method));
    }

    private static boolean isRouteMethod(Method method) {
        return !AnnotationMagic.getAnnotationsOnMethod(method, Route.class).isEmpty();
    }

    private Routing toRouting(Class<?> klass, Method method) {
        Route routeOnMethod = AnnotationMagic.getOneAnnotationOnMethodOrNull(method, Route.class);
        Router routeOnClass = findRouterOnClass(klass);
        String methodPath = getPath(routeOnMethod, method);
        String path = (routeOnClass == null ? "" : routeOnClass.value()) + methodPath;
        if (StringUtils.isNotBlank(routeOnMethod.regex())) {
            return createRouting(routeOnMethod, "", path, method);
        } else {
            return createRouting(routeOnMethod, path, "", method);
        }
    }

    private Router findRouterOnClass(Class<?> klass) {
        List<Router> routers = AnnotationMagic.getAnnotationsOnClass(klass, Router.class);
        if (routers.isEmpty()) {
            return null;
        } else {
            // @Router("/balabala")
            // @ForestApplication
            return routers.stream().filter(r -> !r.value().isEmpty()).findFirst().orElse(routers.get(0));
        }
    }

    @SuppressWarnings({"checkstyle:parameterassignment", "UnstableApiUsage"})
    private Routing createRouting(Route route, String path, String regexPath, Method handlerMethod) {
        if (AnnotationMagic.instanceOf(route, Bridge.class)) {
            List<BridgeEventType> eventTypes = Arrays.asList(AnnotationMagic.cast(route, Bridge.class).eventTypes());
            if (!regexPath.isEmpty()) {
                throw new IllegalArgumentException("Bridge routing doesn't support regex, but you used " + regexPath + " for " + handlerMethod);
            } else if (!BRIDGE_ROUTING_PATH_PATTERN.matcher(path).matches()) {
                throw new IllegalArgumentException("Bridge routing doesn't support wildcard, but you used " + path + " for " + handlerMethod);
            } else {
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                return new DefaultBridgeRouting(
                        isBlockingMethod(handlerMethod),
                        route.type(),
                        path,
                        regexPath,
                        handlerMethod,
                        eventTypes);
            }
        } else if (AnnotationMagic.instanceOf(route, WebSocket.class)) {
            List<WebSocketEventType> eventTypes = Arrays.asList(AnnotationMagic.cast(route, WebSocket.class).eventTypes());
            return new DefaultWebSocketRouting(
                    isBlockingMethod(handlerMethod),
                    route.type(),
                    path,
                    regexPath,
                    handlerMethod,
                    eventTypes);
        } else {
            return new DefaultRouting(
                    isBlockingMethod(handlerMethod),
                    route.type(),
                    path,
                    regexPath,
                    Arrays.asList(route.methods()),
                    handlerMethod,
                    route.order(),
                    singletonList(MediaType.ANY_TYPE.toString()), // TODO: produces/consumes
                    singletonList(MediaType.ANY_TYPE.toString()));
        }
    }

    private String getPath(Route route, Object target) {
        if (route == null) {
            return "";
        }
        if (StringUtils.isNotBlank(route.value()) && StringUtils.isNotBlank(route.regex())) {
            throw new IllegalArgumentException("Path and regexPath are both non-empty: " + target);
        } else if (StringUtils.isBlank(route.value()) && StringUtils.isBlank(route.path())) {
            return route.regex();
        } else if (StringUtils.isBlank(route.value())) {
            return verify(route.path());
        } else {
            return verify(route.value());
        }
    }

    private String verify(String path) {
        // @Router("/websocket")
        if (!path.startsWith("/") && !path.isEmpty()) {
            throw new IllegalArgumentException("All paths must starts with /, got " + path);
        }
        return path;
    }
}
