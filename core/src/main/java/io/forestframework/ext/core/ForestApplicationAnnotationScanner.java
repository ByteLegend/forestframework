package io.forestframework.ext.core;

import io.forestframework.core.ForestApplication;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.api.StartupContext;
import io.forestframework.utils.ComponentScanUtils;

import java.util.Arrays;

/**
 * Scan component classes in {@link ForestApplication#include()} and {@link ForestApplication#includeName()}
 */
public class ForestApplicationAnnotationScanner implements Extension {
    @Override
    public void beforeInjector(StartupContext startupContext) {
        ForestApplication forestApplication = ComponentScanUtils.getApplicationAnnotation(startupContext.getAppClass());
        startupContext.getComponentClasses().add(startupContext.getAppClass());
        startupContext.getComponentClasses().addAll(Arrays.asList(forestApplication.include()));
    }
}
