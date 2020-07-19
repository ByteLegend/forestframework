package io.forestframework.core.http.result;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.CompositeOf;
import io.forestframework.core.http.result.PlainText;
import io.forestframework.core.http.routing.Get;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@CompositeOf({Get.class, PlainText.class})
public @interface GetPlainText {
    @AliasFor(target = Get.class, value = "value")
    String value() default "";

    @AliasFor(target = Get.class, value = "path")
    String path() default "";

    @AliasFor(target = Get.class, value = "regex")
    String regex() default "";
}
