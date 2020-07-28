package io.forestframework.core.http.result;

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
