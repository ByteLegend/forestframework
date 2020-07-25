package io.forestframework.core.http.routing;


import com.github.blindpirate.annotationmagic.AliasFor;
import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.Component;
import io.forestframework.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When Route is annotated on a class, only its path and regexpath can be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface Route {
    RoutingType type() default RoutingType.HANDLER;

    HttpMethod[] methods() default {HttpMethod.GET};

    @AliasFor("path")
    String value() default "";

    String path() default "";

    String regex() default "";
}


// Known sub-annotations
//
// +--- Get/Post/Patch/Delete
// +--- StaticResource
// +--- SocketJS
// +--- SocketJSBridge
// +--- Intercept
//   +--- PreHandler
//   +--- AfterSuccess
//   +--- AfterFailure
//   +--- AfterCompletion
//
// extension sub-annotations
// +--- GET
// +--- POST
// ...