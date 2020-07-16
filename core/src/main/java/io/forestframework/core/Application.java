package io.forestframework.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.forestframework.core.config.ConfigProvider;
import io.forestframework.core.http.DefaultHttpVerticle;
import io.forestframework.core.http.routing.DefaultRoutings;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.ext.core.AutoScanComponentsExtension;
import io.forestframework.ext.core.BannerExtension;
import io.forestframework.ext.core.ForestApplicationAnnotationScanner;
import io.forestframework.ext.core.RoutingExtension;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * For internal use only. This is not public API.
 */
public class Application implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final List<Class<? extends Extension>> CORE_EXTENSION_CLASSES = unmodifiableList(asList(
            BannerExtension.class,
            ForestApplicationAnnotationScanner.class,
            AutoScanComponentsExtension.class,
            RoutingExtension.class)
    );
    private final Vertx vertx;
    private final StartupContext startupContext;
    private Injector injector;
    private String deploymentId;

    public Application(Class<?> applicationClass, List<Class<? extends Extension>> extensionClasses, ConfigProvider configProvider) {
        this.vertx = Vertx.vertx(configProvider.getInstance("forest.vertx", VertxOptions.class));
        this.startupContext = new StartupContext(vertx, applicationClass, configProvider, instantiateExtensions(extensionClasses));
    }

    public void start() {
        configureComponents();
        createInjector();
        configureApplication();
        startHttpServer();
    }

    public Injector getInjector() {
        return injector;
    }

    protected void configureComponents() {
        // 1. Start. Instantiate all extensions and configure components.
        startupContext.getExtensions().forEach(extension -> extension.beforeInjector(startupContext));
    }

    protected void createInjector() {
        // 2. Filter out all Module classes, instantiate them and create the injector
        List<Module> modules = startupContext.getComponentClasses().stream().filter(Module.class::isAssignableFrom)
                .map(componentClass -> (Module) instantiateWithDefaultConstructor(componentClass))
                .collect(Collectors.toList());

        injector = createInjector(startupContext, modules);

        // 3. Inject members to modules because they're created by us, not Guice.
        modules.forEach(injector::injectMembers);
    }

    protected void configureApplication() {
        // 4. Configure the application
        startupContext.getExtensions().forEach(extension -> extension.afterInjector(injector));
    }

    protected void startHttpServer() {
        // 5. Start the HTTP server
        DeploymentOptions deploymentOptions = startupContext.getConfigProvider().getInstance("forest.deploy", DeploymentOptions.class);
        injector.getInstance(DefaultRoutings.class).finalizeRoutings();
        Future<String> vertxFuture = vertx.deployVerticle(() -> injector.getInstance(DefaultHttpVerticle.class), deploymentOptions);
        CompletableFuture<String> future = VertxCompletableFuture.from(vertx.getOrCreateContext(), vertxFuture);
        try {
            deploymentId = future.get();
        } catch (Throwable e) {
            LOGGER.error("", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        VertxCompletableFuture.from(vertx.undeploy(deploymentId)).get();
    }

    public static <T> T instantiateWithDefaultConstructor(Class<?> extensionClass) {
        try {
            return (T) extensionClass.getConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Can't instantiate " + extensionClass + ", an class instantiated at startup time must have an accessible default constructor.");
        }
    }

    private static Injector createInjector(StartupContext startupContext, List<Module> modules) {
        Module current = new CoreModule(startupContext);
        for (Module module : modules) {
            current = Modules.override(current).with(module);
        }
        return Guice.createInjector(current);
    }

    private List<Extension> instantiateExtensions(List<Class<? extends Extension>> extensionClasses) {
        List<Class<? extends Extension>> allExtensionClasses = new ArrayList<>(CORE_EXTENSION_CLASSES);
        allExtensionClasses.addAll(extensionClasses);
        return allExtensionClasses.stream()
                .map(extensionClass -> (Extension) instantiateWithDefaultConstructor(extensionClass))
                .collect(Collectors.toList());
    }
}
