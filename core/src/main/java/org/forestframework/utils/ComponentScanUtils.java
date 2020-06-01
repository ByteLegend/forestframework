package org.forestframework.utils;

import com.google.inject.Module;
import org.forestframework.annotation.Get;
import org.forestframework.annotation.Route;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ComponentScanUtils {
    public static boolean isGuiceModule(Class<?> klass) {
        return Module.class.isAssignableFrom(klass);
    }

    public static boolean isRouter(Class<?> klass) {
        return klass.isAnnotationPresent(Route.class)
                || Arrays.stream(klass.getMethods()).anyMatch(ComponentScanUtils::isRouteMethod);
    }

    private static boolean isRouteMethod(Method method) {
        return method.isAnnotationPresent(Route.class)
                || method.isAnnotationPresent(Get.class);
    }
}
