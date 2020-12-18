package io.forestframework.core;

import com.github.blindpirate.annotationmagic.CompositeOf;
import com.github.blindpirate.annotationmagic.Extends;
import com.google.inject.BindingAnnotation;
import io.forestframework.core.http.Router;
import io.forestframework.ext.api.WithExtensions;
import io.forestframework.ext.core.AutoComponentScanExtension;
import io.forestframework.ext.core.AutoRoutingScanExtension;
import io.forestframework.ext.core.BannerExtension;
import io.forestframework.ext.core.HttpServerExtension;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Scope
@BindingAnnotation
@Extends(WithExtensions.class)
@WithExtensions(extensions = {BannerExtension.class, AutoComponentScanExtension.class, AutoRoutingScanExtension.class, HttpServerExtension.class})
@CompositeOf({Component.class, Router.class, Singleton.class})
public @interface ForestApplication {
}
