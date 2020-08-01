package io.forestframework.core.http;

import com.github.blindpirate.annotationmagic.Extends;
import io.forestframework.core.Component;
import org.apiguardian.api.API;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Scope
@Extends(Component.class)
public @interface Router {
    /**
     * The prefix of all route paths. For example,
     *
     * <pre>
     * @literal @Router("/users")
     * class {
     *     @literal @Get("/:id")
     *     public User getUserById(){}
     *
     *     @literal @Get("")
     *     public User getAllUsers(){}
     * }
     * </pre>
     *
     * @return the route prefix.
     */
    String value() default "";
}
