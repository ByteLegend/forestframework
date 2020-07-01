package io.forestframework.benchmark;

import io.forestframework.core.http.result.ResultProcessor;
import io.forestframework.annotationmagic.Extends;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ResultProcessor(by = RockerResultProcessor.class)
@Extends(ResultProcessor.class)
public @interface RockerTemplateRendering {
}
