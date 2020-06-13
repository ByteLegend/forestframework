package io.forestframework.launch;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import io.forestframework.annotation.ComponentClasses;
import io.forestframework.annotation.ForestApplication;
import io.forestframework.bootstrap.HttpServerStarter;
import io.forestframework.config.ConfigProvider;
import io.forestframework.ext.core.AutoScanComponentClassConfigurer;
import io.forestframework.ext.core.RouteAnnotationRoutingConfigurer;
import io.forestframework.ext.api.ComponentClassConfigurer;
import io.forestframework.utils.ComponentScanUtils;
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
    private static final List<Class<?>> CORE_EXTENSION_CLASSES = unmodifiableList(asList(AutoScanComponentClassConfigurer.class, RouteAnnotationRoutingConfigurer.class));
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

    private static Injector createInjector(Class<?> applicationClass, ConfigProvider configProvider) {
        ForestApplication annotation = applicationClass.getAnnotation(ForestApplication.class);
        if (annotation == null) {
            throw new RuntimeException("@ForestApplication not found on application class " + applicationClass);
        } else {
            Vertx vertx = Vertx.vertx();

            List<Class<?>> extensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);



            Injector bootstrapInjector = Guice.createInjector(new CoreModule(vertx, applicationClass, configProvider));
            extensionClasses.addAll(Arrays.asList(annotation.include()));
            List<Class<?>> componentClasses = getComponentClasses(bootstrapInjector, extensionClasses);
            return createAppInjector(bootstrapInjector, vertx, applicationClass, configProvider, componentClasses);
        }
    }

    private static List<Module> getModules(Injector coreInjector, List<Class<?>> componentClasses) {
        return componentClasses.stream()
                .filter(ComponentScanUtils::isGuiceModule)
                .map(moduleClass -> (Module) coreInjector.getInstance(moduleClass))
                .collect(Collectors.toList());
    }

    private static List<Class<?>> getComponentClasses(Injector coreInjector, List<Class<?>> extensionClasses) {
        List<Class<?>> componentClasses = new ArrayList<>();
        for (Class<?> extensionClass : extensionClasses) {
            if (extensionClass.isAssignableFrom(Module.class)) {
                componentClasses.add(extensionClass);
            } else if (ComponentClassConfigurer.class.isAssignableFrom(extensionClass)) {
                coreInjector.getInstance((Class<ComponentClassConfigurer>) extensionClass).configure(componentClasses);
            }
        }
        return new ArrayList<>(componentClasses);
    }


    private static Injector createAppInjector(Injector bootstrapInjector,
                                              Vertx vertx,
                                              Class<?> applicationClass,
                                              ConfigProvider configProvider,
                                              List<Class<?>> componentClasses) {
        Module current = new ExtensionModule(vertx, applicationClass, configProvider, componentClasses);
        for (Module module : getModules(bootstrapInjector, componentClasses)) {
            current = Modules.override(current).with(module);
        }
        return Guice.createInjector(current);
    }


    private static class ExtensionModule extends CoreModule {
        private final List<Class<?>> componentClasses;

        public ExtensionModule(Vertx vertx, Class<?> applicationClass, ConfigProvider configProvider, List<Class<?>> componentClasses) {
            super(vertx, applicationClass, configProvider);
            this.componentClasses = componentClasses;
        }

        @Override
        protected void configure() {
            super.configure();
            bind(new TypeLiteral<List<Class<?>>>() {
            }).annotatedWith(ComponentClasses.class).toInstance(componentClasses);
        }
    }
}

