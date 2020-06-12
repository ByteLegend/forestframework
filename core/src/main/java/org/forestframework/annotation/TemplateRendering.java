package org.forestframework.annotation;

import io.vertx.ext.web.common.template.TemplateEngine;
import org.forestframework.annotationmagic.Extends;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Extends(ResultProcessor.class)
public @interface TemplateRendering {
    Class<? extends TemplateEngine> engine();
}
