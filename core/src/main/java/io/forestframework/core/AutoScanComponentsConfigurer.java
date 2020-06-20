package io.forestframework.core;

import com.google.common.reflect.ClassPath;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.reflect.ClassPath.from;
import static io.forestframework.utils.ComponentScanUtils.isGuiceModule;
import static io.forestframework.utils.ComponentScanUtils.isRouter;

public class AutoScanComponentsConfigurer implements Extension {
    private LinkedHashSet<Class<?>> scanComponentClasses(Class<?> applicationClass, ForestApplication annotation) {
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
            return componentClasses;
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

    @Override
    public void beforeInjector(ExtensionContext extensionContext) {
        LinkedHashSet<Class<?>> scannedClasses = scanComponentClasses(extensionContext.getApplicationClass(), extensionContext.getApplicationClass().getAnnotation(ForestApplication.class));
        List<Class<?>> componentClasses = extensionContext.getComponentClasses();
        scannedClasses.removeAll(componentClasses);
        componentClasses.addAll(scannedClasses);
    }
}
