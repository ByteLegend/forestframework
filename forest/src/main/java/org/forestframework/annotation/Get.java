package org.forestframework.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Route(method = HttpMethod.GET)
public @interface Get {
    String value();
}
