package io.forestframework.utils;

import com.esotericsoftware.reflectasm.MethodAccess;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
    private static Map<Method, InvocationContext> methodCache = new ConcurrentHashMap<>();

    public static <T> T invoke(Method method, Object instance, Object[] arguments) {
        return (T) methodCache.computeIfAbsent(method, InvocationContext::new).invoke(instance, arguments);
    }

    private static class InvocationContext {
        private MethodAccess access;
        private int index;

        private InvocationContext(Method method) {
            access = MethodAccess.get(method.getDeclaringClass());
            index = access.getIndex(method.getName(), method.getParameterTypes());
        }

        private Object invoke(Object instance, Object[] arguments) {
            return access.invoke(instance, index, arguments);
        }
    }
}
