package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.forestframework.core.ComponentClasses;
import io.forestframework.core.http.DefaultRouting;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.core.http.HttpRequestHandler;
import io.forestframework.core.http.Router;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.RoutingManager;
import io.forestframework.core.modules.WebRequestHandlingModule;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
                .forEach(routing -> routings.getRouting(routing.getType()).add(routing));
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

    @SuppressWarnings("ConstantConditions")
    private Routing toRouting(Class<?> klass, Method method) {
        Route routeOnMethod = AnnotationMagic.getOneAnnotationOnMethodOrNull(method, Route.class);
        Router routeOnClass = AnnotationMagic.getOneAnnotationOnClassOrNull(klass, Router.class);
        if (routeOnClass == null && routeOnMethod != null) {
            return new DefaultRouting(routeOnMethod, method);
        }
        String methodPath = getPath(routeOnMethod, method);
        String path = routeOnClass.value() + methodPath;
        if (StringUtils.isNotBlank(routeOnMethod.regex())) {
            return createRouting(routeOnMethod, "", path, Arrays.asList(routeOnMethod.methods()), method);
        } else {
            return createRouting(routeOnMethod, path, "", Arrays.asList(routeOnMethod.methods()), method);
        }
    }

    private Routing createRouting(Route route, String path, String regexPath, List<HttpMethod> methods, Method handlerMethod) {
//        if (AnnotationMagic.instanceOf(route, WebSocket.class)) {
//            return new DefaultWebSocketRouting(AnnotationMagic.cast(route, WebSocket.class), path, handlerMethod);
//        } else if (AnnotationMagic.instanceOf(route, SockJS.class)) {
//            return new DefaultSockJSRouting(AnnotationMagic.cast(route, SockJS.class), path, handlerMethod);
//        } else {
            return new DefaultRouting(route.type(), path, regexPath, methods, handlerMethod);
//        }
    }

    private String getPath(Route route, Object target) {
        if (route == null) {
            return "";
        }
        if (StringUtils.isNotBlank(route.value()) && StringUtils.isNotBlank(route.regex())) {
            throw new IllegalArgumentException("Path and regexPath are both non-empty: " + target);
        } else if (StringUtils.isBlank(route.value())) {
            return route.regex();
        } else {
            return route.value();
        }
    }
}

