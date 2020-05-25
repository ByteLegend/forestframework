package org.forestframework.annotation;

import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

@TemplateRendering(engine = ThymeleafTemplateEngine.class)
public @interface ThymeleafTemplateRendering {
}
