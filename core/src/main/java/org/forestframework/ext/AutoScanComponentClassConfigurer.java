package org.forestframework.ext;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import org.forestframework.annotation.ForestApplication;
import org.forestframework.ext.api.ComponentClassConfigurer;

import javax.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.reflect.ClassPath.from;
import static org.forestframework.utils.ComponentScanUtils.isGuiceModule;
import static org.forestframework.utils.ComponentScanUtils.isRouter;

public class AutoScanComponentClassConfigurer implements ComponentClassConfigurer {
    private final Class<?> applicationClass;

    @Inject
    public AutoScanComponentClassConfigurer(@Named("forest.application.class") Class<?> applicationClass) {
        this.applicationClass = applicationClass;
    }

    @Override
    public void configure(LinkedHashSet<Class<?>> componentClasses) {
        ForestApplication annotation = applicationClass.getAnnotation(ForestApplication.class);
        componentClasses.addAll(scanComponentClasses(applicationClass, annotation));
    }

    private List<Class<?>> scanComponentClasses(Class<?> applicationClass, ForestApplication annotation) {
        String packageName = applicationClass.getPackage().getName();
        try {
            LinkedHashSet<Class<?>> componentClasses = from(applicationClass.getClassLoader())
                    .getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(this::isComponentClass)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            componentClasses.addAll(Arrays.asList(annotation.include()));
            Stream.of(annotation.includeName()).map(this::loadClass).forEach(componentClasses::add);
            return new ArrayList<>(componentClasses);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isComponentClass(Class<?> klass) {
        return isGuiceModule(klass) || isRouter(klass);
    }

    private Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
