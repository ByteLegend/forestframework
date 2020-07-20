package io.forestframework.core;

import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.CompositeOf;
import io.forestframework.core.http.Router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CompositeOf({Router.class, Component.class})
public @interface SingletonRouter {
    @AliasFor(target = Router.class, value = "value")
    String value() default "";
}
