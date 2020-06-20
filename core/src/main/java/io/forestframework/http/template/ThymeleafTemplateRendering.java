package io.forestframework.http.template;

import io.forestframework.ThymeleafRenderingProcessor;
import io.forestframework.annotation.ResultProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ResultProcessor(by = ThymeleafRenderingProcessor.class)
public @interface ThymeleafTemplateRendering {
}
