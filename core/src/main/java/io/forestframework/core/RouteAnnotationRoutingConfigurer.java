package io.forestframework.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.forestframework.annotation.Route;
import io.forestframework.annotation.RoutingType;
import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.ext.api.Extension;
import io.forestframework.http.DefaultRouting;
import io.forestframework.http.Routing;
import io.forestframework.http.Routings;
import io.forestframework.utils.ComponentScanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RouteAnnotationRoutingConfigurer implements Extension {
    @Override
    public void configure(Injector injector) {
        Map<RoutingType, List<Routing>> routings = injector.getInstance(Key.get(new TypeLiteral<Map<RoutingType, List<Routing>>>() {
        }, Routings.class));

        List<Class<?>> componentClasses = injector.getInstance(Key.get(new TypeLiteral<List<Class<?>>>() {
        }, ComponentClasses.class));

        componentClasses.stream()
                .filter(ComponentScanUtils::isRouter)
                .flatMap(componentClass -> findRoutingHandlers(injector, componentClass))
                .forEach(routing -> routings.get(routing.getType()).add(routing));

    }

    private Stream<Routing> findRoutingHandlers(Injector injector, Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(this::isRouteMethod)
                .map(method -> toRouting(klass, injector.getInstance(klass), method));
    }

    private boolean isRouteMethod(Method method) {
        return !AnnotationMagic.getAnnotationsOnMethod(method, Route.class).isEmpty();
    }

    @VisibleForTesting
    Routing toRouting(Class<?> klass, Object handlerInstance, Method method) {
        Route routeOnMethod = AnnotationMagic.getOneAnnotationOnMethod(method, Route.class);
        Route routeOnClass = AnnotationMagic.getOneAnnotationOnClass(klass, Route.class);
        if (routeOnClass == null && routeOnMethod != null) {
            return new DefaultRouting(routeOnMethod, klass, method, handlerInstance);
        }
        String classPath = getPath(routeOnClass, klass);
        String methodPath = getPath(routeOnMethod, method);
        String path = classPath + methodPath;
        if (StringUtils.isNotBlank(routeOnMethod.regex()) || (routeOnClass != null && StringUtils.isNotBlank(routeOnClass.regex()))) {
            return new DefaultRouting("", path, Arrays.asList(routeOnMethod.methods()), klass, method, handlerInstance);
        } else {
            return new DefaultRouting(path, "", Arrays.asList(routeOnMethod.methods()), klass, method, handlerInstance);
        }
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