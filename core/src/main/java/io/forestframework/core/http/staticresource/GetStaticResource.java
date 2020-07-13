package io.forestframework.core.http.staticresource;

import io.forestframework.core.http.routing.Get;
import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.CompositeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Get.class, StaticResource.class})
public @interface GetStaticResource {
    @AliasFor(target = StaticResource.class, value = "webroot")
    String webroot() default "";

    @AliasFor(target = Get.class, value = "path")
    String value() default "";

    @AliasFor(target = Get.class, value = "path")
    String path() default "";

    @AliasFor(target = Get.class, value = "regex")
    String regex() default "";
}