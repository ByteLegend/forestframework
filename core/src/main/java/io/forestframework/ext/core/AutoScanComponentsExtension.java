package io.forestframework.ext.core;

import com.google.common.reflect.ClassPath;
import io.forestframework.core.ForestApplication;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.utils.StartupUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.reflect.ClassPath.from;
import static io.forestframework.ext.core.AutoScanComponents.APPLICATION_PACKAGE;

public class AutoScanComponentsExtension implements Extension {
    @Override
    public void beforeInjector(StartupContext startupContext) {
        LinkedHashSet<Class<?>> componentClasses = new LinkedHashSet<>(startupContext.getComponentClasses());

        determineBasePackages(startupContext).forEach(packageName -> scanAndAddComponentClasses(startupContext.getAppClass(), packageName, componentClasses));

        startupContext.getComponentClasses().clear();
        startupContext.getComponentClasses().addAll(componentClasses);
    }

    private LinkedHashSet<String> determineBasePackages(StartupContext startupContext) {
        LinkedHashSet<String> ret = startupContext.getEnableExtensionsAnnotation(AutoScanComponents.class)
                .stream()
                .map(AutoScanComponents::basePackages)
                .flatMap(Stream::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (startupContext.getEnableExtensionsAnnotation(ForestApplication.class) != null) {
            ret.add(APPLICATION_PACKAGE);
        }
        return ret;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void scanAndAddComponentClasses(Class<?> appClass, String packageName, LinkedHashSet<Class<?>> resultSet) {
        if (APPLICATION_PACKAGE.equals(packageName)) {
            packageName = appClass.getPackage().getName();
        }
        try {
            from(getClass().getClassLoader())
                    .getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(StartupUtils::isComponentClass)
                    .forEach(resultSet::add);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
