package org.forestframework.ext.api;

import java.util.LinkedHashSet;

/**
 * Configure which classes are used to create app injector.
 * Implementations usually add their Module and router classes to the componentClasses
 */
public interface ComponentClassConfigurer {
    void configure(LinkedHashSet<Class<?>> componentClasses);
}
