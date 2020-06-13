package io.forestframework.ext.api;

import java.util.List;

/**
 * Configure which classes are used to create app injector.
 * Implementations usually add their Module and router classes to the componentClasses
 */
public interface ComponentClassConfigurer {
    void configure(List<Class<?>> componentClasses);
}
