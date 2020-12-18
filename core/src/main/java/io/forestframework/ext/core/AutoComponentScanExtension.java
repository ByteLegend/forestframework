package io.forestframework.ext.core;

import com.google.common.reflect.ClassPath;
import com.google.inject.Module;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.Extension;
import io.forestframework.utils.StartupUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.reflect.ClassPath.from;
import static io.forestframework.ext.core.AutoScanComponents.APPLICATION_PACKAGE;

public class AutoComponentScanExtension implements Extension {
    private final List<String> basePackages;

    public AutoComponentScanExtension() {
        this(Arrays.asList(APPLICATION_PACKAGE));
    }

    public AutoComponentScanExtension(AutoScanComponents autoScanComponents) {
        this(Arrays.asList(autoScanComponents.basePackages()));
    }

    private AutoComponentScanExtension(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public void start(ApplicationContext applicationContext) {
        LinkedHashSet<Class<?>> componentClasses = new LinkedHashSet<>(applicationContext.getComponents());
        basePackages.forEach(packageName -> scanAndAddComponentClasses(applicationContext, packageName, componentClasses));

        applicationContext.getComponents().clear();
        applicationContext.getComponents().addAll(componentClasses);
    }

    private void addModuleIfNecessary(ApplicationContext applicationContext, Class<?> componentClass) {
        if (Module.class.isAssignableFrom(componentClass)) {
            applicationContext.getModules().add(StartupUtils.instantiateWithDefaultConstructor(componentClass));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void scanAndAddComponentClasses(ApplicationContext applicationContext, String packageName, LinkedHashSet<Class<?>> resultSet) {
        if (APPLICATION_PACKAGE.equals(packageName)) {
            packageName = applicationContext.getAppClass().getPackage().getName();
        }
        try {
            from(getClass().getClassLoader())
                .getTopLevelClassesRecursive(packageName)
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(StartupUtils::isComponentClass)
                .forEach(klass -> {
                    if (resultSet.add(klass)) {
                        addModuleIfNecessary(applicationContext, klass);
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
