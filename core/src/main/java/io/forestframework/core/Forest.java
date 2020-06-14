package io.forestframework.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.forestframework.bootstrap.HttpServerStarter;
import io.forestframework.config.ConfigProvider;
import io.forestframework.ext.api.ApplicationConfigurer;
import io.forestframework.ext.api.ComponentsConfigurer;
import io.vertx.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class Forest {
    private static final List<Class<?>> CORE_EXTENSION_CLASSES = unmodifiableList(asList(AutoScanComponentsConfigurer.class, RouteAnnotationRoutingConfigurer.class));
    private static final Logger LOGGER = LoggerFactory.getLogger(Forest.class);

    public static void run(Class<?> applicationClass) {
        try {
            ConfigProvider configProvider = createConfigProvider();
            Unsafe.instrumentGuice(configProvider);
            createInjector(applicationClass, configProvider).getInstance(HttpServerStarter.class).start();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static ConfigProvider createConfigProvider() {
        try {
            InputStream is = Forest.class.getResourceAsStream("/forest.yml");
            String yaml = IOUtils.toString(is, Charset.defaultCharset());
            return ConfigProvider.fromYaml(yaml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T instantiateWithDefaultConstructor(Class<?> extensionClass) {
        try {
            return (T) extensionClass.getConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Can't instantiate " + extensionClass + ", an class instantiated at startup time must have an accessible default constructor.");
        }
    }

    /**
     * Create the injector and start. All classes instantiated at this phase must have an accessible default constructor.
     */
    private static Injector createInjector(Class<?> applicationClass, ConfigProvider configProvider) {
        ForestApplication annotation = applicationClass.getAnnotation(ForestApplication.class);
        if (annotation == null) {
            throw new RuntimeException("@ForestApplication not found on application class " + applicationClass);
        } else {
            Vertx vertx = Vertx.vertx();


            // 1. Start, component classes are empty at the beginning.
            List<Class<?>> componentClasses = new ArrayList<>();

            // class annotated with @ForestApplication is guaranteed to be the first component class
            componentClasses.add(applicationClass);
            componentClasses.addAll(Arrays.asList(annotation.include()));

            // 2. Instantiate all extensions and configure components.
            List<Class<?>> extensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);
            extensionClasses.addAll(Arrays.asList(annotation.extensions()));
            extensionClasses.stream().filter(extensionClass -> ComponentsConfigurer.class.isAssignableFrom(extensionClass))
                    .map(extensionClass -> (ComponentsConfigurer) instantiateWithDefaultConstructor(extensionClass))
                    .forEach(configuer -> configuer.configure(componentClasses));

            // 3. Filter out all Module classes, instantiate them and create the injector
            List<Module> modules = componentClasses.stream().filter(componentClass -> Module.class.isAssignableFrom(componentClass))
                    .map(componentClass -> (Module) instantiateWithDefaultConstructor(componentClass))
                    .collect(Collectors.toList());

            Module current = new CoreModule(vertx, applicationClass, configProvider, componentClasses);
            for (Module module : modules) {
                current = Modules.override(current).with(module);
            }
            Injector injector = Guice.createInjector(current);

            // 4. Inject members to modules because they're created by us, not Guice.
            modules.forEach(injector::injectMembers);

            // 5. Configure the application
            extensionClasses.stream().filter(extensionClass -> ApplicationConfigurer.class.isAssignableFrom(extensionClass))
                    .map(extensionClass -> (ApplicationConfigurer) instantiateWithDefaultConstructor(extensionClass))
                    .forEach(configuer -> configuer.configure(injector));

            return injector;
        }
    }
}

