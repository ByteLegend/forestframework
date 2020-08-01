package io.forestframework.core;

import com.github.blindpirate.annotationmagic.CompositeOf;
import com.github.blindpirate.annotationmagic.Extends;
import com.google.inject.BindingAnnotation;
import io.forestframework.core.http.Router;
import io.forestframework.ext.api.EnableExtensions;
import io.forestframework.ext.core.AutoRoutingScanExtension;
import io.forestframework.ext.core.AutoScanComponentsExtension;
import io.forestframework.ext.core.BannerExtension;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@BindingAnnotation
@Extends(EnableExtensions.class)
@EnableExtensions(extensions = {BannerExtension.class, AutoScanComponentsExtension.class, AutoRoutingScanExtension.class})
@CompositeOf({Component.class, Router.class})
@Scope
public @interface ForestApplication {
}
