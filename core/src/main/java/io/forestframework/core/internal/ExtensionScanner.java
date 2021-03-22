package io.forestframework.core.internal;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.forestframework.ext.api.After;
import io.forestframework.ext.api.Before;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.Repeatable;
import io.forestframework.ext.api.WithExtensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.forestframework.utils.StartupUtils.instantiateWithDefaultConstructor;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class ExtensionScanner {
    public static <T extends Annotation> List<Extension> scan(List<T> annotations) {
        List<ExtensionAndAnnotation> extensionAndAnnotations = new ArrayList<>();

        for (Annotation annotation : annotations) {
            if (AnnotationMagic.instanceOf(annotation, WithExtensions.class)) {
                for (Class<?> extensionClass : AnnotationMagic.cast(annotation, WithExtensions.class).extensions()) {
                    extensionAndAnnotations.add(new ExtensionAndAnnotation(extensionClass, annotation));
                }
            }
        }
        return reorderAndInstantiate(extensionAndAnnotations);
    }

    private static List<Extension> reorderAndInstantiate(List<ExtensionAndAnnotation> extensionAndAnnotations) {
        List<ExtensionAndAnnotation> deduplicated = deduplicate(extensionAndAnnotations);
        List<ExtensionAndAnnotation> reordered = reorder(deduplicated);
        return reordered.stream().map(ExtensionScanner::instantiate).collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private static Extension instantiate(ExtensionAndAnnotation extensionAndAnnotation) {
        try {
            Class<?> extensionClass = extensionAndAnnotation.getExtensionClass();
            Extension ret = instantiateWithAnnotationConstructor(extensionAndAnnotation);
            if (ret != null) {
                return ret;
            }
            return instantiateWithDefaultConstructor(extensionClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Extension instantiateWithAnnotationConstructor(ExtensionAndAnnotation extensionAndAnnotation) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Constructor<?> constructor : extensionAndAnnotation.getExtensionClass().getConstructors()) {
            if (constructor.getParameterTypes().length == 1
                && constructor.getParameterTypes()[0].isAssignableFrom(extensionAndAnnotation.annotation.getClass())) {
                return (Extension) constructor.newInstance(extensionAndAnnotation.annotation);
            }
        }
        return null;
    }

    private static List<ExtensionAndAnnotation> reorder(List<ExtensionAndAnnotation> extensionAndAnnotations) {
        Multimap<Class<?>, Class<?>> afters = HashMultimap.create();

        for (ExtensionAndAnnotation extensionAndAnnotation : extensionAndAnnotations) {
            Class<?> extensionClass = extensionAndAnnotation.extensionClass;

            List<Class<? extends Extension>> classesShouldHappenEarlier = Optional.ofNullable(extensionClass.getAnnotation(After.class))
                                                                                  .map(After::classes).map(Arrays::asList).orElse(Collections.emptyList());
            List<Class<? extends Extension>> classesShouldHappenLater = Optional.ofNullable(extensionClass.getAnnotation(Before.class))
                                                                                .map(Before::classes).map(Arrays::asList).orElse(Collections.emptyList());

            classesShouldHappenEarlier.forEach(it -> happenBefore(it, extensionClass, afters));
            classesShouldHappenLater.forEach(it -> happenBefore(extensionClass, it, afters));
        }

        checkCyclic(afters);

        List<ExtensionAndAnnotation> ret = new ArrayList<>();
        List<ExtensionAndAnnotation> candidates = new ArrayList<>(extensionAndAnnotations);

        while (!candidates.isEmpty()) {
            // find out the one with "nobody ahead"
            ExtensionAndAnnotation candidate = findTheEarliest(candidates, afters);
            System.out.println("Picking up " + candidate.extensionClass);
            candidates.remove(candidate);
            ret.add(candidate);
        }

        return ret;
    }

    private static ExtensionAndAnnotation findTheEarliest(
        List<ExtensionAndAnnotation> candidates,
        Multimap<Class<?>, Class<?>> afters
    ) {
        int counter = 0;
        ExtensionAndAnnotation current = candidates.get(0);
        while (true) {
            Set<Class<?>> earlierClasses = new HashSet<>(afters.get(current.extensionClass));
            if (earlierClasses.isEmpty()) {
                return current;
            }

            Optional<ExtensionAndAnnotation> optional = candidates.stream().filter(it -> earlierClasses.contains(it.extensionClass)).findAny();
            if (optional.isPresent()) {
                current = optional.get();
            } else {
                return current;
            }
            counter++;
            if (counter > candidates.size()) {
                throw new IllegalStateException("Cyclic dependency graph detected!");
            }
        }
    }

    private static void checkCyclic(Multimap<Class<?>, Class<?>> afters) {
        afters.keySet().forEach(it -> checkCyclic(afters, new LinkedHashSet<>(), it));
    }

    private static void checkCyclic(Multimap<Class<?>, Class<?>> afters, Set<Class<?>> seenClasses, Class<?> currentClass) {
        if (!seenClasses.add(currentClass)) {
            throw new IllegalStateException("Cycle: " +
                                                seenClasses
                                                    .stream()
                                                    .map(Object::toString)
                                                    .collect(Collectors.joining(" <- "))
                                                + " <- " + currentClass);
        }
        Collection<Class<?>> laterClasses = afters.get(currentClass);

        for (Class<?> laterClass : laterClasses) {
            checkCyclic(afters, new LinkedHashSet<>(seenClasses), laterClass);
        }
    }

    private static void happenBefore(Class<?> earlier, Class<?> later, Multimap<Class<?>, Class<?>> afters) {
        afters.put(later, earlier);
    }

    private static List<ExtensionAndAnnotation> deduplicate(List<ExtensionAndAnnotation> extensionAndAnnotations) {
        List<ExtensionAndAnnotation> ret = new ArrayList<>();
        for (ExtensionAndAnnotation extensionAndAnnotation : extensionAndAnnotations) {
            if (!extensionAndAnnotation.extensionClass.isAnnotationPresent(Repeatable.class)) {
                // override
                ret.remove(extensionAndAnnotation);
            }
            ret.add(extensionAndAnnotation);
        }
        return ret;
    }

    private static class ExtensionAndAnnotation {
        private final Class<?> extensionClass;
        private final Annotation annotation;

        private ExtensionAndAnnotation(Class<?> extensionClass, Annotation annotation) {
            this.extensionClass = extensionClass;
            this.annotation = annotation;
        }

        public Class<?> getExtensionClass() {
            return extensionClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExtensionAndAnnotation that = (ExtensionAndAnnotation) o;
            return Objects.equals(extensionClass, that.extensionClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extensionClass);
        }
    }
}
