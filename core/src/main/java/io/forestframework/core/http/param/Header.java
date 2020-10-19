package io.forestframework.core.http.param;

import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Extends(ParameterResolver.class)
@ParameterResolver(resolver = HeaderParameterResolver.class)
public @interface Header {
    String value();
}
