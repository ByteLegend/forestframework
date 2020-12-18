package io.forestframework.core;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.internal.ExtensionScanner;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.DefaultApplicationContext;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Arrays;

public class Forest {
    public static final String VERSION = "0.1";

    public static ApplicationContext run(Class<?> appClass, String... args) {
        ConfigProvider configProvider = ConfigProvider.load();
        initLogger();
        DefaultApplicationContext applicationContext = createApplicationContext(appClass, configProvider);
        applicationContext.start();
        return applicationContext;
    }

    private static DefaultApplicationContext createApplicationContext(Class<?> appClass, ConfigProvider configProvider) {
        Vertx vertx = Vertx.vertx();
        return new DefaultApplicationContext(
            vertx,
            appClass,
            configProvider,
            ExtensionScanner.scan(Arrays.asList(appClass.getAnnotations())));
    }


    private static void initLogger() {
        try {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
        } catch (NoClassDefFoundError e) {
            // log4j is explicitly excluded, ignore
        }
    }
}

