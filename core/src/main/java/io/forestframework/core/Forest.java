package io.forestframework.core;

import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.internal.ExtensionScanner;
import io.forestframework.ext.api.ApplicationContext;
import io.forestframework.ext.api.DefaultApplicationContext;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        return new DefaultApplicationContext(
            createClusteredOrLocalVertx(configProvider),
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

    private static Vertx createClusteredOrLocalVertx(ConfigProvider configProvider) {
        if (configProvider.getInstance("vertx.clusterManager", Map.class) == null) {
            // No cluster manager, not clustered
            return Vertx.vertx();
        } else {
            VertxOptions vertxOptions = configProvider.getInstance("vertx", VertxOptions.class);
            CompletableFuture<Vertx> vertFuture = new CompletableFuture<>();
            Vertx.clusteredVertx(vertxOptions).onComplete(result -> {
                if (result.failed()) {
                    vertFuture.completeExceptionally(result.cause());
                } else {
                    vertFuture.complete(result.result());
                }
            });
            try {
                return vertFuture.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

