package io.forestframework.ext.api;

import com.google.inject.Module;
import io.forestframework.core.config.ConfigProvider;
import io.vertx.core.Vertx;
import org.apiguardian.api.API;

import java.util.List;

/**
 * The context to start up a Forest application. It's used and configured by {@link Extension#start(ApplicationContext)}.
 * A typical extension implementation usually does the following:
 *
 * <ul>
 *     <li>Adds/removes application components from the context.</li>
 *     <li>Adds configs to {@link io.forestframework.core.config.ConfigProvider}.</li>
 *     <li>Does custom actions like database or application initialization.</li>
 * </ul>
 *
 * This phase happens before the core injector is created, so you can't do any DI at this point.
 *
 * The startup process is guaranteed to happen in a single non-Vert.x-managed thread.
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface ApplicationContext {
    /**
     * Get the Vert.x instance associated with the Forest application. It's guaranteed to be only one Vert.x
     * instance per Forest application.
     *
     * @return the Vert.x instance
     */
    Vertx getVertx();

    /**
     * Get the application class which Forest application starts from. It's usually the class annotated by
     * {@link io.forestframework.core.ForestApplication} and started via {@link io.forestframework.core.Forest#run(Class, String...)}.
     *
     * @return the application class
     */
    Class<?> getAppClass();

    /**
     * Get the global {@link ConfigProvider} instance, which is the core of Forest application configuration.
     * Extension implementations can configure it so the corresponding configuration can be read later.
     *
     * @return the core {@link ConfigProvider} instance
     */
    ConfigProvider getConfigProvider();

    /**
     * Get the immutable extension list. You can only retrieve but not modify things from it.
     *
     * @return the immutable extension list.
     */
    List<Extension> getExtensions();

    List<Module> getModules();
    /**
     * Get the mutable component classes which constructs the Forest application. Extension implementations
     * can adds, deletes, or reorders the component classes.
     *
     * @return the mutable component class list
     */
    List<Class<?>> getComponents();
}
