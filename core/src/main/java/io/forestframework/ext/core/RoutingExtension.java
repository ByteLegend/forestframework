package io.forestframework.ext.core;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import io.forestframework.core.ComponentClasses;
import io.forestframework.core.http.DefaultRouting;
import io.forestframework.core.http.routing.DefaultRoutings;
import io.forestframework.core.http.routing.FastRequestHandler;
import io.forestframework.core.http.routing.RequestHandler;
import io.forestframework.core.http.routing.Route;
import io.forestframework.core.http.routing.RouterRequestHandler;
import io.forestframework.core.http.routing.Routing;
import io.forestframework.core.http.routing.Routings;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manage routing-related work at startup.
 *
 * <ol>
 *     <li>1. Register {@link RequestHandler}s</li>
 *     <li>2. Scan all {@link io.forestframework.core.http.routing.Routing}s from component classes.</li>
 * </ol>
 */
public class RoutingExtension implements Extension {
    @Override
    public void beforeInjector(StartupContext startupContext) {
        startupContext.getComponentClasses().add(RoutingModule.class);
    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    public @interface RoutingEngines {
    }

    public static class RoutingModule extends AbstractModule {
        private final Routings routings = new DefaultRoutings();

        @Override
        protected void configure() {
            bind(Routings.class).toInstance(routings);
        }

        @Provides
        @RoutingEngines
        public List<RequestHandler> defaultRoutingEngines(FastRequestHandler fastRoutingEngine, RouterRequestHandler routerRequestHandler) {
            return Collections.unmodifiableList(Arrays.asList(fastRoutingEngine, routerRequestHandler));
        }
    }

    @Override
    public void afterInjector(Injector injector) {
        Routings routings = injector.getInstance(Routings.class);

        // @formatter:off
        List<Class<?>> componentClasses = injector.getInstance(Key.get(new TypeLiteral<List<Class<?>>>() { }, ComponentClasses.class));
        // @formatter:on

        componentClasses.stream()
                .filter(RoutingExtension::isRouter)
                .flatMap(componentClass -> findRoutingHandlers(injector, componentClass))
                .forEach(routing -> routings.getRouting(routing.getType()).add(routing));
    }

    private static boolean isRouter(Class<?> klass) {
        return klass.isAnnotationPresent(Route.class)
                || Arrays.stream(klass.getMethods()).anyMatch(RoutingExtension::isRouteMethod);
    }

    private Stream<Routing> findRoutingHandlers(Injector injector, Class<?> klass) {
        return Stream.of(klass.getMethods())
                .filter(RoutingExtension::isRouteMethod)
                .map(method -> toRouting(klass, method));
    }

    private static boolean isRouteMethod(Method method) {
        return !AnnotationMagic.getAnnotationsOnMethod(method, Route.class).isEmpty();
    }

    @VisibleForTesting
    Routing toRouting(Class<?> klass, Method method) {
        Route routeOnMethod = AnnotationMagic.getOneAnnotationOnMethodOrNull(method, Route.class);
        Route routeOnClass = AnnotationMagic.getOneAnnotationOnClassOrNull(klass, Route.class);
        if (routeOnClass == null && routeOnMethod != null) {
            return new DefaultRouting(routeOnMethod, method);
        }
        String classPath = getPath(routeOnClass, klass);
        String methodPath = getPath(routeOnMethod, method);
        String path = classPath + methodPath;
        if (StringUtils.isNotBlank(routeOnMethod.regex()) || (routeOnClass != null && StringUtils.isNotBlank(routeOnClass.regex()))) {
            return new DefaultRouting("", path, Arrays.asList(routeOnMethod.methods()), method);
        } else {
            return new DefaultRouting(path, "", Arrays.asList(routeOnMethod.methods()), method);
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