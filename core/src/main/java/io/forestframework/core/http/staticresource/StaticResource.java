package io.forestframework.core.http.staticresource;

import io.forestframework.core.http.result.ResultProcessor;
import com.github.blindpirate.annotationmagic.Extends;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Extends(ResultProcessor.class)
@ResultProcessor(by = StaticResourceProcessor.class)
public @interface StaticResource {
}

