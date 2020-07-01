package io.forestframework.core.http.result;

import io.forestframework.annotationmagic.CompositeOf;
import io.forestframework.core.http.routing.Get;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Get.class, JsonResponseBody.class})
public @interface GetJson {
}
