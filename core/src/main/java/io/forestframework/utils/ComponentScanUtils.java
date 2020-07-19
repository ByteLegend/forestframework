package io.forestframework.utils;

import com.github.blindpirate.annotationmagic.AnnotationMagic;
import io.forestframework.core.Component;
import io.forestframework.core.ForestApplication;
import io.forestframework.core.http.routing.Route;

/**
 * For internal use only.
 */
public class ComponentScanUtils {
    public static boolean isComponentClass(Class<?> klass) {
        return AnnotationMagic.isAnnotationPresent(klass, Component.class);
    }

    public static ForestApplication getApplicationAnnotation(Class<?> appClass) {
        ForestApplication forestApplication = AnnotationMagic.getOneAnnotationOnClassOrNull(appClass, ForestApplication.class);
        if (forestApplication == null) {
            throw new RuntimeException("@ForestApplication not found on application class " + appClass);
        } else {
            return forestApplication;
        }
    }
}
