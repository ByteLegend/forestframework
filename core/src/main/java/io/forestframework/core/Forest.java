package io.forestframework.core;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.ext.api.DefaultStartupContext;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.utils.StartupUtils;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Forest {
    public static final String VERSION = "0.1";

    public static Application run(Class<?> appClass, String... args) {
        ConfigProvider configProvider = ConfigProvider.load();
        return run(createStartupContext(appClass, configProvider));
    }

    public static Application run(StartupContext startupContext) {
        initLogger();
        Application app = new Application(startupContext);
        app.start();
        return app;
    }

    private static StartupContext createStartupContext(Class<?> appClass, ConfigProvider configProvider) {
        Vertx vertx = Vertx.vertx();
        List<Extension> extensions = AnnotationMagic.getAnnotationsOnClass(appClass, EnableExtensions.class)
                .stream()
                .map(EnableExtensions::extensions)
                .flatMap(Stream::of)
                .map(StartupUtils::instantiateWithDefaultConstructor)
                .map(it -> (Extension) it)
                .collect(Collectors.toList());
        return new DefaultStartupContext(vertx, appClass, configProvider, extensions);
    }

    private static void initLogger() {
        try {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
        } catch (NoClassDefFoundError e) {
            // log4j is explicitly excluded, ignore
        }
    }
}

