package org.forestframework.annotation;

import io.vertx.ext.web.common.template.TemplateEngine;


public @interface TemplateRendering {
    Class<? extends TemplateEngine> engine();
}
