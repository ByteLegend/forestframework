package io.forestframework.core;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.Extension;
import io.forestframework.logging.ForestConfigurationFactory;

import java.util.Arrays;
import java.util.List;

import static io.forestframework.utils.ComponentScanUtils.getApplicationAnnotation;
import static org.apache.logging.log4j.core.config.ConfigurationFactory.CONFIGURATION_FACTORY_PROPERTY;

public class Forest {
    public static final String VERSION = "0.1";

    public static Application run(Class<?> appClass, String... args) {
        return run(appClass, Arrays.asList(getApplicationAnnotation(appClass).extensions()), args);
    }

    public static Application run(Class<?> appClass, List<Class<? extends Extension>> extensions, String... args) {
        ConfigProvider configProvider = ConfigProvider.load();
        initLogger();
        UnsafeHack.instrumentGuice(configProvider);
        Application app = new Application(appClass, extensions, configProvider);
        app.start();
        return app;
    }

    private static void initLogger() {
        try {
            if (System.getProperty(CONFIGURATION_FACTORY_PROPERTY) == null) {
                System.setProperty(CONFIGURATION_FACTORY_PROPERTY, ForestConfigurationFactory.class.getName());
            }
        } catch (NoClassDefFoundError e) {
            // log4j is explicitly excluded, ignore
        }
    }
}

