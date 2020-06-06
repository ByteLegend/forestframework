package org.forestframework.annotationmagic;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class AnnotationMagic {
    // To avoid circular @Extends, walk max 10 super annotations.

    /**
     * Get annotation in the magic way.
     * For example, we have annotation X extends annotation Y,
     * {@code
     *
     * @X class C {}
     * }
     * Then getAnnotation(Y) returns an instance of X because X extends Y.
     * If more than one Y and Y's subannotation exist, an exception will be thrown.
     */
    public static <A extends Annotation> A getOneAnnotationOnClass(Class<?> targetClass, Class<A> annotationClass) {
        return assertZeroOrOne(getAnnotations(targetClass.getAnnotations(), annotationClass), targetClass);
    }

    private static <A extends Annotation> A assertZeroOrOne(List<A> annotations, Object target) {
        if (annotations.size() > 1) {
            throw new IllegalArgumentException("Found more than one annotation on " + target + ":\n"
                    + annotations.stream().map(Annotation::toString).collect(joining("\n")));
        }

        return annotations.isEmpty() ? null : annotations.get(0);
    }

    public static <A extends Annotation> A getOneAnnotationOnMethod(Method method, Class<A> targetAnnotationClass) {
        return assertZeroOrOne(getAnnotationsOnMethod(method, targetAnnotationClass), method);
    }

    public static <A extends Annotation> List<A> getAnnotationsOnMethod(Method method, Class<A> targetAnnotationClass) {
        return getAnnotations(method.getAnnotations(), targetAnnotationClass);
    }

    public static <A extends Annotation> A getOneAnnotationOnMethodParameter(Method method, int index, Class<A> targetAnnotation) {
        return assertZeroOrOne(getAnnotations(method.getParameterAnnotations()[index], targetAnnotation), method);
    }

    public static <A extends Annotation> List<A> getAnnotations(Annotation[] annotations, Class<A> targetClass) {
        return Stream.of(annotations)
                .map(annotation -> examineAnnotation(annotation, targetClass))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public static boolean instanceOf(Annotation annotation, Class<? extends Annotation> klass) {
        return getAnnotationHierarchy(annotation.annotationType()).contains(klass);
    }

    private static LinkedHashSet<Class<? extends Annotation>> getAnnotationHierarchy(Class<? extends Annotation> klass) {
        Class<? extends Annotation> currentClass = klass;
        LinkedHashSet<Class<? extends Annotation>> hierarchy = new LinkedHashSet<>();
        while (currentClass != null) {
            if (!hierarchy.add(currentClass)) {
                throw new IllegalArgumentException("Annotation hierarchy circular inheritance detected: " + currentClass);
            }
            currentClass = getSuperAnnotationOrNull(currentClass);
        }

        return hierarchy;
    }

    /*
     * Walk along `@Extends` annotation hierarchy to get all annotations.
     */
    private static <A extends Annotation> A examineAnnotation(Annotation actual, Class<A> targetAnnotationClass) {
        // Two passes:
        // 1. scan all annotation hierarchy classes
        // 2. construct a proxy with all information (probably overridden by sub annotations)
        LinkedHashSet<Class<? extends Annotation>> hierarchy = getAnnotationHierarchy(actual.annotationType());

        if (!hierarchy.contains(targetAnnotationClass)) {
            return null;
        }

        return (A) Proxy.newProxyInstance(targetAnnotationClass.getClassLoader(), new Class[]{targetAnnotationClass}, new InvocationHandler() {
            private Map<String, Optional<Object>> fieldsCache = new HashMap<>();

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Optional<Object> cachedField = fieldsCache.get(method.getName());
                if (cachedField == null) {
                    cachedField = searchInHierarchy(actual, targetAnnotationClass, hierarchy, method.getName());
                    fieldsCache.put(method.getName(), cachedField);
                }
                return cachedField.orElse(null);
            }
        });
    }

    private static Optional<Object> searchInHierarchy(Annotation actual, Class<? extends Annotation> targetAnnotationClass, LinkedHashSet<Class<? extends Annotation>> hierarchy, String name) {
        try {
            Method method = actual.annotationType().getMethod(name);
            return Optional.of(safeInvokeAnnotationMethod(method, actual));
        } catch (NoSuchMethodException e) {
            for (Class<? extends Annotation> klass : hierarchy) {
                Annotation[] annotationsOnCurrentAnnotationClass = klass.getAnnotations();
                for (Annotation annotationOnCurrentAnnotationClass : annotationsOnCurrentAnnotationClass) {
                    if (hierarchy.contains(annotationOnCurrentAnnotationClass.annotationType())) {
                        try {
                            Method method = annotationOnCurrentAnnotationClass.annotationType().getMethod(name);
                            return Optional.of(safeInvokeAnnotationMethod(method, annotationOnCurrentAnnotationClass));
                        } catch (NoSuchMethodException ignored) {
                            break;
                        }
                    }
                }
            }
            try {
                Method method = targetAnnotationClass.getMethod(name);
                return Optional.of(method.getDefaultValue());
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Object safeInvokeAnnotationMethod(Method method, Annotation annotation) {
        try {
            return method.invoke(annotation, new Object[]{});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<? extends Annotation> getSuperAnnotationOrNull(Class<? extends Annotation> currentClass) {
        Extends extendsAnnotation = currentClass.getAnnotation(Extends.class);
        return extendsAnnotation == null ? null : extendsAnnotation.value();
    }
}
