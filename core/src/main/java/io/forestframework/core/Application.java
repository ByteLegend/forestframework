package io.forestframework.core;

import com.google.inject.GuiceExt;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.LookupInterceptor;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.forestframework.core.config.Config;
import io.forestframework.core.modules.CoreModule;
import io.forestframework.ext.api.After;
import io.forestframework.ext.api.Before;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.utils.StartupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.forestframework.utils.StartupUtils.instantiateWithDefaultConstructor;

/**
 * For internal use only. This is not public API.
 */
public class Application implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private final StartupContext startupContext;
    private Injector injector;

    public Application(StartupContext startupContext) {
        this.startupContext = startupContext;
    }

    public void start() {
        verifyOrder();
        configureComponents();
        createInjector();
        configureApplication();
    }

    private void verifyOrder() {
        Map<Class<? extends Extension>, Integer> extensionClassToIndexMap = IntStream.range(0, startupContext.getExtensions().size())
                .boxed()
                .collect(Collectors.toMap(i -> startupContext.getExtensions().get(i).getClass(), i -> i));
        for (int i = 0; i < startupContext.getExtensions().size(); ++i) {
            int iCopy = i;
            Extension extension = startupContext.getExtensions().get(i);
            After after = extension.getClass().getAnnotation(After.class);
            if (after != null) {
                Stream<Class<?>> classesShouldHappenEarlier = Stream.concat(Stream.of(after.classes()), Stream.of(after.classNames()).map(StartupUtils::loadClass));
                classesShouldHappenEarlier.filter(klass -> extensionClassToIndexMap.getOrDefault(klass, Integer.MIN_VALUE) > iCopy)
                        .findFirst()
                        .ifPresent(klass -> {
                            throw new RuntimeException("Can't start application. Extension " + extension.getClass() + " should be after extension " + klass + ", extension classes: " +
                                    startupContext.getExtensions().stream().map(Object::getClass).collect(Collectors.toList())
                            );
                        });
            }

            Before before = extension.getClass().getAnnotation(Before.class);
            if (before != null) {
                Stream<Class<?>> classesShouldHappenLater = Stream.concat(Stream.of(before.classes()), Stream.of(before.classNames()).map(StartupUtils::loadClass));
                classesShouldHappenLater.filter(klass -> extensionClassToIndexMap.getOrDefault(klass, Integer.MAX_VALUE) < iCopy)
                        .findFirst()
                        .ifPresent(klass -> {
                            throw new RuntimeException("Can't start application. Extension " + extension.getClass() + " should be before extension " + klass + ", extension classes: " +
                                    startupContext.getExtensions().stream().map(Object::getClass).collect(Collectors.toList())
                            );
                        });
            }
        }
    }

    public Injector getInjector() {
        return injector;
    }

    protected void configureComponents() {
        // 1. Start. Instantiate all extensions and configure components.
        startupContext.getExtensions().forEach(extension -> {
            LOGGER.debug("Apply extension beforeInjector: {}", extension.getClass());
            extension.beforeInjector(startupContext);
        });
    }

    protected void createInjector() {
        // 2. Filter out all Module classes, instantiate them and create the injector
        List<Module> modules = startupContext.getComponentClasses().stream().filter(Module.class::isAssignableFrom)
                .map(componentClass -> (Module) instantiateWithDefaultConstructor(componentClass))
                .collect(Collectors.toList());

        injector = createInjector(startupContext, modules);

        LOGGER.debug("Injector created successfully with {}", modules);

        // 3. Inject members to modules because they're created by us, not Guice.
        modules.forEach(injector::injectMembers);
    }

    protected void configureApplication() {
        // 4. Configure the application
        startupContext.getExtensions().forEach(extension -> extension.afterInjector(injector));
    }

    @Override
    public void close() throws Exception {
        for (Extension extension : startupContext.getExtensions()) {
            extension.close();
        }
    }

    private static Injector createInjector(StartupContext startupContext, List<Module> modules) {
        Module current = new CoreModule(startupContext);
        for (Module module : modules) {
            current = Modules.override(current).with(module);
        }
        return GuiceExt.createInjector(new LookupInterceptor() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T intercept(Key<T> key) {
                if (key.getAnnotation() != null && key.getAnnotation().annotationType() == Config.class) {
                    return (T) startupContext.getConfigProvider().getInstance(((Config) key.getAnnotation()).value(), key.getTypeLiteral().getRawType());
                }
                return null;
            }
        }, current);
    }
}
