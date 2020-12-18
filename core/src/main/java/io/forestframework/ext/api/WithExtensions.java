package io.forestframework.ext.api;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithExtensions {
    Class<? extends Extension>[] extensions();
}
