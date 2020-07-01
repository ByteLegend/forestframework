package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.ext.api.Extension;

/**
 * Scan classpath and register all classpath:/static/* resources
 * as {@link io.forestframework.core.http.staticresource.GetStaticResource} routings.
 *
 * For example, classpath:/static/js/ directory will be registered as '/js/*'
 *
 * Specially, classpath:/static/index.html will be registered as '/' routing.
 *
 * Note that existing routings will not be overwritten. For example, if your application
 * defines route path '/', the default classpath:/static/index.html will not be registered.
 *
 * This extension is not enabled by default due to two reasons:
 *
 * 1. We don't want the static resource handling to be "magic". You have to enable this feature
 * explicitly via {@code @ForestApplication(extensions = StaticResourceExtension.class)}
 *
 * 2. Classpath scanning and wildcard route matching harms performance.
 */
public class StaticResourceExtension implements Extension {
    @Override
    public void afterInjector(Injector injector) {
    }
}
