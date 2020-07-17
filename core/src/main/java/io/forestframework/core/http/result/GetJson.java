package io.forestframework.core.http.result;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.CompositeOf;
import io.forestframework.core.http.routing.Get;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Get.class, JsonResponseBody.class})
public @interface GetJson {
    @AliasFor(target = Get.class, value = "path")
    String value() default "";

    @AliasFor(target = Get.class, value = "regex")
    String regex() default "";

    @AliasFor(target = JsonResponseBody.class, value = "pretty")
    boolean pretty() default false;
}
