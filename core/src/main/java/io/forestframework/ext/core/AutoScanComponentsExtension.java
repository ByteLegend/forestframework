package io.forestframework.ext.core;

import com.google.common.reflect.ClassPath;
import io.forestframework.core.ForestApplication;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.utils.ComponentScanUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.reflect.ClassPath.from;

public class AutoScanComponentsExtension implements Extension {
    @SuppressWarnings("UnstableApiUsage")
    private LinkedHashSet<Class<?>> scanComponentClasses(Class<?> applicationClass, ForestApplication annotation) {
        String packageName = applicationClass.getPackage().getName();
        try {
            LinkedHashSet<Class<?>> componentClasses = from(applicationClass.getClassLoader())
                    .getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(ComponentScanUtils::isComponentClass)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            componentClasses.addAll(Arrays.asList(annotation.include()));
            Stream.of(annotation.includeName()).map(this::loadClass).forEach(componentClasses::add);
            return componentClasses;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeInjector(StartupContext startupContext) {
        LinkedHashSet<Class<?>> scannedClasses = scanComponentClasses(startupContext.getAppClass(), startupContext.getAppClass().getAnnotation(ForestApplication.class));
        List<Class<?>> componentClasses = startupContext.getComponentClasses();
        scannedClasses.removeAll(componentClasses);
        componentClasses.addAll(scannedClasses);
    }
}
