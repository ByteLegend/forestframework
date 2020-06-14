package io.forestframework.ext.api;

import java.util.List;

/**
 * Configure which classes are used to create the injector.
 * Implementations usually add their own component classes (Module or router classes) to the componentClasses.
 * It's also allowed to mutate the component classes, at your own risk.
 */
public interface ComponentsConfigurer {
    /**
     * Configure (add, remove or sort) the component classes.
     *
     * @param componentClasses current component classes.
     */
    void configure(List<Class<?>> componentClasses);
}
