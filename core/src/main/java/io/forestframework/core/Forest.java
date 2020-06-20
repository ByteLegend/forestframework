package io.forestframework.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.forestframework.bootstrap.HttpServerStarter;
import io.forestframework.config.ConfigProvider;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.ExtensionContext;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
            Vertx vertx = Vertx.vertx(configProvider.getInstance("forest.vertx", VertxOptions.class));

            // 1. Start, component classes are empty at the beginning.
            List<Class<?>> componentClasses = getInitialComponentClasses(applicationClass, annotation);

            ExtensionContext extensionContext = new ExtensionContext(vertx, applicationClass, configProvider, componentClasses);

            // 2. Instantiate all extensions and configure components.
            List<Extension> extensions = instantiateExtensions(annotation);
            extensions.forEach(extension -> extension.beforeInjector(extensionContext));

            // 3. Filter out all Module classes, instantiate them and create the injector
            List<Module> modules = componentClasses.stream().filter(componentClass -> Module.class.isAssignableFrom(componentClass))
                    .map(componentClass -> (Module) instantiateWithDefaultConstructor(componentClass))
                    .collect(Collectors.toList());

            Injector injector = createInjector(extensionContext, modules);

            // 4. Inject members to modules because they're created by us, not Guice.
            modules.forEach(injector::injectMembers);

            // 5. Configure the application
            extensions.forEach(extension -> extension.configure(injector));

            return injector;
        }
    }

    private static Injector createInjector(ExtensionContext extensionContext, List<Module> modules) {
        Module current = new CoreModule(extensionContext);
        for (Module module : modules) {
            current = Modules.override(current).with(module);
        }
        return Guice.createInjector(current);
    }

    private static List<Extension> instantiateExtensions(ForestApplication annotation) {
        List<Class<?>> extensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);
        extensionClasses.addAll(Arrays.asList(annotation.extensions()));
        return extensionClasses.stream()
                .map(extensionClass -> (Extension) instantiateWithDefaultConstructor(extensionClass))
                .collect(Collectors.toList());
    }

    private static List<Class<?>> getInitialComponentClasses(Class<?> applicationClass, ForestApplication annotation) {
        List<Class<?>> componentClasses = new ArrayList<>();
        // class annotated with @ForestApplication is guaranteed to be the first component class
        componentClasses.add(applicationClass);
        componentClasses.addAll(Arrays.asList(annotation.include()));

        return componentClasses;
    }
}

